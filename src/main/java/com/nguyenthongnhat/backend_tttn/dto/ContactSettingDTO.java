package com.nguyenthongnhat.backend_tttn.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactSettingDTO {
    private String zaloUrl;
    private String facebookUrl;
    private String instagramUrl;
    private String tiktokUrl;
    private String hotline;
    private String email;
    private String address;
    private String googleMaps;
    private String openingHours;
    private String logoUrl;
}
