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
public class ChatbotChatResponse {
    private String reply;
    private List<ProductResponse> suggestedProducts;
    private List<String> suggestions;
    private String action;
    private Long actionProductId;
}
