package com.nguyenthongnhat.backend_tttn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotAnalyticsDTO {
    private long totalChats;
    private long totalSessions;
    private double conversionRate;
    private List<TopQuestion> topQuestions;
    private List<TopProduct> topSuggestedProducts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopQuestion {
        private String question;
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private Long productId;
        private String productName;
        private String slug;
        private String thumbnail;
        private long count;
    }
}
