package com.edunest.backend.repository;

import com.edunest.backend.entity.BuoiHoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BuoiHocRepository extends JpaRepository<BuoiHoc, UUID> {
    List<BuoiHoc> findByLopIdOrderByNgayHocAscGioBatDauAsc(UUID lopId);
    long countByLopId(UUID lopId);
}

