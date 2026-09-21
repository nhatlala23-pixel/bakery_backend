package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.dto.AuthResponse;
import com.nguyenthongnhat.backend_tttn.dto.ForgotPasswordRequest;
import com.nguyenthongnhat.backend_tttn.dto.LoginRequest;
import com.nguyenthongnhat.backend_tttn.dto.ResetPasswordRequest;
import com.nguyenthongnhat.backend_tttn.mapper.UserMapper;
import com.nguyenthongnhat.backend_tttn.repository.RoleRepository;
import com.nguyenthongnhat.backend_tttn.repository.UserRepository;
import com.nguyenthongnhat.backend_tttn.security.JwtUtils;
import com.nguyenthongnhat.backend_tttn.service.AuthService;
import com.nguyenthongnhat.backend_tttn.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nguyenthongnhat.backend_tttn.entity.User;
import com.nguyenthongnhat.backend_tttn.entity.Role;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    @Value("${tttn.google.clientId}")
    private String googleClientId;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.generateAccessToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication);

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    public AuthResponse googleLogin(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");

                Role userRole = roleRepository.findByRoleName("USER")
                        .orElseThrow(() -> new RuntimeException("Role USER không tồn tại trong DB!"));

                User user = userRepository.findByEmail(email).orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .fullName(name)
                            .avatar(pictureUrl)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Random password
                            .role(userRole)
                            .status(1)
                            .build();
                    return userRepository.save(newUser);
                });

                // Tự động cấp quyền và đăng nhập
                org.springframework.security.core.userdetails.UserDetails userDetails = 
                        org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                        .password(user.getPassword())
                        .authorities("ROLE_" + user.getRole().getRoleName())
                        .build();

                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                String accessToken = jwtUtils.generateAccessToken(authentication);
                String refreshToken = jwtUtils.generateRefreshToken(authentication);

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .user(userMapper.toResponse(user))
                        .build();
            } else {
                throw new RuntimeException("Google Token không hợp lệ!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xác thực Google: " + e.getMessage());
        }
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (jwtUtils.validateJwtToken(refreshToken)) {
            String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
            
            // Trong thực tế, bạn nên kiểm tra xem Refresh Token có tồn tại trong DB không
            // Nhưng hiện tại chúng ta làm đơn giản hóa bằng cách check validity của Token
            
            var user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String newAccessToken = jwtUtils.generateTokenFromUsername(username, 3600000); // 1h

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .user(userMapper.toResponse(user))
                    .build();
        }
        throw new RuntimeException("Refresh token không hợp lệ!");
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống!"));

        // Tạo mã OTP 6 chữ số
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        // Lưu OTP vào DB với thời hạn 5 phút
        user.setOtpCode(otpCode);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        // Gửi email chứa mã OTP
        String htmlBody = buildOtpEmailTemplate(user.getFullName(), otpCode);
        emailService.sendHtmlEmail(user.getEmail(), "🔐 Mã xác nhận đặt lại mật khẩu", htmlBody);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống!"));

        // Kiểm tra OTP có tồn tại
        if (user.getOtpCode() == null || user.getOtpExpiry() == null) {
            throw new RuntimeException("Bạn chưa yêu cầu đặt lại mật khẩu! Vui lòng gửi yêu cầu trước.");
        }

        // Kiểm tra OTP hết hạn
        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            user.setOtpCode(null);
            user.setOtpExpiry(null);
            userRepository.save(user);
            throw new RuntimeException("Mã OTP đã hết hạn! Vui lòng yêu cầu mã mới.");
        }

        // Kiểm tra OTP đúng
        if (!user.getOtpCode().equals(request.getOtpCode())) {
            throw new RuntimeException("Mã OTP không chính xác!");
        }

        // Đổi mật khẩu
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    private String buildOtpEmailTemplate(String fullName, String otpCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body style="margin:0;padding:0;background-color:#f0f4f8;font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f0f4f8;padding:40px 0;">
                    <tr>
                        <td align="center">
                            <table role="presentation" width="480" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;box-shadow:0 4px 24px rgba(0,0,0,0.08);overflow:hidden;">
                                <!-- Header -->
                                <tr>
                                    <td style="background:linear-gradient(135deg,#667eea 0%%,#764ba2 100%%);padding:32px 40px;text-align:center;">
                                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">🔐 Đặt lại mật khẩu</h1>
                                    </td>
                                </tr>
                                <!-- Body -->
                                <tr>
                                    <td style="padding:36px 40px;">
                                        <p style="margin:0 0 16px;color:#333;font-size:16px;">Xin chào <strong>%s</strong>,</p>
                                        <p style="margin:0 0 24px;color:#555;font-size:15px;line-height:1.6;">
                                            Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.
                                            Vui lòng sử dụng mã xác nhận bên dưới:
                                        </p>
                                        <!-- OTP Code -->
                                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                                            <tr>
                                                <td align="center" style="padding:20px 0;">
                                                    <div style="display:inline-block;background:linear-gradient(135deg,#667eea 0%%,#764ba2 100%%);border-radius:12px;padding:16px 40px;">
                                                        <span style="font-size:36px;font-weight:800;color:#ffffff;letter-spacing:10px;font-family:monospace;">%s</span>
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
                                        <p style="margin:24px 0 8px;color:#e74c3c;font-size:14px;font-weight:600;text-align:center;">
                                            ⏱ Mã có hiệu lực trong 5 phút
                                        </p>
                                        <hr style="border:none;border-top:1px solid #eee;margin:24px 0;">
                                        <p style="margin:0;color:#999;font-size:13px;line-height:1.5;">
                                            Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                                            Tài khoản của bạn vẫn an toàn.
                                        </p>
                                    </td>
                                </tr>
                                <!-- Footer -->
                                <tr>
                                    <td style="background:#f8f9fa;padding:20px 40px;text-align:center;">
                                        <p style="margin:0;color:#aaa;font-size:12px;">© 2026 TTTN Store. All rights reserved.</p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(fullName, otpCode);
    }
}
