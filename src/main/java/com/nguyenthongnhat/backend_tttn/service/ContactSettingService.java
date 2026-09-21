package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.ContactSettingDTO;

public interface ContactSettingService {
    ContactSettingDTO getContactSetting();
    ContactSettingDTO updateContactSetting(ContactSettingDTO dto);
}
