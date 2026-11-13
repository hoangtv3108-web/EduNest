package com.edunest.backend.repository;

import com.edunest.backend.entity.HoaDonHocPhi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HoaDonHocPhiRepository extends JpaRepository<HoaDonHocPhi, UUID> {
    List<HoaDonHocPhi> findByGhiDanhId(UUID ghiDanhId);
    Optional<HoaDonHocPhi> findByGhiDanhIdAndKyHocPhi(UUID ghiDanhId, String kyHocPhi);
}

