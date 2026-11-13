package com.edunest.backend.repository;

import com.edunest.backend.entity.GhiDanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GhiDanhRepository extends JpaRepository<GhiDanh, UUID> {
    List<GhiDanh> findByLopId(UUID lopId);
    boolean existsByLopIdAndHocVienId(UUID lopId, UUID hocVienId);
    Optional<GhiDanh> findByLopIdAndHocVienId(UUID lopId, UUID hocVienId);
}

