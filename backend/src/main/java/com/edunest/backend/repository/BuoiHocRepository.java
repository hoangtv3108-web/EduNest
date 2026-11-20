package com.edunest.backend.repository;

import com.edunest.backend.entity.BuoiHoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BuoiHocRepository extends JpaRepository<BuoiHoc, UUID> {

    List<BuoiHoc> findByLopIdOrderByNgayHocAscGioBatDauAsc(UUID lopId);

    long countByLopId(UUID lopId);

    boolean existsByLopIdAndNgayHocAndGioBatDau(UUID lopId, LocalDate ngayHoc, LocalTime gioBatDau);

    /**
     * Tìm các buổi học trong cùng 1 lớp bị giao nhau về mặt thời gian:
     * start_1 < end_2 AND end_1 > start_2
     */
    @Query("""
        SELECT b FROM BuoiHoc b 
        WHERE b.lopId = :lopId 
          AND b.ngayHoc = :ngayHoc 
          AND (:excludeId IS NULL OR b.id <> :excludeId)
          AND (b.gioBatDau < :gioKetThuc AND b.gioKetThuc > :gioBatDau)
    """)
    List<BuoiHoc> timBuoiHocTrungTrongLop(
            @Param("lopId") UUID lopId,
            @Param("ngayHoc") LocalDate ngayHoc,
            @Param("gioBatDau") LocalTime gioBatDau,
            @Param("gioKetThuc") LocalTime gioKetThuc,
            @Param("excludeId") UUID excludeId
    );

    /**
     * Kiểm tra xung đột lịch dạy của giáo viên trên TẤT CẢ các lớp thuộc cùng tenant:
     * Giáo viên đã có lịch dạy ở lớp khác hoặc lớp này vào cùng ngày và khoảng giờ bị giao nhau.
     */
    @Query("""
        SELECT b FROM BuoiHoc b 
        JOIN Lop l ON b.lopId = l.id 
        WHERE l.giaoVienId = :giaoVienId 
          AND b.ngayHoc = :ngayHoc 
          AND (:excludeId IS NULL OR b.id <> :excludeId)
          AND (b.gioBatDau < :gioKetThuc AND b.gioKetThuc > :gioBatDau)
    """)
    List<BuoiHoc> timBuoiHocXungDotGiaoVien(
            @Param("giaoVienId") UUID giaoVienId,
            @Param("ngayHoc") LocalDate ngayHoc,
            @Param("gioBatDau") LocalTime gioBatDau,
            @Param("gioKetThuc") LocalTime gioKetThuc,
            @Param("excludeId") UUID excludeId
    );
}
