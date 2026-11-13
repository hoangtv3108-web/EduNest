package com.edunest.backend.repository;

import com.edunest.backend.entity.PhuHuynh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhuHuynhRepository extends JpaRepository<PhuHuynh, UUID> {
    Optional<PhuHuynh> findBySdtZalo(String sdtZalo);
}

