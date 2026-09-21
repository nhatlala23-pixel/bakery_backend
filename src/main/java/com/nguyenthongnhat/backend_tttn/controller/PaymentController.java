package com.nguyenthongnhat.backend_tttn.controller;

import com.nguyenthongnhat.backend_tttn.entity.Order;
import com.nguyenthongnhat.backend_tttn.enums.OrderStatus;
import com.nguyenthongnhat.backend_tttn.enums.PaymentStatus;
import com.nguyenthongnhat.backend_tttn.repository.OrderRepository;
import com.nguyenthongnhat.backend_tttn.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;   
import java.util.TreeMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.nguyenthongnhat.backend_tttn.repository.PaymentRepository;
import com.nguyenthongnhat.backend_tttn.dto.PaymentResponse;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

    @Value("${tttn.momo.accessKey}")
    private String momoAccessKey;

    @Value("${tttn.momo.secretKey}")
    private String momoSecretKey;

    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(Pageable pageable) {
        Page<PaymentResponse> responses = paymentRepository.findAll(pageable).map(payment -> {
            Order order = payment.getOrder();
            return PaymentResponse.builder()
                    .id(payment.getId())
                    .orderId(order.getId())
                    .orderCode(order.getOrderCode())
                    .customerName(order.getReceiverName())
                    .paymentMethod(payment.getPaymentMethod().name())
                    .paymentStatus(payment.getPaymentStatus().name())
                    .amount(payment.getAmount())
                    .transactionNo(payment.getTransactionNo())
                    .gatewayTransactionId(payment.getGatewayTransactionId())
                    .paidAt(payment.getPaidAt())
                    .createdAt(payment.getCreatedAt())
                    .build();
        });
        return ResponseEntity.ok(responses);
    }

    /**
     * IPN Callback từ MoMo server (yêu cầu public URL - dùng ngrok trên môi trường local)
     */
    @PostMapping("/momo-callback")
    @Transactional
    public ResponseEntity<String> momoCallback(@RequestBody Map<String, Object> requestBody) {
        log.info("--- NHẬN IPN CALLBACK TỪ MOMO ---");
        log.info("MoMo Callback RequestBody: {}", requestBody);
        processOrderFromMoMoResponse(requestBody);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint để frontend gọi sau khi MoMo redirect về (redirectUrl).
     * Giải pháp cho môi trường local khi MoMo server không thể gọi về localhost.
     * Frontend gửi toàn bộ query params MoMo trả về trong body.
     */
    @PostMapping("/momo-return")
    @Transactional
    public ResponseEntity<Map<String, Object>> momoReturn(@RequestBody Map<String, Object> requestBody) {
        log.info("--- NHẬN RETURN TỪ MOMO (Frontend call) ---");
        log.info("MoMo Return Params: {}", requestBody);

        try {
            // Xác minh chữ ký HMAC để đảm bảo dữ liệu không bị giả mạo
            String receivedSignature = (String) requestBody.get("signature");
            if (receivedSignature != null && !verifyMoMoSignature(requestBody, receivedSignature)) {
                log.warn("Chữ ký MoMo không hợp lệ!");
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Chữ ký không hợp lệ"));
            }

            boolean updated = processOrderFromMoMoResponse(requestBody);

            Object resultCodeObj = requestBody.get("resultCode");
            int resultCode = -1;
            if (resultCodeObj instanceof Number) {
                resultCode = ((Number) resultCodeObj).intValue();
            } else if (resultCodeObj instanceof String) {
                resultCode = Integer.parseInt((String) resultCodeObj);
            }

            return ResponseEntity.ok(Map.of(
                "success", resultCode == 0,
                "updated", updated,
                "resultCode", resultCode,
                "orderId", requestBody.getOrDefault("orderId", "")
            ));
        } catch (Exception e) {
            log.error("Error processing MoMo return: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Xử lý chung: cập nhật trạng thái đơn hàng từ dữ liệu MoMo trả về.
     */
    private boolean processOrderFromMoMoResponse(Map<String, Object> data) {
        try {
            String orderId = (String) data.get("orderId");
            Object resultCodeObj = data.get("resultCode");
            int resultCode = -1;
            if (resultCodeObj instanceof Number) {
                resultCode = ((Number) resultCodeObj).intValue();
            } else if (resultCodeObj instanceof String) {
                resultCode = Integer.parseInt((String) resultCodeObj);
            }

            if (orderId == null || orderId.isBlank()) {
                log.warn("orderId bị null hoặc rỗng trong MoMo response");
                return false;
            }

            Order order = orderRepository.findByOrderCode(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderId));

            // Chỉ cập nhật nếu trạng thái chưa được xử lý (tránh cập nhật nhiều lần)
            if (order.getOrderStatus() != OrderStatus.PENDING) {
                log.info("Đơn hàng {} đã được xử lý trước đó (status: {}). Bỏ qua.", orderId, order.getOrderStatus());
                return false;
            }

            if (resultCode == 0) {
                if (order.getPayment() != null) {
                    order.getPayment().setPaymentStatus(PaymentStatus.SUCCESS);
                    if (data.containsKey("transId")) {
                        order.getPayment().setGatewayTransactionId(String.valueOf(data.get("transId")));
                        order.getPayment().setPaidAt(java.time.LocalDateTime.now());
                    }
                }
                order.setOrderStatus(OrderStatus.CONFIRMED);
                log.info("Đơn hàng {} đã thanh toán THÀNH CÔNG (resultCode=0).", orderId);
                
                try {
                    orderService.sendOrderConfirmationEmail(order.getId());
                } catch (Exception e) {
                    log.error("Lỗi khi gửi email xác nhận cho đơn hàng {}: {}", orderId, e.getMessage());
                }
            } else {
                if (order.getPayment() != null) {
                    order.getPayment().setPaymentStatus(PaymentStatus.FAILED);
                }
                order.setOrderStatus(OrderStatus.CANCELLED);
                log.warn("Đơn hàng {} thanh toán THẤT BẠI (resultCode={}).", orderId, resultCode);
            }
            orderRepository.save(order);
            return true;
        } catch (Exception e) {
            log.error("processOrderFromMoMoResponse error: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Xác minh chữ ký HMAC-SHA256 từ MoMo.
     */
    private boolean verifyMoMoSignature(Map<String, Object> params, String receivedSignature) {
        try {
            // Các field dùng để tạo signature khi MoMo redirect về (theo tài liệu MoMo)
            TreeMap<String, String> rawData = new TreeMap<>();
            String[] signFields = {"accessKey", "amount", "extraData", "message", "orderId",
                    "orderInfo", "orderType", "partnerCode", "payType", "requestId",
                    "responseTime", "resultCode", "transId"};
            for (String field : signFields) {
                Object val = params.get(field);
                if (val != null) {
                    rawData.put(field, String.valueOf(val));
                }
            }
            // Override accessKey với giá trị từ config
            rawData.put("accessKey", momoAccessKey);

            String rawHash = rawData.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(java.util.stream.Collectors.joining("&"));

            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    momoSecretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(rawHash.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b & 0xff));
            }
            String calculatedSignature = sb.toString();
            boolean valid = calculatedSignature.equals(receivedSignature);
            if (!valid) {
                log.warn("Signature mismatch! Expected: {} | Got: {}", calculatedSignature, receivedSignature);
            }
            return valid;
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }
}
