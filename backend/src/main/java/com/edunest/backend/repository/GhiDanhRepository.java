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

    List<GhiDanh> findByLopIdAndTrangThai(UUID lopId, String trangThai);

    boolean existsByLopIdAndHocVienId(UUID lopId, UUID hocVienId);

    boolean existsByLopIdAndHocVienIdAndTrangThai(UUID lopId, UUID hocVienId, String trangThai);

    Optional<GhiDanh> findByLopIdAndHocVienId(UUID lopId, UUID hocVienId);

    /**
     * Tìm bản ghi ghi danh đang hoạt động (DANG_HOC) của học viên trên toàn trung tâm.
     * Dùng để thực thi luật: một học viên chỉ được học 1 lớp tại 1 thời điểm.
     */
    Optional<GhiDanh> findByHocVienIdAndTrangThai(UUID hocVienId, String trangThai);
}
