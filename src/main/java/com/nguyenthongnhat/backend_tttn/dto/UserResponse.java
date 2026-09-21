package com.nguyenthongnhat.backend_tttn.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String avatar;
    private String roleName; // Chuyển từ Role entity sang tên quyền
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
