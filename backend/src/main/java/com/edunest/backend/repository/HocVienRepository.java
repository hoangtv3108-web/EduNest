package com.edunest.backend.repository;

import com.edunest.backend.entity.HocVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HocVienRepository extends JpaRepository<HocVien, UUID> {
    List<HocVien> findByPhuHuynhId(UUID phuHuynhId);
}

