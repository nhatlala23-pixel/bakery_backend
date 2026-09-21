package com.nguyenthongnhat.backend_tttn.controller;

import com.nguyenthongnhat.backend_tttn.dto.ChatbotAnalyticsDTO;
import com.nguyenthongnhat.backend_tttn.entity.ChatbotConfig;
import com.nguyenthongnhat.backend_tttn.entity.ChatbotMessage;
import com.nguyenthongnhat.backend_tttn.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/chatbot")
@RequiredArgsConstructor
@Tag(name = "Admin Chatbot", description = "Quản trị cấu hình và thống kê Chatbot AI")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminChatbotController {

    private final ChatbotService chatbotService;

    @GetMapping("/config")
    @Operation(summary = "Lấy cấu hình Chatbot AI hiện tại")
    public ResponseEntity<ChatbotConfig> getConfig() {
        return ResponseEntity.ok(chatbotService.getConfig());
    }

    @PutMapping("/config")
    @Operation(summary = "Lưu hoặc cập nhật cấu hình Chatbot AI")
    public ResponseEntity<ChatbotConfig> saveConfig(@RequestBody ChatbotConfig config) {
        return ResponseEntity.ok(chatbotService.saveConfig(config));
    }

    @GetMapping("/analytics")
    @Operation(summary = "Xem số liệu thống kê hiệu suất Chatbot AI")
    public ResponseEntity<ChatbotAnalyticsDTO> getAnalytics() {
        return ResponseEntity.ok(chatbotService.getAnalytics());
    }

    @GetMapping("/history")
    @Operation(summary = "Xem lịch sử toàn bộ cuộc trò chuyện")
    public ResponseEntity<List<ChatbotMessage>> getHistory() {
        return ResponseEntity.ok(chatbotService.getHistory());
    }

    @DeleteMapping("/history")
    @Operation(summary = "Xóa lịch sử toàn bộ cuộc trò chuyện")
    public ResponseEntity<Void> clearHistory() {
        chatbotService.clearHistory();
        return ResponseEntity.noContent().build();
    }
}
