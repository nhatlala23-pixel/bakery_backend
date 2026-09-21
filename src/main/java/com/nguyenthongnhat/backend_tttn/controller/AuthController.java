package com.nguyenthongnhat.backend_tttn.controller;

import com.nguyenthongnhat.backend_tttn.dto.AuthResponse;
import com.nguyenthongnhat.backend_tttn.dto.ForgotPasswordRequest;
import com.nguyenthongnhat.backend_tttn.dto.LoginRequest;
import com.nguyenthongnhat.backend_tttn.dto.ResetPasswordRequest;
import com.nguyenthongnhat.backend_tttn.dto.UserRequest;
import com.nguyenthongnhat.backend_tttn.dto.UserResponse;
import com.nguyenthongnhat.backend_tttn.service.AuthService;
import com.nguyenthongnhat.backend_tttn.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Đăng nhập & Quản lý Token")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập và nhận Access Token + Refresh Token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    @Operation(summary = "Đăng nhập bằng tài khoản Google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody com.nguyenthongnhat.backend_tttn.dto.GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request.getToken()));
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới Access Token bằng Refresh Token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Gửi mã OTP đặt lại mật khẩu qua email")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "Mã xác nhận đã được gửi đến email của bạn!"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu bằng mã OTP")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công!"));
    }
}

