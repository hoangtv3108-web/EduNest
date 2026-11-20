package com.edunest.backend.baocao;

import java.math.BigDecimal;

/**
 * Báo cáo học phí của một tháng, gom theo hạn đóng (han_dong) của hóa đơn trong
 * tháng đó.
 * Tỉ lệ đóng đúng hạn = số hóa đơn đã thanh toán có ngày thanh toán ≤ hạn đóng
 * ÷ tổng số hóa đơn của tháng.
 */
public record DoanhThuThangResponse(
        String thang,
        long soHoaDon,
        BigDecimal tongHocPhiPhatSinh,
        BigDecimal tongDaThu,
        BigDecimal tongConNo,
        long soHoaDonDaThu,
        long soHoaDonConNo,
        long soHoaDonQuaHanChuaThu,
        long soHoaDonDungHan,
        BigDecimal tiLeThuPhanTram,
        BigDecimal tiLeDungHanPhanTram) {
}