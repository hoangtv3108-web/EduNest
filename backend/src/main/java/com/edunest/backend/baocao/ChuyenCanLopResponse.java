package com.edunest.backend.baocao;

import java.math.BigDecimal;
import java.util.UUID;

public record ChuyenCanLopResponse(
        UUID lopId,
        String tenLop,
        int soBuoiDay,
        int soBuoiDaDiemDanh,
        long tongLuotDiemDanh,
        long coMat,
        long vangCoPhep,
        long vangKhongPhep,
        BigDecimal tiLeCoMatPhanTram,
        BigDecimal tiLeVangCoPhepPhanTram,
        BigDecimal tiLeVangKhongPhepPhanTram) {
}