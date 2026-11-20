package com.edunest.backend.nhacno;

/** Kết quả một lần quét của một trung tâm. */
public record KetQuaQuetNhacNo(
        int soHoaDonDenHan,
        int daGui,
        int boQuaDaNhac,
        int boQuaKhongCoLienLac,
        int loi) {
}