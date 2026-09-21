package com.nguyenthongnhat.backend_tttn.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nguyenthongnhat.backend_tttn.dto.ChatbotAnalyticsDTO;
import com.nguyenthongnhat.backend_tttn.dto.ChatbotChatRequest;
import com.nguyenthongnhat.backend_tttn.dto.ChatbotChatResponse;
import com.nguyenthongnhat.backend_tttn.dto.ProductResponse;
import com.nguyenthongnhat.backend_tttn.entity.*;
import com.nguyenthongnhat.backend_tttn.repository.*;
import com.nguyenthongnhat.backend_tttn.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotServiceImpl implements ChatbotService {

    private final ChatbotConfigRepository configRepository;
    private final ChatbotMessageRepository messageRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final OrderRepository orderRepository;
    private final PromotionRepository promotionRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public ChatbotChatResponse chat(ChatbotChatRequest request) {
        String messageText = request.getMessage() != null ? request.getMessage().trim() : "";
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();

        if (messageText.isEmpty()) {
            return ChatbotChatResponse.builder()
                    .reply("Dạ, em có thể giúp gì cho anh/chị hôm nay ạ?")
                    .suggestedProducts(new ArrayList<>())
                    .suggestions(Arrays.asList("Tìm laptop gaming", "Điện thoại iPhone", "Sản phẩm khuyến mãi"))
                    .build();
        }

        // 1. Save user message to history
        ChatbotMessage userMsg = ChatbotMessage.builder()
                .sessionId(sessionId)
                .sender("USER")
                .message(messageText)
                .createdAt(LocalDateTime.now())
                .build();
        messageRepository.save(userMsg);

        // 2. Fetch config
        ChatbotConfig config = configRepository.findFirstByStatus(1)
                .orElseGet(() -> ChatbotConfig.builder()
                        .provider("GEMINI")
                        .systemPrompt("Bạn là một nhân viên tư vấn bán lẻ chuyên nghiệp và nhiệt tình của Techno, website bán lẻ laptop, điện thoại và phụ kiện công nghệ hàng đầu Việt Nam. Hãy tư vấn khách hàng ngắn gọn, tự nhiên, tập trung vào sản phẩm, và luôn lịch sự chào khách.")
                        .faqData("Q: Shop có hỗ trợ trả góp không? A: Techno hỗ trợ trả góp 0% lãi suất qua thẻ tín dụng và công ty tài chính.\nQ: Chính sách bảo hành thế nào? A: Tất cả sản phẩm Techno bán ra đều được bảo hành chính hãng từ 12 đến 24 tháng.")
                        .status(1)
                        .build());

        // Expand Vietnamese chat abbreviations to improve search parsing & LLM understanding
        String expandedText = expandAbbreviations(messageText);

        // 3. Search database based on query to construct LLM context
        List<Product> matchedProducts = parseAndSearchProducts(expandedText);
        Order matchedOrder = parseAndSearchOrder(expandedText);
        List<Promotion> activePromotions = getActivePromotionsList();

        // 4. Call AI LLM if API Key is available, else fallback
        String replyText;
        List<ProductResponse> suggestedProductDTOs = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        if (config.getApiKey() != null && !config.getApiKey().trim().isEmpty()) {
            try {
                replyText = callLLM(config, expandedText, sessionId, matchedProducts, matchedOrder, activePromotions);
            } catch (Exception e) {
                log.error("AI API Call failed, fallback to rule-based: ", e);
                replyText = generateFallbackResponse(expandedText, matchedProducts, matchedOrder, activePromotions);
            }
        } else {
            replyText = generateFallbackResponse(expandedText, matchedProducts, matchedOrder, activePromotions);
        }

        // 5. Post-process AI response: extract suggested product IDs and build dynamic suggestions
        // Extract product IDs suggested by AI (look for pattern [PRODUCT:id] or similar)
        Set<Long> suggestedIds = new HashSet<>();
        Pattern productPattern = Pattern.compile("\\[PRODUCT:(\\d+)\\]");
        Matcher matcher = productPattern.matcher(replyText);
        while (matcher.find()) {
            suggestedIds.add(Long.parseLong(matcher.group(1)));
        }

        // Remove [PRODUCT:id] markers from output so it's clean markdown
        replyText = replyText.replaceAll("\\[PRODUCT:\\d+\\]", "").trim();

        // Map matched/suggested products to response DTOs
        if (!suggestedIds.isEmpty()) {
            for (Long id : suggestedIds) {
                productRepository.findById(id).ifPresent(p -> suggestedProductDTOs.add(mapToProductResponse(p)));
            }
        } else if (!matchedProducts.isEmpty()) {
            // Default to matched products in carousel if LLM did not explicitly format them
            matchedProducts.stream().limit(5).forEach(p -> suggestedProductDTOs.add(mapToProductResponse(p)));
        }

        // Build follow-up suggestions dynamically
        suggestions = buildDynamicSuggestions(messageText, matchedProducts, matchedOrder);

        // Detect add to cart action
        String action = null;
        Long actionProductId = null;
        String lowercaseQuery = expandedText.toLowerCase();
        if (lowercaseQuery.contains("thêm") && (lowercaseQuery.contains("giỏ") || lowercaseQuery.contains("cart"))) {
            if (!suggestedProductDTOs.isEmpty()) {
                action = "ADD_TO_CART";
                actionProductId = suggestedProductDTOs.get(0).getId();
                replyText = "Dạ, em đã tự động thêm sản phẩm **" + suggestedProductDTOs.get(0).getProductName() + "** vào giỏ hàng cho anh/chị rồi ạ! 🛒";
            } else if (!matchedProducts.isEmpty()) {
                action = "ADD_TO_CART";
                actionProductId = matchedProducts.get(0).getId();
                suggestedProductDTOs.add(mapToProductResponse(matchedProducts.get(0)));
                replyText = "Dạ, em đã tự động thêm sản phẩm **" + matchedProducts.get(0).getProductName() + "** vào giỏ hàng cho anh/chị rồi ạ! 🛒";
            } else {
                replyText = "Dạ, em không tìm thấy sản phẩm phù hợp để thêm vào giỏ hàng. Anh/chị có thể cung cấp tên sản phẩm chính xác hơn không?";
            }
        }

        // 6. Save AI reply to history
        Long mainProductId = suggestedProductDTOs.isEmpty() ? null : suggestedProductDTOs.get(0).getId();
        ChatbotMessage aiMsg = ChatbotMessage.builder()
                .sessionId(sessionId)
                .sender("AI")
                .message(replyText)
                .productSuggestedId(mainProductId)
                .createdAt(LocalDateTime.now())
                .build();
        messageRepository.save(aiMsg);

        return ChatbotChatResponse.builder()
                .reply(replyText)
                .suggestedProducts(suggestedProductDTOs)
                .suggestions(suggestions)
                .action(action)
                .actionProductId(actionProductId)
                .build();
    }

    @Override
    public ChatbotConfig getConfig() {
        return configRepository.findFirstByStatus(1)
                .orElseGet(() -> ChatbotConfig.builder()
                        .provider("GEMINI")
                        .systemPrompt("Bạn là một nhân viên tư vấn bán lẻ chuyên nghiệp và nhiệt tình của Techno, website bán lẻ laptop, điện thoại và phụ kiện công nghệ hàng đầu Việt Nam. Hãy tư vấn khách hàng ngắn gọn, tự nhiên, tập trung vào sản phẩm, và luôn lịch sự chào khách.")
                        .faqData("Q: Shop có hỗ trợ trả góp không? A: Techno hỗ trợ trả góp 0% lãi suất qua thẻ tín dụng và công ty tài chính.\nQ: Chính sách bảo hành thế nào? A: Tất cả sản phẩm Techno bán ra đều được bảo hành chính hãng từ 12 đến 24 tháng.")
                        .status(1)
                        .build());
    }

    @Override
    @Transactional
    public ChatbotConfig saveConfig(ChatbotConfig config) {
        configRepository.findFirstByStatus(1).ifPresent(existing -> config.setId(existing.getId()));
        config.setStatus(1);
        return configRepository.save(config);
    }

    @Override
    public ChatbotAnalyticsDTO getAnalytics() {
        long totalChats = messageRepository.count();
        long totalSessions = messageRepository.countChatsPerSession().size();
        
        // Calculate conversion rate: sessions containing AI replies suggesting a product / total sessions
        long sessionsWithSuggestions = messageRepository.findAll().stream()
                .filter(m -> "AI".equals(m.getSender()) && m.getProductSuggestedId() != null)
                .map(ChatbotMessage::getSessionId)
                .distinct()
                .count();

        double conversionRate = totalSessions > 0 ? ((double) sessionsWithSuggestions / totalSessions) * 100 : 0.0;

        // Top suggested products
        List<ChatbotAnalyticsDTO.TopProduct> topProducts = new ArrayList<>();
        List<Object[]> productData = messageRepository.findTopSuggestedProducts(PageRequest.of(0, 5));
        for (Object[] row : productData) {
            Long prodId = (Long) row[0];
            Long count = (Long) row[1];
            productRepository.findById(prodId).ifPresent(p -> {
                topProducts.add(new ChatbotAnalyticsDTO.TopProduct(
                        p.getId(), p.getProductName(), p.getSlug(), p.getThumbnail(), count
                ));
            });
        }

        // Top questions
        List<ChatbotAnalyticsDTO.TopQuestion> topQuestions = new ArrayList<>();
        List<Object[]> questionData = messageRepository.findTopQuestions(PageRequest.of(0, 5));
        for (Object[] row : questionData) {
            String question = (String) row[0];
            Long count = (Long) row[1];
            if (question.length() > 50) question = question.substring(0, 47) + "...";
            topQuestions.add(new ChatbotAnalyticsDTO.TopQuestion(question, count));
        }

        return ChatbotAnalyticsDTO.builder()
                .totalChats(totalChats)
                .totalSessions(totalSessions)
                .conversionRate(Math.round(conversionRate * 10.0) / 10.0)
                .topQuestions(topQuestions)
                .topSuggestedProducts(topProducts)
                .build();
    }

    @Override
    public List<ChatbotMessage> getHistory() {
        return messageRepository.findAll();
    }

    @Override
    @Transactional
    public void clearHistory() {
        messageRepository.deleteAll();
    }

    // --- Core Parsing & Matching Engines ---

    private String expandAbbreviations(String query) {
        if (query == null) return "";
        String result = " " + query.toLowerCase() + " ";
        
        // Product terminology abbreviations (supporting ip15, ip17, etc.)
        result = result.replaceAll("\\bip(\\d+)\\b", "iphone $1");
        result = result.replaceAll("\\bip\\b", "iphone");
        result = result.replaceAll("\\bip\\s+(\\d+)\\b", "iphone $1");
        result = result.replaceAll("\\bmac\\b", "macbook");
        result = result.replaceAll("\\blt\\b", "laptop");
        result = result.replaceAll("\\bđt\\b", "điện thoại");
        result = result.replaceAll("\\bmh\\b", "màn hình");
        result = result.replaceAll("\\btn\\b", "tai nghe");
        result = result.replaceAll("\\bpk\\b", "phụ kiện");
        result = result.replaceAll("\\bsp\\b", "sản phẩm");
        result = result.replaceAll("\\bss\\b", "samsung");
        
        // Feature/Policy abbreviations
        result = result.replaceAll("\\bbh\\b", "bảo hành");
        result = result.replaceAll("\\btg\\b", "trả góp");
        result = result.replaceAll("\\bkm\\b", "khuyến mãi");
        result = result.replaceAll("\\bdt\\b", "đổi trả");
        result = result.replaceAll("\\bvc\\b", "vận chuyển");
        result = result.replaceAll("\\bship\\b", "vận chuyển");
        result = result.replaceAll("\\bđh\\b", "đơn hàng");
        
        // Specs abbreviations
        result = result.replaceAll("\\bpin trâu\\b", "dung lượng pin lớn");
        result = result.replaceAll("\\bpin lau\\b", "pin sử dụng lâu");
        
        return result.trim();
    }

    private List<Product> parseAndSearchProducts(String query) {
        // Parse price range from query using the smart price parser
        ProductServiceImpl.PriceParseResult priceResult = ProductServiceImpl.parsePriceQuery(query);
        String cleanQuery = priceResult.cleanedKeyword.toLowerCase();
        java.math.BigDecimal minPrice = priceResult.minPrice;
        java.math.BigDecimal maxPrice = priceResult.maxPrice;

        // Detect brands & categories in database
        List<Category> allCategories = categoryRepository.findAll();
        List<Brand> allBrands = brandRepository.findAll();

        Category matchedCategory = allCategories.stream()
                .filter(c -> cleanQuery.contains(c.getCategoryName().toLowerCase()) 
                          || (cleanQuery.contains("iphone") && c.getCategoryName().toLowerCase().contains("điện thoại"))
                          || (cleanQuery.contains("macbook") && c.getCategoryName().toLowerCase().contains("laptop")))
                .findFirst()
                .orElse(null);

        Brand matchedBrand = allBrands.stream()
                .filter(b -> cleanQuery.contains(b.getBrandName().toLowerCase())
                          || (cleanQuery.contains("iphone") && b.getBrandName().toLowerCase().contains("apple"))
                          || (cleanQuery.contains("macbook") && b.getBrandName().toLowerCase().contains("apple")))
                .findFirst()
                .orElse(null);

        // Filter only meaningful keywords (length > 2, not stop-words)
        Set<String> STOP_WORDS = Set.of("thêm", "vào", "giỏ", "hàng", "cart", "cho", "tôi", "mình",
                "của", "bên", "này", "đây", "với", "và", "nào", "là", "có", "không",
                "sản", "phẩm", "giá", "mua", "xem", "tìm");
        String[] rawKeywords = cleanQuery.split("\\s+");
        List<String> keywords = Arrays.stream(rawKeywords)
                .filter(kw -> kw.length() >= 2 && !STOP_WORDS.contains(kw))
                .collect(Collectors.toList());

        // Score each product by relevance
        List<Product> products = productRepository.findAll();
        // Store [product, score] pairs
        List<Map.Entry<Product, Integer>> scoredProducts = new ArrayList<>();

        for (Product p : products) {
            if (p.getStatus() != 1) continue; // Only active products

            String nameLower = p.getProductName().toLowerCase();
            String descLower = p.getShortDescription() != null ? p.getShortDescription().toLowerCase() : "";

            int score = 0;

            // Brand/Category bonus: +5 points each (strong signal)
            if (matchedBrand != null && p.getBrand() != null &&
                p.getBrand().getId().equals(matchedBrand.getId())) {
                score += 5;
            }
            if (matchedCategory != null && p.getCategory() != null &&
                p.getCategory().getId().equals(matchedCategory.getId())) {
                score += 5;
            }

            // Keyword matching: only count keywords > 2 chars to avoid short-number false matches
            for (String kw : keywords) {
                // For short words (2-3 chars including numbers), require word-boundary style match in name
                if (kw.length() <= 3 && kw.matches("\\d+")) {
                    // Numeric model suffix: match only if surrounded by non-digit in product name
                    Pattern numPattern = Pattern.compile("(?<![\\d])\\Q" + kw + "\\E(?![\\d])");
                    if (nameLower.length() > 0 && numPattern.matcher(nameLower).find()) {
                        score += 2;
                    }
                } else if (kw.length() > 2) {
                    if (nameLower.contains(kw)) {
                        score += 3; // name match is stronger signal
                    } else if (descLower.contains(kw)) {
                        score += 1;
                    }
                }
            }

            // Price filtering – only include if meets price constraints
            if (score > 0) {
                if (minPrice != null && p.getSalePrice().compareTo(minPrice) < 0) {
                    score = 0;
                }
                if (score > 0 && maxPrice != null && p.getSalePrice().compareTo(maxPrice) > 0) {
                    score = 0;
                }
            }

            if (score > 0) {
                scoredProducts.add(new java.util.AbstractMap.SimpleEntry<>(p, score));
            }
        }

        // If no keyword products matched but query is generic ("sản phẩm", "danh sách", "gì")
        if (scoredProducts.isEmpty() && (cleanQuery.contains("sản phẩm") || cleanQuery.contains("danh sách") || cleanQuery.contains("gì"))) {
            products.stream()
                    .filter(p -> p.getStatus() == 1)
                    .limit(8)
                    .forEach(p -> scoredProducts.add(new java.util.AbstractMap.SimpleEntry<>(p, 1)));
        }

        // Extremely strict fallback: if they searched for something specific but we got no good matches, don't just dump random items
        if (scoredProducts.isEmpty() && keywords.size() > 0 && (cleanQuery.contains("iphone") || cleanQuery.contains("samsung"))) {
            products.stream()
                    .filter(p -> p.getStatus() == 1 && matchedBrand != null && p.getBrand() != null && p.getBrand().getId().equals(matchedBrand.getId()))
                    .limit(5)
                    .forEach(p -> scoredProducts.add(new java.util.AbstractMap.SimpleEntry<>(p, 1)));
        }

        // Sort: primarily by relevance score desc, then in-stock first, then price desc
        scoredProducts.sort((e1, e2) -> {
            int scoreDiff = Integer.compare(e2.getValue(), e1.getValue());
            if (scoreDiff != 0) return scoreDiff;
            int stock1 = e1.getKey().getStock() != null ? e1.getKey().getStock() : 0;
            int stock2 = e2.getKey().getStock() != null ? e2.getKey().getStock() : 0;
            if (stock1 > 0 && stock2 == 0) return -1;
            if (stock1 == 0 && stock2 > 0) return 1;
            return e2.getKey().getSalePrice().compareTo(e1.getKey().getSalePrice());
        });

        return scoredProducts.stream()
                .map(Map.Entry::getKey)
                .limit(8)
                .collect(Collectors.toList());
    }

    private Order parseAndSearchOrder(String query) {
        // Look for order code containing alphanumeric characters matching standard order code formats, e.g., "DH123456"
        Pattern codePattern = Pattern.compile("(dh\\d{3,10})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = codePattern.matcher(query);
        if (matcher.find()) {
            String code = matcher.group(1);
            return orderRepository.findByOrderCode(code).orElse(null);
        }
        return null;
    }

    private List<Promotion> getActivePromotionsList() {
        // Uses dedicated indexed query: status=1 AND end_date > NOW()
        return promotionRepository.findByStatusAndEndDateAfter(1, LocalDateTime.now());
    }

    private ProductResponse mapToProductResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .sku(p.getSku())
                .productName(p.getProductName())
                .slug(p.getSlug())
                .originalPrice(p.getOriginalPrice())
                .salePrice(p.getSalePrice())
                .stock(p.getStock())
                .thumbnail(p.getThumbnail())
                .shortDescription(p.getShortDescription())
                .categoryName(p.getCategory() != null ? p.getCategory().getCategoryName() : "")
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .brandName(p.getBrand() != null ? p.getBrand().getBrandName() : "")
                .brandId(p.getBrand() != null ? p.getBrand().getId() : null)
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }

    // --- LLM Connections ---

    private String callLLM(
            ChatbotConfig config, 
            String query, 
            String sessionId, 
            List<Product> matchedProducts, 
            Order matchedOrder, 
            List<Promotion> promotions
    ) throws Exception {
        // Construct the prompt context
        StringBuilder context = new StringBuilder();
        context.append("THÔNG TIN DỮ LIỆU ĐANG CÓ TRONG HỆ THỐNG CỬA HÀNG:\n\n");

        if (!matchedProducts.isEmpty()) {
            context.append("DANH SÁCH SẢN PHẨM KHỚP VỚI CÂU HỎI:\n");
            for (Product p : matchedProducts) {
                context.append("- ID: ").append(p.getId())
                        .append(" | ").append(p.getProductName())
                        .append(" | SKU: ").append(p.getSku())
                        .append(" | Giá niêm yết: ").append(p.getOriginalPrice()).append("đ")
                        .append(" | Giá bán: ").append(p.getSalePrice()).append("đ")
                        .append(" | Tồn kho: ").append(p.getStock())
                        .append(" | Phân loại: ").append(p.getCategory() != null ? p.getCategory().getCategoryName() : "Khác")
                        .append(" | Hãng: ").append(p.getBrand() != null ? p.getBrand().getBrandName() : "Khác")
                        .append("\n");
            }
            context.append("LƯU Ý QUAN TRỌNG: Khi gợi ý bất kỳ sản phẩm nào trên, hãy chèn thẻ định dạng `[PRODUCT:id]` (ví dụ: `[PRODUCT:1]`) vào cuối mô tả sản phẩm để hệ thống tự động vẽ thẻ card sản phẩm.\n\n");
        } else {
            context.append("Không tìm thấy sản phẩm nào khớp trực tiếp trong Database.\n\n");
        }

        if (matchedOrder != null) {
            context.append("KẾT QUẢ TRA CỨU ĐƠN HÀNG:\n")
                    .append("- Mã đơn hàng: ").append(matchedOrder.getOrderCode())
                    .append("\n- Người nhận: ").append(matchedOrder.getReceiverName())
                    .append("\n- Số điện thoại: ").append(matchedOrder.getReceiverPhone())
                    .append("\n- Trạng thái: ").append(matchedOrder.getOrderStatus())
                    .append("\n- Tổng cộng: ").append(matchedOrder.getTotalAmount()).append("đ\n\n");
        }

        if (!promotions.isEmpty()) {
            context.append("DANH SÁCH CHƯƠNG TRÌNH KHUYẾN MÃI HOẠT ĐỘNG:\n");
            for (Promotion pr : promotions) {
                context.append("- ").append(pr.getName())
                        .append(": Giảm ").append(pr.getDiscountValue()).append(" ")
                        .append(pr.getDiscountType())
                        .append(" (Hạn: ").append(pr.getEndDate()).append(")\n");
            }
            context.append("\n");
        }

        if (config.getFaqData() != null && !config.getFaqData().trim().isEmpty()) {
            context.append("CÂU HỎI THƯỜNG GẶP (FAQ) ĐỂ THAM KHẢO TƯ VẤN:\n")
                    .append(config.getFaqData()).append("\n\n");
        }

        context.append("CHÍNH SÀCH BÁN HÀNG & DỊCH VỤ CỦA CỬA HÀNG TECHNO (ĐỂ TRẢ LỜI CÁC CÂU HỎI KHÓ):\n")
               .append("- CHÍNH SÁCH ĐỔI TRẢ: Miễn phí đổi mới trong 30 ngày đầu tiên nếu sản phẩm phát sinh lỗi từ nhà sản xuất. Hoàn tiền gấp 10 lần nếu phát hiện hàng giả, hàng nhái.\n")
               .append("- CHÍNH SÁCH TRẢ GÓP: Hỗ trợ trả góp 0% lãi suất qua thẻ tín dụng (kỳ hạn 3-6-9-12 tháng) hoặc qua công ty tài chính (Home Credit, HD Saison) chỉ cần CCCD gắn chip, xét duyệt hồ sơ nhanh trong 15 phút.\n")
               .append("- CHÍNH SÁCH VẬN CHUYỂN: Giao hàng hỏa tốc 2 giờ tại nội thành Hà Nội và TP.HCM. Giao hàng nhanh toàn quốc từ 2 đến 4 ngày làm việc. Miễn phí vận chuyển cho mọi đơn hàng từ 2.000.000đ trở lên. Khách hàng được kiểm tra hàng trước khi thanh toán (đồng kiểm).\n")
               .append("- CHÍNH SÁCH BẢO HÀNH: Bảo hành chính hãng 12 tháng đến 24 tháng tùy dòng máy. Hỗ trợ tiếp nhận bảo hành tại tất cả hệ thống cửa hàng Techno hoặc tại trung tâm bảo hành chính hãng của hãng (Apple, Dell, Asus, HP, MSI, Samsung...).\n")
               .append("- LIÊN HỆ HỖ TRỢ: Hotline kỹ thuật: 1900.8198 (8h00 - 22h00 hàng ngày), Email: support@techno.vn, Địa chỉ showroom chính: 123 Đường Ba Tháng Hai, Quận 10, TP. Hồ Chí Minh.\n\n");

        // Get past messages in this session
        List<ChatbotMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<ChatbotMessage> recentHistory = history.stream()
                .skip(Math.max(0, history.size() - 8)) // Get last 8 messages
                .collect(Collectors.toList());

        if ("GEMINI".equalsIgnoreCase(config.getProvider())) {
            return callGemini(config.getApiKey(), config.getSystemPrompt(), context.toString(), recentHistory, query);
        } else {
            return callOpenAI(config.getApiKey(), config.getSystemPrompt(), context.toString(), recentHistory, query);
        }
    }

    private String callGemini(
            String apiKey, 
            String systemPrompt, 
            String context, 
            List<ChatbotMessage> history, 
            String currentQuery
    ) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build the prompt containing system prompt, context, conversation history, and user's query
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("System Prompt:\n").append(systemPrompt).append("\n\n");
        promptBuilder.append("Context Data:\n").append(context).append("\n\n");
        promptBuilder.append("Lịch sử chat gần đây:\n");
        for (ChatbotMessage m : history) {
            promptBuilder.append(m.getSender()).append(": ").append(m.getMessage()).append("\n");
        }
        promptBuilder.append("USER: ").append(currentQuery).append("\nAI:");

        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contentsNode = requestBody.putArray("contents");
        ObjectNode contentParts = contentsNode.addObject();
        ArrayNode partsNode = contentParts.putArray("parts");
        partsNode.addObject().put("text", promptBuilder.toString());

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        JsonNode responseNode = objectMapper.readTree(response.getBody());
        return responseNode.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText("Dạ, Techno rất vui được phục vụ anh/chị ạ.");
    }

    private String callOpenAI(
            String apiKey, 
            String systemPrompt, 
            String context, 
            List<ChatbotMessage> history, 
            String currentQuery
    ) throws Exception {
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "gpt-4o-mini");
        
        ArrayNode messages = requestBody.putArray("messages");
        
        // System message
        messages.addObject()
                .put("role", "system")
                .put("content", systemPrompt + "\n\nContext Data:\n" + context);

        // History
        for (ChatbotMessage m : history) {
            String role = "USER".equals(m.getSender()) ? "user" : "assistant";
            messages.addObject()
                    .put("role", role)
                    .put("content", m.getMessage());
        }

        // Current query
        messages.addObject()
                .put("role", "user")
                .put("content", currentQuery);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        JsonNode responseNode = objectMapper.readTree(response.getBody());
        return responseNode.path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText("Dạ, Techno rất vui được phục vụ anh/chị ạ.");
    }

    // --- Fallback & Rule-Based Responders ---

    private String generateFallbackResponse(
            String query, 
            List<Product> matchedProducts, 
            Order matchedOrder, 
            List<Promotion> promotions
    ) {
        String q = query.toLowerCase();

        // 1. Check Policy questions first
        if (q.contains("bảo hành") || q.contains("warranty")) {
            return "Dạ, Techno cam kết bảo hành chính hãng từ 12 đến 24 tháng cho toàn bộ sản phẩm bán ra. " +
                   "Anh/chị có thể mang sản phẩm qua showroom của Techno gần nhất hoặc gửi trực tiếp tới trung tâm bảo hành chính hãng của " +
                   "các thương hiệu lớn như Apple, Dell, Asus, HP, Samsung... để được hỗ trợ kiểm tra nhanh nhất ạ!";
        }
        if (q.contains("đổi trả") || q.contains("hoàn tiền") || q.contains("lỗi")) {
            return "Dạ, chính sách đổi trả của Techno vô cùng linh hoạt:\n\n" +
                   "* **Đổi mới miễn phí:** Trong vòng 30 ngày đầu sử dụng nếu sản phẩm có lỗi từ nhà sản xuất.\n" +
                   "* **Cam kết chính hãng:** Hoàn tiền gấp 10 lần giá trị sản phẩm nếu phát hiện hàng giả, hàng nhái.\n\n" +
                   "Anh/chị cứ yên tâm mua sắm và sử dụng dịch vụ tại cửa hàng nhé!";
        }
        if (q.contains("trả góp") || q.contains("installment")) {
            return "Dạ, Techno hỗ trợ 2 hình thức trả góp vô cùng thuận tiện:\n\n" +
                   "1. **Trả góp 0% lãi suất:** Qua thẻ tín dụng của hơn 25 ngân hàng liên kết, thủ tục nhanh gọn không cần giấy tờ.\n" +
                   "2. **Trả góp qua công ty tài chính (Home Credit, HD Saison):** Chỉ cần CCCD gắn chip, xét duyệt hồ sơ online nhanh chóng trong 15 phút.\n\n" +
                   "Anh/chị muốn đăng ký theo hình thức nào để em hướng dẫn chi tiết hơn ạ?";
        }
        if (q.contains("vận chuyển") || q.contains("giao hàng") || q.contains("ship")) {
            return "Dạ, chính sách giao hàng của Techno hỗ trợ giao hàng hỏa tốc trong **2 giờ** tại khu vực nội thành Hà Nội & TP. Hồ Chí Minh.\n\n" +
                   "Đối với khu vực ngoại thành và các tỉnh thành khác, thời gian giao hàng từ 2 - 4 ngày. Đặc biệt, mọi đơn hàng từ **2.000.000đ** trở lên sẽ được **miễn phí vận chuyển** hoàn toàn và anh/chị được quyền đồng kiểm trước khi nhận hàng ạ!";
        }
        if (q.contains("chính hãng") || q.contains("nguồn gốc") || q.contains("uy tín")) {
            return "Dạ, toàn bộ sản phẩm tại Techno đều là hàng chính hãng 100%, nguyên seal, đầy đủ hóa đơn chứng từ và VAT.\n" +
                   "Shop nói không với hàng xách tay không rõ nguồn gốc hay hàng dựng, hàng cũ, anh/chị hoàn toàn yên tâm tuyệt đối khi mua sắm tại cửa hàng ạ!";
        }

        if (matchedOrder != null) {
            return "Dạ, em tìm thấy đơn hàng **" + matchedOrder.getOrderCode() + "** của anh/chị rồi ạ!\n\n" +
                   "* **Người nhận:** " + matchedOrder.getReceiverName() + "\n" +
                   "* **Trạng thái giao hàng:** `" + matchedOrder.getOrderStatus() + "`\n" +
                   "* **Địa chỉ nhận:** " + matchedOrder.getShippingAddress() + "\n" +
                   "* **Tổng thanh toán:** " + formatCurrency(matchedOrder.getTotalAmount()) + "\n\n" +
                   "Đơn hàng đang được đơn vị vận chuyển xử lý, Techno sẽ sớm cập nhật hành trình cho anh/chị nhé!";
        }

        if (q.contains("đơn hàng") || q.contains("tra cứu") || q.contains("order")) {
            return "Dạ, để kiểm tra trạng thái đơn hàng, anh/chị vui lòng nhập đúng **Mã đơn hàng** (Ví dụ: `DH12345`) để em hỗ trợ kiểm tra trạng thái trực tiếp nhé!";
        }

        if (q.contains("khuyến mãi") || q.contains("giảm giá") || q.contains("sale")) {
            StringBuilder reply = new StringBuilder("Dạ, hiện tại Techno đang áp dụng các chương trình khuyến mãi vô cùng hấp dẫn:\n\n");
            if (!promotions.isEmpty()) {
                for (Promotion p : promotions) {
                    reply.append("* **").append(p.getName()).append(":** Giảm giá cực sâu lên tới **")
                            .append(p.getDiscountValue()).append(" ").append(p.getDiscountType())
                            .append("**.\n");
                }
            } else {
                reply.append("* **Flash Sale Hè Cực Nhiệt:** Giảm trực tiếp lên tới 20% cho các dòng Laptop và Điện thoại.\n")
                        .append("* Hỗ trợ trả góp 0% lãi suất, giao hàng nhanh trong 2h.\n");
            }
            reply.append("\nDưới đây là danh sách các sản phẩm đang có giá tốt nhất ạ:");
            
            // Append product marks
            for (Product p : matchedProducts) {
                reply.append(" [PRODUCT:").append(p.getId()).append("]");
            }
            return reply.toString();
        }

        if (!matchedProducts.isEmpty()) {
            StringBuilder reply = new StringBuilder("Dạ, Techno có một số sản phẩm rất phù hợp với yêu cầu của anh/chị đây ạ:\n\n");
            for (Product p : matchedProducts) {
                reply.append("* **").append(p.getProductName()).append("** - Giá bán: **")
                        .append(formatCurrency(p.getSalePrice())).append("** ")
                        .append(p.getStock() > 0 ? "(Còn hàng)" : "(Hết hàng)")
                        .append(" [PRODUCT:").append(p.getId()).append("]\n");
            }
            reply.append("\nAnh/chị có thể click trực tiếp vào sản phẩm để xem thông số chi tiết hoặc đặt mua trả góp nhé!");
            return reply.toString();
        }

        if (q.contains("chào") || q.contains("hello") || q.contains("hi")) {
            return "Xin chào! Em là AI tư vấn bán hàng của Techno. Em có thể tư vấn các dòng laptop gaming, điện thoại pin trâu, máy ảnh, kiểm tra đơn hàng, hoặc tìm sản phẩm theo khoảng giá. Anh/chị cần em hỗ trợ gì ạ?";
        }

        return "Dạ, Techno ghi nhận yêu cầu của anh/chị ạ. Hệ thống hiện có đa dạng các dòng Laptop (Dell, Asus, MacBook, MSI) và Điện thoại di động (iPhone, Samsung, Xiaomi) chất lượng cao.\n\n" +
               "Anh/chị có thể cho em xin rõ hơn nhu cầu (như khoảng giá, thương hiệu, hoặc mục đích sử dụng) để em tìm kiếm sản phẩm chính xác nhất cho anh/chị nhé!";
    }

    private List<String> buildDynamicSuggestions(String query, List<Product> matched, Order order) {
        String q = query.toLowerCase();
        if (order != null || q.contains("đơn hàng")) {
            return Arrays.asList("Cách hủy đơn hàng?", "Bao lâu nhận được hàng?", "Cách thanh toán qua MoMo");
        }
        if (q.contains("laptop") || q.contains("gaming") || q.contains("asus") || q.contains("msi")) {
            return Arrays.asList("Laptop gaming dưới 20 triệu", "So sánh ASUS và MSI", "Chính sách bảo hành laptop");
        }
        if (q.contains("điện thoại") || q.contains("iphone") || q.contains("samsung") || q.contains("pin")) {
            return Arrays.asList("Điện thoại pin trâu giá rẻ", "So sánh iPhone 15 và S24", "iPhone chụp ảnh đẹp nhất");
        }
        if (q.contains("khuyến mãi") || q.contains("giảm giá") || q.contains("sale")) {
            return Arrays.asList("Laptop đang giảm sâu nhất", "Điện thoại flash sale", "Xem Voucher giảm thêm");
        }
        return Arrays.asList("Laptop gaming dưới 20 triệu", "Điện thoại pin trâu", "Sản phẩm nào đang giảm giá?");
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0đ";
        return new IntlFormatCurrency().format(value);
    }

    private static class IntlFormatCurrency {
        public String format(BigDecimal val) {
            return new java.text.DecimalFormat("#,###đ").format(val);
        }
    }
}
