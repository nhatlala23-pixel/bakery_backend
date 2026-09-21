package com.nguyenthongnhat.backend_tttn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileRequest {
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không quá 100 ký tự")
    private String fullName;

    @Size(max = 15, message = "Số điện thoại không quá 15 ký tự")
    private String phone;

    private String address;
    private String avatar;
}
