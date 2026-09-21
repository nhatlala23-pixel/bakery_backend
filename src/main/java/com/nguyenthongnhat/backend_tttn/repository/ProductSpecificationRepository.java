package com.nguyenthongnhat.backend_tttn.repository;

import com.nguyenthongnhat.backend_tttn.entity.ProductSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductSpecificationRepository extends JpaRepository<ProductSpecification, Long> {
    List<ProductSpecification> findByProductIdOrderBySortOrderAsc(Long productId);
}
