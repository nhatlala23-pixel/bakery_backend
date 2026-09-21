package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.dto.OrderRequest;
import com.nguyenthongnhat.backend_tttn.dto.OrderResponse;
import com.nguyenthongnhat.backend_tttn.entity.*;
import com.nguyenthongnhat.backend_tttn.enums.*;
import com.nguyenthongnhat.backend_tttn.mapper.OrderMapper;
import com.nguyenthongnhat.backend_tttn.repository.*;
import com.nguyenthongnhat.backend_tttn.service.MomoService;
import com.nguyenthongnhat.backend_tttn.service.OrderService;
import com.nguyenthongnhat.backend_tttn.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final PaymentRepository paymentRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final OrderMapper orderMapper;
    private final MomoService momoService;
    private final EmailService emailService;

    @Override
    @Transactional
    public OrderResponse placeOrder(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // 1. Khởi tạo đơn hàng sơ bộ
        Order order = Order.builder()
                .orderCode("ORD-" + System.currentTimeMillis())
                .user(user)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .shippingAddress(request.getShippingAddress())
                .note(request.getNote())
                .orderStatus(OrderStatus.PENDING)
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotalAmount = BigDecimal.ZERO;

        // 2. Xử lý từng sản phẩm & trừ kho
        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm biến thể id: " + itemReq.getVariantId()));

            if (variant.getStock() < itemReq.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + variant.getProduct().getProductName() + " không đủ tồn kho!");
            }

            // Tính tiền
            BigDecimal itemSubtotal = variant.getVariantPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotalAmount = subtotalAmount.add(itemSubtotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .variant(variant)
                    .productName(variant.getProduct().getProductName())
                    .productImage(variant.getThumbnailUrl())
                    .quantity(itemReq.getQuantity())
                    .price(variant.getVariantPrice())
                    .subtotal(itemSubtotal)
                    .build();
            orderItems.add(orderItem);

            // Cập nhật kho
            int stockBefore = variant.getStock();
            variant.setStock(stockBefore - itemReq.getQuantity());
            productVariantRepository.save(variant);

            // Ghi Log kho
            inventoryLogRepository.save(InventoryLog.builder()
                    .product(variant.getProduct())
                    .changeType(InventoryChangeType.EXPORT)
                    .quantityBefore(stockBefore)
                    .quantityChange(itemReq.getQuantity())
                    .quantityAfter(variant.getStock())
                    .note("Đặt hàng: " + order.getOrderCode())
                    .build());
        }

                // 3. Xử lý phí vận chuyển và Voucher
        log.debug(">>> [DEBUG] OrderRequest received. VoucherCode: [{}]", request.getVoucherCode());
        
        // Phí vận chuyển: 30k, miễn phí cho đơn từ 2 triệu
        BigDecimal shippingFee = subtotalAmount.compareTo(new BigDecimal("2000000")) >= 0 
                ? BigDecimal.ZERO 
                : new BigDecimal("30000");
        order.setShippingFee(shippingFee);

        BigDecimal discount = BigDecimal.ZERO;
        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            String cleanCode = request.getVoucherCode().trim();
            Voucher voucher = voucherRepository.findByCodeIgnoreCaseAndStatusAndEndDateAfter(
                    cleanCode, 1, LocalDateTime.now())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá [" + cleanCode + "] không hợp lệ hoặc đã hết hạn!"));
            
            log.debug(">>> [DEBUG] Found Voucher: {} | Type: {} | Value: {}", 
                       voucher.getCode(), voucher.getDiscountType(), voucher.getDiscountValue());

            if (voucher.getDiscountType() == DiscountType.PERCENTAGE || "PERCENTAGE".equals(voucher.getDiscountType().name())) {
                // Cách tính an toàn nhất: (Tạm tính * Giá trị giảm) / 100
                BigDecimal ratio = voucher.getDiscountValue().divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);
                discount = subtotalAmount.multiply(ratio).setScale(0, java.math.RoundingMode.HALF_UP);
                
                if (voucher.getMaxDiscountValue() != null && discount.compareTo(voucher.getMaxDiscountValue()) > 0) {
                    discount = voucher.getMaxDiscountValue();
                }
            } else {
                discount = voucher.getDiscountValue();
            }
            
            order.setVoucher(voucher);
            log.debug(">>> [DEBUG] Calculated Discount Result: {}", discount);
        }
        
        order.setDiscountAmount(discount);
        BigDecimal finalTotal = subtotalAmount.add(shippingFee).subtract(discount);
        order.setTotalAmount(finalTotal);
        
        log.info(">>> [DEBUG] FINAL PRICE FOR MOMO: {}", finalTotal);

        // 4. Lưu đơn hàng
        Order savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        // 5. Tạo thông tin thanh toán (PENDING)
        paymentRepository.save(Payment.builder()
                .order(savedOrder)
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .paymentStatus(PaymentStatus.PENDING)
                .amount(finalTotal)
                .build());

        // 6. Xóa giỏ hàng
        cartRepository.findByUserId(userId).ifPresent(cart -> 
            cartItemRepository.deleteByCartId(cart.getId())
        );

        OrderResponse response = orderMapper.toResponse(savedOrder);
        response.setTotalAmount(finalTotal); // Ensure response has the calculated total

        if (request.getPaymentMethod().equals("MOMO")) {
            String payUrl = momoService.createPaymentUrl(
                savedOrder.getOrderCode(),
                "[NEW] Thanh toan don hang " + savedOrder.getOrderCode(),
                finalTotal
            );
            response.setPaymentUrl(payUrl);
        }

        if (!"MOMO".equals(request.getPaymentMethod())) {
            // Send order confirmation email for COD and other methods immediately
            sendOrderConfirmationEmail(savedOrder.getId());
        }

        return response;
    }

    private String buildOrderConfirmationEmail(String customerName, String orderCode, String orderDate, 
            BigDecimal totalAmount, String paymentMethod, String shippingAddress) {
        return "<div style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">" +
                "<p>Kính chào Quý khách <strong>" + customerName + "</strong>,</p>" +
                "<p>TECHNO xin chân thành cảm ơn Quý khách đã tin tưởng và lựa chọn mua sắm tại cửa hàng của chúng tôi.</p>" +
                "<p>Đơn hàng của Quý khách đã được ghi nhận thành công với các thông tin sau:</p>" +
                "<ul>" +
                "<li><strong>Mã đơn hàng:</strong> " + orderCode + "</li>" +
                "<li><strong>Ngày đặt hàng:</strong> " + orderDate + "</li>" +
                "<li><strong>Tổng giá trị đơn hàng:</strong> " + String.format("%,.0f", totalAmount) + " VND</li>" +
                "<li><strong>Phương thức thanh toán:</strong> " + paymentMethod + "</li>" +
                "<li><strong>Địa chỉ nhận hàng:</strong> " + shippingAddress + "</li>" +
                "</ul>" +
                "<p>Chúng tôi sẽ tiến hành xác nhận và xử lý đơn hàng trong thời gian sớm nhất. Quý khách có thể theo dõi trạng thái đơn hàng thông qua tài khoản trên website hoặc liên hệ với bộ phận hỗ trợ nếu cần thêm thông tin.</p>" +
                "<p>Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ:</p>" +
                "<p><strong>TECHNO</strong><br>" +
                "Email: <a href=\"mailto:support@techno.vn\">support@techno.vn</a><br>" +
                "Hotline: 1900 xxxx<br>" +
                "Website: <a href=\"http://www.techno.vn\">www.techno.vn</a></p>" +
                "<p>Một lần nữa, TECHNO xin cảm ơn Quý khách đã đồng hành cùng chúng tôi.</p>" +
                "<p>Trân trọng,<br><strong>Đội ngũ TECHNO</strong></p>" +
                "</div>";
    }

    @Override
    public OrderResponse getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
    }

    @Override
    public List<OrderResponse> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
        order.setOrderStatus(OrderStatus.valueOf(status));
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, String cancelReason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ đơn hàng ở trạng thái Chờ xử lý mới có thể hủy!");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        String updatedNote = order.getNote() != null 
                ? order.getNote() + " (Lý do hủy: " + cancelReason + ")" 
                : "Lý do hủy: " + cancelReason;
        order.setNote(updatedNote);

        // Hoàn lại kho hàng cho từng sản phẩm trong đơn hàng
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                ProductVariant variant = item.getVariant();
                if (variant != null) {
                    int stockBefore = variant.getStock();
                    variant.setStock(stockBefore + item.getQuantity());
                    productVariantRepository.save(variant);

                    // Ghi log nhập kho
                    inventoryLogRepository.save(InventoryLog.builder()
                            .product(variant.getProduct())
                            .changeType(InventoryChangeType.IMPORT)
                            .quantityBefore(stockBefore)
                            .quantityChange(item.getQuantity())
                            .quantityAfter(variant.getStock())
                            .note("Hủy đơn hàng: " + order.getOrderCode())
                            .build());
                }
            }
        }

        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public void sendOrderConfirmationEmail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime orderTime = order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now();
        String formattedDate = orderTime.format(formatter);

        String paymentMethod = order.getPayment() != null && order.getPayment().getPaymentMethod() != null
                ? order.getPayment().getPaymentMethod().name()
                : "N/A";

        String emailContent = buildOrderConfirmationEmail(order.getUser().getFullName(), order.getOrderCode(), 
                formattedDate, order.getTotalAmount(), paymentMethod, order.getShippingAddress());
        
        emailService.sendHtmlEmail(order.getUser().getEmail(), "Xác nhận đặt hàng thành công - " + order.getOrderCode(), emailContent);
    }
}
