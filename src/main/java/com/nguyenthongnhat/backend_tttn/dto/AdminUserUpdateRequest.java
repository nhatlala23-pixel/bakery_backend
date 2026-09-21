package com.nguyenthongnhat.backend_tttn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserUpdateRequest {
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;
    private String phone;
    private String address;
    private String roleName;
    private Integer status;

    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    private String newPassword; // Optional - nếu null hoặc blank thì giữ nguyên mật khẩu cũ
}
