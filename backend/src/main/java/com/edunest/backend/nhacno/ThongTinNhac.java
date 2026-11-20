package com.edunest.backend.nhacno;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/** Mọi thông tin cần để soạn một thông báo nhắc học phí cho một hóa đơn. */
public record ThongTinNhac(
        UUID hoaDonId,
        String tenTrungTam,
        String tenPhuHuynh,
        String email,
        String sdtZalo,
        String tenHocVien,
        String tenLop,
        String kyHocPhi,
        BigDecimal soTien,
        LocalDate hanDong) {

    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String soTienHienThi() {
        return NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(soTien) + " đ";
    }

    public String hanDongHienThi() {
        return hanDong.format(NGAY);
    }

    public String tieuDe() {
        return "[" + tenTrungTam + "] Nhắc học phí " + kyHocPhi + " của em " + tenHocVien;
    }

    public String noiDung() {
        String ten = (tenPhuHuynh == null || tenPhuHuynh.isBlank()) ? "Quý phụ huynh" : "Phụ huynh " + tenPhuHuynh;
        return "Kính gửi " + ten + ",\n\n"
                + tenTrungTam + " xin nhắc học phí " + kyHocPhi + " của em " + tenHocVien
                + " (lớp " + tenLop + "):\n"
                + "- Số tiền: " + soTienHienThi() + "\n"
                + "- Hạn đóng: " + hanDongHienThi() + "\n\n"
                + "Kính mong Quý phụ huynh thanh toán đúng hạn. Nếu đã thanh toán, xin bỏ qua tin nhắn này.\n\n"
                + "Trân trọng,\n" + tenTrungTam;
    }
}