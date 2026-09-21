package com.nguyenthongnhat.backend_tttn.service;

public interface MailService {
    void sendHtmlMail(String to, String subject, String content);
    void sendOtpMail(String to, String otp);
}
