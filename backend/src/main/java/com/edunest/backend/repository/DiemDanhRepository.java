package com.edunest.backend.repository;

import com.edunest.backend.entity.DiemDanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiemDanhRepository extends JpaRepository<DiemDanh, UUID> {
    List<DiemDanh> findByBuoiHocId(UUID buoiHocId);
    Optional<DiemDanh> findByBuoiHocIdAndHocVienId(UUID buoiHocId, UUID hocVienId);
}

