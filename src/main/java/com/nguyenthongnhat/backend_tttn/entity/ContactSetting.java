package com.nguyenthongnhat.backend_tttn.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zalo_url", length = 255)
    private String zaloUrl;

    @Column(name = "facebook_url", length = 255)
    private String facebookUrl;

    @Column(name = "instagram_url", length = 255)
    private String instagramUrl;

    @Column(name = "tiktok_url", length = 255)
    private String tiktokUrl;

    @Column(length = 20)
    private String hotline;

    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(name = "google_maps", columnDefinition = "TEXT")
    private String googleMaps;

    @Column(name = "opening_hours", length = 150)
    private String openingHours;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    @PrePersist
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
