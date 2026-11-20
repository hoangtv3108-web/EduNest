package com.edunest.backend.nhacno;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LichSuNhacNoResponse(
        UUID id,
        UUID hoaDonId,
        String tenHocVien,
        String tenLop,
        String kyHocPhi,
        LocalDate ngayNhac,
        KenhGui kenhGui,
        String trangThai,
        String nguoiNhan,
        String chiTietLoi,
        OffsetDateTime ngayTao) {
}