package com.nguyenthongnhat.backend_tttn.controller;

import com.nguyenthongnhat.backend_tttn.dto.PasswordChangeRequest;
import com.nguyenthongnhat.backend_tttn.dto.UserProfileRequest;
import com.nguyenthongnhat.backend_tttn.dto.UserRequest;
import com.nguyenthongnhat.backend_tttn.dto.UserResponse;
import com.nguyenthongnhat.backend_tttn.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Quản lý thông tin người dùng & Tài khoản")
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Lấy danh sách tất cả người dùng (Phân trang - Admin only)")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Lấy danh sách tất cả người dùng (Không phân trang - Admin only)")
    public ResponseEntity<List<UserResponse>> getAllUsersList() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin tạo tài khoản nhân viên/admin mới")
    public ResponseEntity<UserResponse> createAccount(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createAccountByAdmin(request));
    }

    @GetMapping("/{id}/profile")
    @Operation(summary = "Lấy thông tin hồ sơ người dùng")
    public ResponseEntity<UserResponse> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getProfile(id));
    }

    @PutMapping("/{id}/profile")
    @Operation(summary = "Cập nhật thông tin hồ sơ")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(id, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật thông tin người dùng (Admin only)")
    public ResponseEntity<UserResponse> updateUserByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody com.nguyenthongnhat.backend_tttn.dto.AdminUserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserByAdmin(id, request));
    }

    @PostMapping("/{id}/password/otp")
    @Operation(summary = "Yêu cầu mã OTP để đổi mật khẩu")
    public ResponseEntity<?> requestPasswordOTP(@PathVariable Long id) {
        try {
            userService.requestPasswordOTP(id);
            return ResponseEntity.ok("OTP đã được gửi tới email của bạn.");
        } catch (Exception e) {
            log.error("Error requesting OTP for user {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("message", "Lỗi gửi OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/password/change")
    @Operation(summary = "Xác nhận đổi mật khẩu với OTP")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordChangeRequest request) {
        try {
            userService.changePassword(id, request);
            return ResponseEntity.ok("Đổi mật khẩu thành công.");
        } catch (Exception e) {
            log.error("Error changing password for user {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
