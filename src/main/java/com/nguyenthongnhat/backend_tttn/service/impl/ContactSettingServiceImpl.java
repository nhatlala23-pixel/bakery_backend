package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.dto.ContactSettingDTO;
import com.nguyenthongnhat.backend_tttn.entity.ContactSetting;
import com.nguyenthongnhat.backend_tttn.repository.ContactSettingRepository;
import com.nguyenthongnhat.backend_tttn.service.ContactSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactSettingServiceImpl implements ContactSettingService {

    private final ContactSettingRepository contactSettingRepository;

    @Override
    public ContactSettingDTO getContactSetting() {
        ContactSetting setting = contactSettingRepository.findAll().stream().findFirst()
                .orElseGet(() -> contactSettingRepository.save(new ContactSetting()));
        return mapToDTO(setting);
    }

    @Override
    @Transactional
    public ContactSettingDTO updateContactSetting(ContactSettingDTO dto) {
        ContactSetting setting = contactSettingRepository.findAll().stream().findFirst()
                .orElseGet(ContactSetting::new);

        setting.setZaloUrl(dto.getZaloUrl());
        setting.setFacebookUrl(dto.getFacebookUrl());
        setting.setInstagramUrl(dto.getInstagramUrl());
        setting.setTiktokUrl(dto.getTiktokUrl());
        setting.setHotline(dto.getHotline());
        setting.setEmail(dto.getEmail());
        setting.setAddress(dto.getAddress());
        setting.setGoogleMaps(dto.getGoogleMaps());
        setting.setOpeningHours(dto.getOpeningHours());
        setting.setLogoUrl(dto.getLogoUrl());

        return mapToDTO(contactSettingRepository.save(setting));
    }

    private ContactSettingDTO mapToDTO(ContactSetting setting) {
        return ContactSettingDTO.builder()
                .zaloUrl(setting.getZaloUrl())
                .facebookUrl(setting.getFacebookUrl())
                .instagramUrl(setting.getInstagramUrl())
                .tiktokUrl(setting.getTiktokUrl())
                .hotline(setting.getHotline())
                .email(setting.getEmail())
                .address(setting.getAddress())
                .googleMaps(setting.getGoogleMaps())
                .openingHours(setting.getOpeningHours())
                .logoUrl(setting.getLogoUrl())
                .build();
    }
}
