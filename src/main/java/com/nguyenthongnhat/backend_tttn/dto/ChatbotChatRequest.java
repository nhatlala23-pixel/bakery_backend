package com.nguyenthongnhat.backend_tttn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotChatRequest {
    private String message;
    private String sessionId;
}
