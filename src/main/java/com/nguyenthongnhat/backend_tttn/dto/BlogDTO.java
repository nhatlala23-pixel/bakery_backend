package com.nguyenthongnhat.backend_tttn.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogDTO {
    private Long id;
    private String title;
    private String slug;
    private String thumbnail;
    private String excerpt;
    private String content;
    private String authorName;
    private Long authorId;
    private String status; // DRAFT, PUBLISHED
    private LocalDateTime publishedDate;
    private String seoTitle;
    private String seoDescription;
    private LocalDateTime createdAt;
}
