package com.edunest.backend.baocao;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Một dòng chi tiết hóa đơn trong báo cáo học phí xuất Excel. */
public record DongHoaDonBaoCao(
        String tenHocVien,
        String tenLop,
        String tenPhuHuynh,
        String kyHocPhi,
        int soBuoi,
        BigDecimal soTien,
        LocalDate hanDong,
        String trangThai,
        LocalDate ngayThanhToan) {
}