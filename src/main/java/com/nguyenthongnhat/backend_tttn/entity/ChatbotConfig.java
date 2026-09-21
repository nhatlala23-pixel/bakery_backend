package com.nguyenthongnhat.backend_tttn.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chatbot_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_key", length = 255)
    private String apiKey;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(name = "faq_data", columnDefinition = "TEXT")
    private String faqData;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status;
}
