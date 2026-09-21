package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.UserRequest;
import com.nguyenthongnhat.backend_tttn.dto.UserResponse;

// import com.nguyenthongnhat.backend_tttn.dto.UserResponse;
import java.util.List;

public interface UserService {
    UserResponse register(UserRequest request);
    UserResponse createAccountByAdmin(UserRequest request);
    UserResponse getProfile(Long id);
    UserResponse updateProfile(Long id, com.nguyenthongnhat.backend_tttn.dto.UserProfileRequest request);
    List<UserResponse> getAllUsers();
    org.springframework.data.domain.Page<UserResponse> getAllUsers(org.springframework.data.domain.Pageable pageable);
    UserResponse updateUserByAdmin(Long id, com.nguyenthongnhat.backend_tttn.dto.AdminUserUpdateRequest request);
    
    void requestPasswordOTP(Long id);
    void changePassword(Long id, com.nguyenthongnhat.backend_tttn.dto.PasswordChangeRequest request);
}
