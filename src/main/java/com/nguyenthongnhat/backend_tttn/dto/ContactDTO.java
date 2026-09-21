package com.nguyenthongnhat.backend_tttn.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ContactDTO {
    private Long id;
    
    @NotBlank(message = "Họ tên không được để trống")
    private String name;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;
    
    @NotBlank(message = "Chủ đề không được để trống")
    private String subject;
    
    @NotBlank(message = "Nội dung không được để trống")
    private String message;
    
    private String status;
    private String adminReply;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
}
