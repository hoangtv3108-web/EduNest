package com.edunest.backend.repository;

import com.edunest.backend.entity.HocVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HocVienRepository extends JpaRepository<HocVien, UUID> {

    List<HocVien> findByPhuHuynhId(UUID phuHuynhId);

    /**
     * Lấy danh sách các học viên KHẢ DỤNG để ghi danh:
     * Là những học viên CHƯA có bất kỳ bản ghi ghi danh nào ở trạng thái 'DANG_HOC'
     * trong toàn trung tâm (nhờ RLS tự lọc theo tenant_id).
     */
    @Query("""
        SELECT h FROM HocVien h 
        WHERE h.id NOT IN (
            SELECT g.hocVienId FROM GhiDanh g WHERE g.trangThai = 'DANG_HOC'
        )
        ORDER BY h.hoTen ASC
    """)
    List<HocVien> timHocVienKhaDungGhiDanh();
}
