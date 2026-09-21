package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.AuthResponse;
import com.nguyenthongnhat.backend_tttn.dto.LoginRequest;
import com.nguyenthongnhat.backend_tttn.dto.ForgotPasswordRequest;
import com.nguyenthongnhat.backend_tttn.dto.ResetPasswordRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse googleLogin(String idTokenString);
    AuthResponse refreshToken(String refreshToken);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
