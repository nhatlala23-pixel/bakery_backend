package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.ChatbotAnalyticsDTO;
import com.nguyenthongnhat.backend_tttn.dto.ChatbotChatRequest;
import com.nguyenthongnhat.backend_tttn.dto.ChatbotChatResponse;
import com.nguyenthongnhat.backend_tttn.entity.ChatbotConfig;
import com.nguyenthongnhat.backend_tttn.entity.ChatbotMessage;

import java.util.List;

public interface ChatbotService {
    ChatbotChatResponse chat(ChatbotChatRequest request);
    ChatbotConfig getConfig();
    ChatbotConfig saveConfig(ChatbotConfig config);
    ChatbotAnalyticsDTO getAnalytics();
    List<ChatbotMessage> getHistory();
    void clearHistory();
}
