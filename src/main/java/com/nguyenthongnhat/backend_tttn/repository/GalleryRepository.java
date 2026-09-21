package com.nguyenthongnhat.backend_tttn.repository;

import com.nguyenthongnhat.backend_tttn.entity.Gallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, Long> {
    List<Gallery> findByActiveTrueOrderByDisplayOrderAsc();
    List<Gallery> findByCategoryAndActiveTrueOrderByDisplayOrderAsc(String category);
}
