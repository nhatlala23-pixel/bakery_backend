package com.nguyenthongnhat.backend_tttn.repository;

import com.nguyenthongnhat.backend_tttn.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByStatusAndEndDateAfter(Integer status, LocalDateTime now);
}
