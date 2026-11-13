package com.edunest.backend.repository;

import com.edunest.backend.entity.Lop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LopRepository extends JpaRepository<Lop, UUID> {
}

