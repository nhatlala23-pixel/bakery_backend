package com.nguyenthongnhat.backend_tttn.repository;

import com.nguyenthongnhat.backend_tttn.entity.WebsiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WebsiteSettingRepository extends JpaRepository<WebsiteSetting, Long> {
    Optional<WebsiteSetting> findBySettingKey(String settingKey);
}
