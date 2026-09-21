package com.nguyenthongnhat.backend_tttn.controller;

import com.nguyenthongnhat.backend_tttn.dto.ContactSettingDTO;
import com.nguyenthongnhat.backend_tttn.service.ContactSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact-settings")
@RequiredArgsConstructor
@Tag(name = "Contact Settings", description = "Quản lý thông tin liên hệ và cài đặt thương hiệu")
public class ContactSettingController {

    private final ContactSettingService contactSettingService;

    @GetMapping
    @Operation(summary = "Lấy thông tin liên hệ (Public)")
    public ResponseEntity<ContactSettingDTO> getContactSetting() {
        return ResponseEntity.ok(contactSettingService.getContactSetting());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật thông tin liên hệ (Admin only)")
    public ResponseEntity<ContactSettingDTO> updateContactSetting(@RequestBody ContactSettingDTO dto) {
        return ResponseEntity.ok(contactSettingService.updateContactSetting(dto));
    }
}
