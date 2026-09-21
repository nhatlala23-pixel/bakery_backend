package com.nguyenthongnhat.backend_tttn.controller;

import com.nguyenthongnhat.backend_tttn.dto.ChatbotChatRequest;
import com.nguyenthongnhat.backend_tttn.dto.ChatbotChatResponse;
import com.nguyenthongnhat.backend_tttn.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Tag(name = "Chatbot", description = "API chatbot tư vấn sản phẩm công nghệ")
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/chat")
    @Operation(summary = "Gửi tin nhắn chat với AI tư vấn")
    public ResponseEntity<ChatbotChatResponse> chat(@RequestBody ChatbotChatRequest request) {
        return ResponseEntity.ok(chatbotService.chat(request));
    }
}
