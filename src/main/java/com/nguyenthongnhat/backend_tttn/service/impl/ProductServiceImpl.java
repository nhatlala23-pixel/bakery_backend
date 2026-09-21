package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.dto.ProductDetailResponse;
import com.nguyenthongnhat.backend_tttn.dto.ProductRequest;
import com.nguyenthongnhat.backend_tttn.dto.ProductResponse;
import com.nguyenthongnhat.backend_tttn.entity.Brand;
import com.nguyenthongnhat.backend_tttn.entity.Category;
import com.nguyenthongnhat.backend_tttn.entity.Product;
import com.nguyenthongnhat.backend_tttn.entity.ProductImage;
import com.nguyenthongnhat.backend_tttn.entity.ProductVariant;
import com.nguyenthongnhat.backend_tttn.mapper.ProductMapper;
import com.nguyenthongnhat.backend_tttn.repository.BrandRepository;
import com.nguyenthongnhat.backend_tttn.repository.CategoryRepository;
import com.nguyenthongnhat.backend_tttn.repository.ProductRepository;
import com.nguyenthongnhat.backend_tttn.repository.InventoryLogRepository;
import com.nguyenthongnhat.backend_tttn.entity.InventoryLog;
import com.nguyenthongnhat.backend_tttn.enums.InventoryChangeType;
import com.nguyenthongnhat.backend_tttn.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.nguyenthongnhat.backend_tttn.dto.ProductVariantRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;
    private final InventoryLogRepository inventoryLogRepository;

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByStatus(1, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    public ProductDetailResponse getProductDetail(String slug) {
        return productRepository.findBySlugAndStatus(slug, 1)
                .map(productMapper::toDetailResponse)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
    }

    @Override
    public Page<ProductResponse> getByCategoryId(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndStatus(categoryId, 1, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    public Page<ProductResponse> getByBrandId(Long brandId, Pageable pageable) {
        return productRepository.findByBrandIdAndStatus(brandId, 1, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    public Page<ProductResponse> getByCategorySlug(String slug, Pageable pageable) {
        return productRepository.findByCategorySlugAndStatus(slug, 1, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    public Page<ProductResponse> getByBrandSlug(String slug, Pageable pageable) {
        return productRepository.findByBrandSlugAndStatus(slug, 1, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional
    public ProductDetailResponse createProduct(ProductRequest request) {
        log.info("[DEBUG_LOG] Received ProductRequest: {}", request);

        if (request.getOriginalPrice() == null) {
            log.error("[DEBUG_LOG] originalPrice is NULL!");
            throw new RuntimeException("[DEBUG_LOG] originalPrice is NULL! Request content: " + request.toString());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
        
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu!"));

        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setCategory(category);
        product.setBrand(brand);
        product.setDescription(request.getDescription());
        product.setShortDescription(request.getShortDescription());
        product.setThumbnail(request.getThumbnail());
        product.setStatus(request.getStatus());
        product.setSku(request.getSku());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setSalePrice(request.getSalePrice());
        product.setStock(request.getStock());
        product.setSlug(generateSlug(request.getProductName()));
        product.setIngredients(request.getIngredients());
        product.setSize(request.getSize());
        product.setPreservation(request.getPreservation());
        product.setIsBestseller(request.getIsBestseller() != null ? request.getIsBestseller() : false);
        product.setIsFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false);
        product.setRating(request.getRating() != null ? request.getRating() : java.math.BigDecimal.valueOf(5.0));

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            product.setVariants(new ArrayList<>());
            request.getVariants().forEach(vr -> {
                ProductVariant variant = new ProductVariant();
                variant.setSku(vr.getSku());
                variant.setVariantPrice(vr.getPrice());
                variant.setStock(vr.getStockQuantity());
                variant.setColorCode(vr.getColorCode());
                variant.setSize(vr.getSize());
                variant.setThumbnailUrl(vr.getThumbnailUrl());
                variant.setStatus(vr.getStatus() != null ? vr.getStatus() : 1);
                variant.setProduct(product);
                product.getVariants().add(variant);
            });
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            product.setImages(new ArrayList<>());
            request.getImages().forEach(imgReq -> {
                ProductImage image = new ProductImage();
                image.setImageUrl(imgReq.getImageUrl());
                image.setIsMain(imgReq.getIsMain());
                image.setSortOrder(imgReq.getSortOrder());
                image.setProduct(product);
                product.getImages().add(image);
            });
        }

        return productMapper.toDetailResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDetailResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
        
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu!"));

        product.setProductName(request.getProductName());
        product.setCategory(category);
        product.setBrand(brand);
        product.setDescription(request.getDescription());
        product.setShortDescription(request.getShortDescription());
        product.setThumbnail(request.getThumbnail());
        product.setStatus(request.getStatus());
        product.setSku(request.getSku());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setSalePrice(request.getSalePrice());
        product.setStock(request.getStock());
        product.setSlug(generateSlug(request.getProductName()));
        product.setIngredients(request.getIngredients());
        product.setSize(request.getSize());
        product.setPreservation(request.getPreservation());
        product.setIsBestseller(request.getIsBestseller() != null ? request.getIsBestseller() : false);
        product.setIsFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false);
        product.setRating(request.getRating() != null ? request.getRating() : java.math.BigDecimal.valueOf(5.0));

        // Cập nhật các phiên bản (Variants) - Sử dụng logic đồng bộ để tránh lỗi Duplicate SKU
        if (request.getVariants() != null) {
            List<ProductVariant> currentVariants = product.getVariants();
            List<ProductVariantRequest> newVariantRequests = request.getVariants();
            
            // Danh sách các SKU mới gửi lên (đã trim và chuyển sang lowercase để so sánh)
            List<String> newSkusLower = newVariantRequests.stream()
                    .map(vr -> vr.getSku() != null ? vr.getSku().trim().toLowerCase() : "")
                    .collect(Collectors.toList());

            // 1. Xóa các phiên bản không còn trong danh sách mới
            currentVariants.removeIf(v -> v.getSku() == null || !newSkusLower.contains(v.getSku().trim().toLowerCase()));

            // 2. Cập nhật hoặc thêm mới
            for (ProductVariantRequest vr : newVariantRequests) {
                String requestSku = vr.getSku() != null ? vr.getSku().trim() : "";
                
                ProductVariant variant = currentVariants.stream()
                        .filter(v -> v.getSku() != null && v.getSku().trim().equalsIgnoreCase(requestSku))
                        .findFirst()
                        .orElseGet(() -> {
                            ProductVariant newV = new ProductVariant();
                            newV.setProduct(product);
                            currentVariants.add(newV);
                            return newV;
                        });

                variant.setSku(requestSku);
                variant.setVariantPrice(vr.getPrice());
                variant.setStock(vr.getStockQuantity());
                variant.setColorCode(vr.getColorCode());
                variant.setSize(vr.getSize());
                variant.setThumbnailUrl(vr.getThumbnailUrl());
                variant.setStatus(vr.getStatus() != null ? vr.getStatus() : 1);
            }
        }

        // Cập nhật album ảnh phụ
        if (request.getImages() != null) {
            product.getImages().clear();
            request.getImages().forEach(imgReq -> {
                ProductImage image = new ProductImage();
                image.setImageUrl(imgReq.getImageUrl());
                image.setIsMain(imgReq.getIsMain());
                image.setSortOrder(imgReq.getSortOrder());
                image.setProduct(product);
                product.getImages().add(image);
            });
        }

        return productMapper.toDetailResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy sản phẩm để xóa!");
        }
        productRepository.deleteById(id);
    }

    public static class PriceParseResult {
        public String cleanedKeyword;
        public java.math.BigDecimal minPrice;
        public java.math.BigDecimal maxPrice;

        public PriceParseResult(String cleanedKeyword, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) {
            this.cleanedKeyword = cleanedKeyword;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
    }

    public static PriceParseResult parsePriceQuery(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new PriceParseResult("", null, null);
        }

        String cleaned = keyword;
        java.math.BigDecimal minPrice = null;
        java.math.BigDecimal maxPrice = null;

        // 1. Range Pattern: "từ 5tr đến 10tr", "5 - 10 triệu", "từ 5 triệu đến 10 triệu", "5.000.000 - 10.000.000"
        java.util.regex.Pattern rangePattern = java.util.regex.Pattern.compile(
                "(?:từ\\s+)?([0-9.,]+)\\s*(triệu|tr|trđ|k|nghìn|ngàn|đ|vnd|đồng)?\\s*(?:đến|-|tới|\\s+to\\s+)\\s*([0-9.,]+)\\s*(triệu|tr|trđ|k|nghìn|ngàn|đ|vnd|đồng)?",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher rangeMatcher = rangePattern.matcher(cleaned);
        StringBuffer sbRange = new StringBuffer();
        boolean foundRange = false;
        while (rangeMatcher.find()) {
            String val1Str = rangeMatcher.group(1);
            String unit1 = rangeMatcher.group(2);
            String val2Str = rangeMatcher.group(3);
            String unit2 = rangeMatcher.group(4);

            String checkUnit1 = unit1;
            if (checkUnit1 == null) {
                checkUnit1 = unit2;
            }
            java.math.BigDecimal val1 = parseSingleValue(val1Str, checkUnit1);
            java.math.BigDecimal val2 = parseSingleValue(val2Str, unit2);

            boolean val1IsLarge = val1 != null && val1.compareTo(new java.math.BigDecimal("10000")) >= 0;
            boolean val2IsLarge = val2 != null && val2.compareTo(new java.math.BigDecimal("10000")) >= 0;

            // Match if there is a unit OR prefix OR at least one value is a large number (>= 10000)
            if (unit1 != null || unit2 != null || rangeMatcher.group(0).toLowerCase().startsWith("từ") || val1IsLarge || val2IsLarge) {
                minPrice = val1;
                maxPrice = val2;

                rangeMatcher.appendReplacement(sbRange, "");
                foundRange = true;
                break;
            }
        }
        if (foundRange) {
            rangeMatcher.appendTail(sbRange);
            cleaned = sbRange.toString();
            return new PriceParseResult(cleanExtraSpaces(cleaned), minPrice, maxPrice);
        }

        // 2. Under Pattern: "dưới 10tr", "nhỏ hơn 10 triệu", "< 10tr"
        java.util.regex.Pattern underPattern = java.util.regex.Pattern.compile(
                "(dưới|nhỏ\\s+hơn|ít\\s+hơn|<|<=)\\s*([0-9.,]+)\\s*(triệu|tr|trđ|k|nghìn|ngàn|đ|vnd|đồng)?",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher underMatcher = underPattern.matcher(cleaned);
        StringBuffer sbUnder = new StringBuffer();
        if (underMatcher.find()) {
            String valStr = underMatcher.group(2);
            String unit = underMatcher.group(3);
            maxPrice = parseSingleValue(valStr, unit);

            underMatcher.appendReplacement(sbUnder, "");
            underMatcher.appendTail(sbUnder);
            cleaned = sbUnder.toString();
            return new PriceParseResult(cleanExtraSpaces(cleaned), null, maxPrice);
        }

        // 3. Above Pattern: "trên 15 triệu", "lớn hơn 15tr", "> 15tr", "hơn 15tr"
        java.util.regex.Pattern abovePattern = java.util.regex.Pattern.compile(
                "(trên|hơn|lớn\\s+hơn|>|>=)\\s*([0-9.,]+)\\s*(triệu|tr|trđ|k|nghìn|ngàn|đ|vnd|đồng)?",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher aboveMatcher = abovePattern.matcher(cleaned);
        StringBuffer sbAbove = new StringBuffer();
        if (aboveMatcher.find()) {
            String valStr = aboveMatcher.group(2);
            String unit = aboveMatcher.group(3);
            minPrice = parseSingleValue(valStr, unit);

            aboveMatcher.appendReplacement(sbAbove, "");
            aboveMatcher.appendTail(sbAbove);
            cleaned = sbAbove.toString();
            return new PriceParseResult(cleanExtraSpaces(cleaned), minPrice, null);
        }

        // 4. Approximate/Standalone Pattern: "tầm 15tr", "khoảng 15 triệu", "15tr", "10.000.000"
        java.util.regex.Pattern approxPattern = java.util.regex.Pattern.compile(
                "(tầm|khoảng|xấp\\s+xỉ|tầm\\s+khoảng)?\\s*([0-9.,]+)\\s*(triệu|tr|trđ|k|nghìn|ngàn|đ|vnd|đồng)?",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher approxMatcher = approxPattern.matcher(cleaned);
        StringBuffer sbApprox = new StringBuffer();
        boolean foundApprox = false;
        while (approxMatcher.find()) {
            String prefix = approxMatcher.group(1);
            String valStr = approxMatcher.group(2);
            String unit = approxMatcher.group(3);

            java.math.BigDecimal target = parseSingleValue(valStr, unit);
            if (target != null) {
                boolean hasPrefix = prefix != null && !prefix.trim().isEmpty();
                boolean hasUnit = unit != null && !unit.trim().isEmpty();
                boolean isLargeNumber = target.compareTo(new java.math.BigDecimal("10000")) >= 0;

                // Match if there's prefix OR unit OR target is a large number (>= 10000)
                if (hasPrefix || hasUnit || isLargeNumber) {
                    if (hasPrefix) {
                        // For "tầm X" or "khoảng X", return range [X * 0.85, X * 1.15]
                        minPrice = target.multiply(new java.math.BigDecimal("0.85"));
                        maxPrice = target.multiply(new java.math.BigDecimal("1.15"));
                    } else {
                        // If no prefix (e.g. "15tr" or "10.000.000"), treat as range [target * 0.8, target * 1.2]
                        minPrice = target.multiply(new java.math.BigDecimal("0.8"));
                        maxPrice = target.multiply(new java.math.BigDecimal("1.2"));
                    }
                    approxMatcher.appendReplacement(sbApprox, "");
                    foundApprox = true;
                    break;
                }
            }
        }
        if (foundApprox) {
            approxMatcher.appendTail(sbApprox);
            cleaned = sbApprox.toString();
            return new PriceParseResult(cleanExtraSpaces(cleaned), minPrice, maxPrice);
        }

        return new PriceParseResult(cleanExtraSpaces(cleaned), null, null);
    }

    private static java.math.BigDecimal parseSingleValue(String valStr, String unit) {
        if (valStr == null) return null;
        String cleanedNum = valStr.replace(".", "").replace(",", ".");
        try {
            double val = Double.parseDouble(cleanedNum);
            double multiplier = 1;
            if (unit != null && !unit.trim().isEmpty()) {
                unit = unit.trim().toLowerCase();
                if (unit.contains("triệu") || unit.equals("tr") || unit.equals("trđ")) {
                    multiplier = 1_000_000;
                } else if (unit.equals("k") || unit.contains("nghìn") || unit.contains("ngàn")) {
                    multiplier = 1_000;
                } else if (unit.equals("trăm")) {
                    multiplier = 100_000;
                } else if (unit.equals("đồng") || unit.equals("đ") || unit.equals("vnd")) {
                    multiplier = 1;
                }
            } else {
                if (val <= 100) {
                    multiplier = 1_000_000; // default to million if small number
                } else {
                    multiplier = 1;
                }
            }
            return java.math.BigDecimal.valueOf(val * multiplier);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String cleanExtraSpaces(String input) {
        if (input == null) return "";
        return input.replaceAll("\\s+", " ").trim();
    }

    @Override
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        PriceParseResult parseResult = parsePriceQuery(keyword);
        if (parseResult.minPrice != null || parseResult.maxPrice != null) {
            return searchProductsFiltered(
                    parseResult.cleanedKeyword,
                    -1L,
                    -1L,
                    parseResult.minPrice,
                    parseResult.maxPrice,
                    "default",
                    pageable.getPageNumber(),
                    pageable.getPageSize()
            );
        }
        return productRepository.searchProducts(keyword, 1, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    public Page<ProductResponse> searchProductsFiltered(
            String keyword,
            Long brandId,
            Long categoryId,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice,
            String sort,
            int page,
            int size) {

        PriceParseResult parseResult = parsePriceQuery(keyword);
        String finalKeyword = parseResult.cleanedKeyword;

        Long brandIdParam = (brandId != null) ? brandId : -1L;
        Long categoryIdParam = (categoryId != null) ? categoryId : -1L;

        java.math.BigDecimal min = minPrice;
        if (min == null) {
            min = (parseResult.minPrice != null) ? parseResult.minPrice : java.math.BigDecimal.ZERO;
        }
        java.math.BigDecimal max = maxPrice;
        if (max == null) {
            max = (parseResult.maxPrice != null) ? parseResult.maxPrice : new java.math.BigDecimal("999999999999");
        }

        if ("discount".equals(sort)) {
            org.springframework.data.domain.Pageable pageable =
                    org.springframework.data.domain.PageRequest.of(page, size);
            return productRepository.searchProductsByDiscount(finalKeyword, brandIdParam, categoryIdParam, min, max, pageable)
                    .map(productMapper::toResponse);
        }

        org.springframework.data.domain.Sort sortObj;
        switch (sort == null ? "" : sort) {
            case "newest"     -> sortObj = org.springframework.data.domain.Sort.by("createdAt").descending();
            case "price_asc"  -> sortObj = org.springframework.data.domain.Sort.by("salePrice").ascending();
            case "price_desc" -> sortObj = org.springframework.data.domain.Sort.by("salePrice").descending();
            default           -> sortObj = org.springframework.data.domain.Sort.by("id").descending();
        }

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size, sortObj);
        return productRepository.searchProductsFiltered(finalKeyword, brandIdParam, categoryIdParam, min, max, pageable)
                .map(productMapper::toResponse);
    }

    private String generateSlug(String input) {
        if (input == null || input.isEmpty()) return "";
        String nowhitespace = Pattern.compile("\\s+").matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    @Override
    @Transactional
    public ProductResponse updateStock(Long id, Integer stock) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        int stockBefore = product.getStock() != null ? product.getStock() : 0;
        product.setStock(stock);
        Product savedProduct = productRepository.save(product);

        int diff = stock - stockBefore;
        if (diff != 0) {
            InventoryChangeType type = diff > 0 ? InventoryChangeType.IMPORT : InventoryChangeType.EXPORT;
            inventoryLogRepository.save(InventoryLog.builder()
                    .product(savedProduct)
                    .changeType(type)
                    .quantityBefore(stockBefore)
                    .quantityChange(Math.abs(diff))
                    .quantityAfter(stock)
                    .note("Cập nhật tồn kho thủ công (Admin/Staff)")
                    .build());
        }

        return productMapper.toResponse(savedProduct);
    }
}
