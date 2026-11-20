package com.edunest.backend.baocao;

import java.math.BigDecimal;
import java.util.List;

/**
 * Báo cáo chuyên cần của một tháng. Quy ước theo trạng thái điểm danh hiện có:
 * CO_MAT = đi học, CO_PHEP = vắng có phép, VANG = vắng không phép.
 * Tỉ lệ tính trên tổng lượt đã điểm danh (không tính lượt chưa điểm danh).
 */
public record ChuyenCanThangResponse(
        String thang,
        int tongSoBuoiDay,
        int tongSoBuoiDaDiemDanh,
        long tongLuotDiemDanh,
        long coMat,
        long vangCoPhep,
        long vangKhongPhep,
        BigDecimal tiLeCoMatPhanTram,
        BigDecimal tiLeVangCoPhepPhanTram,
        BigDecimal tiLeVangKhongPhepPhanTram,
        List<ChuyenCanLopResponse> theoLop) {
}