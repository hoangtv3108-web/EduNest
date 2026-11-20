package com.edunest.backend.xuat;

import com.edunest.backend.audit.NhatKyResponse;
import com.edunest.backend.baocao.DoanhThuThangResponse;
import com.edunest.backend.baocao.DongHoaDonBaoCao;
import com.edunest.backend.util.ThoiGianVN;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Tạo file .xlsx bằng Apache POI (đã có trong pom từ giai đoạn 2). */
final class ExcelXuat {

    private static final int GIOI_HAN_KY_TU_O = 32_000; // Excel tối đa 32.767 ký tự mỗi ô

    private ExcelXuat() {
    }

    private record KieuDang(CellStyle tieuDeLon, CellStyle tieuDeCot, CellStyle chu, CellStyle so, CellStyle tien,
            CellStyle ngay, CellStyle phanTram, CellStyle chuDam, CellStyle tienDam) {
    }

    private static KieuDang kieu(Workbook wb) {
        DataFormat df = wb.createDataFormat();

        Font dam = wb.createFont();
        dam.setBold(true);
        Font trang = wb.createFont();
        trang.setBold(true);
        trang.setColor(IndexedColors.WHITE.getIndex());
        Font lon = wb.createFont();
        lon.setBold(true);
        lon.setFontHeightInPoints((short) 14);

        CellStyle tieuDeLon = wb.createCellStyle();
        tieuDeLon.setFont(lon);

        CellStyle tieuDeCot = vien(wb.createCellStyle());
        tieuDeCot.setFont(trang);
        tieuDeCot.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        tieuDeCot.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        tieuDeCot.setAlignment(HorizontalAlignment.CENTER);
        tieuDeCot.setVerticalAlignment(VerticalAlignment.CENTER);
        tieuDeCot.setWrapText(true);

        CellStyle chu = vien(wb.createCellStyle());
        CellStyle so = vien(wb.createCellStyle());
        so.setAlignment(HorizontalAlignment.CENTER);
        CellStyle tien = vien(wb.createCellStyle());
        tien.setDataFormat(df.getFormat("#,##0"));
        CellStyle ngay = vien(wb.createCellStyle());
        ngay.setDataFormat(df.getFormat("dd/mm/yyyy"));
        ngay.setAlignment(HorizontalAlignment.CENTER);
        CellStyle phanTram = vien(wb.createCellStyle());
        phanTram.setDataFormat(df.getFormat("0.0\"%\""));
        CellStyle chuDam = vien(wb.createCellStyle());
        chuDam.setFont(dam);
        chuDam.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        chuDam.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        CellStyle tienDam = vien(wb.createCellStyle());
        tienDam.setFont(dam);
        tienDam.setDataFormat(df.getFormat("#,##0"));
        tienDam.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        tienDam.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return new KieuDang(tieuDeLon, tieuDeCot, chu, so, tien, ngay, phanTram, chuDam, tienDam);
    }

    private static CellStyle vien(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private static Cell o(Row row, int cot, String giaTri, CellStyle kieu) {
        Cell c = row.createCell(cot);
        c.setCellValue(giaTri == null ? "" : cat(giaTri));
        c.setCellStyle(kieu);
        return c;
    }

    private static Cell o(Row row, int cot, double giaTri, CellStyle kieu) {
        Cell c = row.createCell(cot);
        c.setCellValue(giaTri);
        c.setCellStyle(kieu);
        return c;
    }

    private static Cell oNgay(Row row, int cot, LocalDate ngay, CellStyle kieu) {
        Cell c = row.createCell(cot);
        if (ngay != null)
            c.setCellValue(ngay);
        c.setCellStyle(kieu);
        return c;
    }

    private static String cat(String s) {
        return s.length() <= GIOI_HAN_KY_TU_O ? s : s.substring(0, GIOI_HAN_KY_TU_O) + "…[cắt bớt]";
    }

    private static void doRong(Sheet sheet, int... soKyTu) {
        for (int i = 0; i < soKyTu.length; i++) {
            sheet.setColumnWidth(i, soKyTu[i] * 256); // không dùng autoSize vì cần font AWT, hay lỗi trên server
        }
    }

    // ========================================================================
    // 3.4 Báo cáo doanh thu & học phí
    // ========================================================================

    static byte[] baoCaoHocPhi(String tenTrungTam, YearMonth thang, DoanhThuThangResponse tong,
            List<DongHoaDonBaoCao> dong, String xuatLuc) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            KieuDang k = kieu(wb);
            sheetTongHop(wb, k, tenTrungTam, thang, tong, xuatLuc);
            sheetChiTiet(wb, k, dong);
            wb.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Không tạo được file Excel", e);
        }
    }

    private static void sheetTongHop(Workbook wb, KieuDang k, String tenTrungTam, YearMonth thang,
            DoanhThuThangResponse t, String xuatLuc) {
        Sheet sh = wb.createSheet("Tổng hợp");
        int r = 0;
        Row tieuDe = sh.createRow(r++);
        o(tieuDe, 0,
                "BÁO CÁO DOANH THU & HỌC PHÍ THÁNG " + String.format("%02d/%d", thang.getMonthValue(), thang.getYear()),
                k.tieuDeLon());
        o(sh.createRow(r++), 0, tenTrungTam, k.chu());
        o(sh.createRow(r++), 0, "Xuất lúc: " + xuatLuc + " (hóa đơn gom theo tháng của hạn đóng)", k.chu());
        r++;

        Row th = sh.createRow(r++);
        o(th, 0, "Chỉ tiêu", k.tieuDeCot());
        o(th, 1, "Giá trị", k.tieuDeCot());

        r = dongTongHop(sh, k, r, "Tổng học phí phát sinh (đ)", t.tongHocPhiPhatSinh().doubleValue(), k.tien());
        r = dongTongHop(sh, k, r, "Tổng tiền đã thu (đ)", t.tongDaThu().doubleValue(), k.tien());
        r = dongTongHop(sh, k, r, "Tổng tiền còn nợ (đ)", t.tongConNo().doubleValue(), k.tien());
        r = dongTongHop(sh, k, r, "Tổng số hóa đơn", t.soHoaDon(), k.so());
        r = dongTongHop(sh, k, r, "Số hóa đơn đã thu", t.soHoaDonDaThu(), k.so());
        r = dongTongHop(sh, k, r, "Số hóa đơn còn nợ", t.soHoaDonConNo(), k.so());
        r = dongTongHop(sh, k, r, "Số hóa đơn quá hạn chưa thu", t.soHoaDonQuaHanChuaThu(), k.so());
        r = dongTongHop(sh, k, r, "Số hóa đơn đóng đúng hạn", t.soHoaDonDungHan(), k.so());
        r = dongTongHop(sh, k, r, "Tỉ lệ thu (theo giá trị)", t.tiLeThuPhanTram().doubleValue(), k.phanTram());
        dongTongHop(sh, k, r, "Tỉ lệ đóng đúng hạn", t.tiLeDungHanPhanTram().doubleValue(), k.phanTram());
        doRong(sh, 42, 22);
    }

    private static int dongTongHop(Sheet sh, KieuDang k, int r, String nhan, double giaTri, CellStyle kieuGiaTri) {
        Row row = sh.createRow(r);
        o(row, 0, nhan, k.chu());
        o(row, 1, giaTri, kieuGiaTri);
        return r + 1;
    }

    private static void sheetChiTiet(Workbook wb, KieuDang k, List<DongHoaDonBaoCao> ds) {
        Sheet sh = wb.createSheet("Chi tiết hóa đơn");
        String[] tieuDe = { "STT", "Học viên", "Lớp", "Phụ huynh", "Kỳ học phí", "Số buổi", "Tổng tiền (đ)",
                "Hạn đóng", "Trạng thái", "Ngày thanh toán", "Tình trạng hạn" };
        Row th = sh.createRow(0);
        th.setHeightInPoints(30);
        for (int i = 0; i < tieuDe.length; i++)
            o(th, i, tieuDe[i], k.tieuDeCot());

        LocalDate homNay = ThoiGianVN.homNay();
        BigDecimal tong = BigDecimal.ZERO;
        BigDecimal daThu = BigDecimal.ZERO;
        int r = 1;
        for (DongHoaDonBaoCao d : ds) {
            boolean daNop = "DA_THANH_TOAN".equals(d.trangThai());
            Row row = sh.createRow(r);
            o(row, 0, r, k.so());
            o(row, 1, d.tenHocVien(), k.chu());
            o(row, 2, d.tenLop(), k.chu());
            o(row, 3, d.tenPhuHuynh(), k.chu());
            o(row, 4, d.kyHocPhi(), k.chu());
            o(row, 5, d.soBuoi(), k.so());
            o(row, 6, d.soTien().doubleValue(), k.tien());
            oNgay(row, 7, d.hanDong(), k.ngay());
            o(row, 8, daNop ? "Đã đóng" : "Còn nợ", k.so());
            oNgay(row, 9, d.ngayThanhToan(), k.ngay());
            o(row, 10, tinhTrangHan(d, daNop, homNay), k.chu());
            tong = tong.add(d.soTien());
            if (daNop)
                daThu = daThu.add(d.soTien());
            r++;
        }
        int cuoiDuLieu = r - 1;

        // Ba dòng tổng cuối bảng (tính sẵn bằng số, không phụ thuộc công thức nên mở ở
        // phần mềm nào cũng đúng)
        r = dongTong(sh, k, r, "TỔNG PHÁT SINH", tong);
        r = dongTong(sh, k, r, "ĐÃ THU", daThu);
        dongTong(sh, k, r, "CÒN NỢ", tong.subtract(daThu));

        sh.createFreezePane(2, 1); // cố định hàng tiêu đề và 2 cột đầu (STT, Học viên)
        if (cuoiDuLieu >= 1) {
            sh.setAutoFilter(new CellRangeAddress(0, cuoiDuLieu, 0, tieuDe.length - 1));
        }
        doRong(sh, 6, 28, 24, 24, 16, 10, 16, 13, 13, 16, 24);
        sh.setFitToPage(true);
        sh.getPrintSetup().setLandscape(true);
        sh.getPrintSetup().setFitWidth((short) 1);
        sh.getPrintSetup().setFitHeight((short) 0);
    }

    private static int dongTong(Sheet sh, KieuDang k, int r, String nhan, BigDecimal giaTri) {
        Row row = sh.createRow(r);
        for (int c = 0; c < 11; c++)
            o(row, c, "", k.chuDam());
        o(row, 1, nhan, k.chuDam());
        o(row, 6, giaTri.doubleValue(), k.tienDam());
        return r + 1;
    }

    private static String tinhTrangHan(DongHoaDonBaoCao d, boolean daNop, LocalDate homNay) {
        if (daNop) {
            if (d.ngayThanhToan() == null)
                return "Không rõ ngày đóng";
            return d.ngayThanhToan().isAfter(d.hanDong()) ? "Đóng trễ hạn" : "Đóng đúng hạn";
        }
        return d.hanDong().isBefore(homNay) ? "Quá hạn chưa đóng" : "Chưa đến hạn";
    }

    // ========================================================================
    // 3.5 Nhật ký kiểm toán dạng Excel
    // ========================================================================

    static byte[] nhatKy(List<NhatKyResponse> ds, Map<String, String> thongTinXuat) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            KieuDang k = kieu(wb);

            Sheet sh = wb.createSheet("Nhật ký kiểm toán");
            Row th = sh.createRow(0);
            for (int i = 0; i < CotNhatKy.TIEU_DE.size(); i++)
                o(th, i, CotNhatKy.TIEU_DE.get(i), k.tieuDeCot());
            int r = 1;
            for (NhatKyResponse n : ds) {
                Row row = sh.createRow(r++);
                List<String> cacO = CotNhatKy.dong(n);
                for (int i = 0; i < cacO.size(); i++)
                    o(row, i, cacO.get(i), k.chu());
            }
            sh.createFreezePane(0, 1);
            if (!ds.isEmpty())
                sh.setAutoFilter(new CellRangeAddress(0, ds.size(), 0, CotNhatKy.TIEU_DE.size() - 1));
            doRong(sh, 38, 32, 38, 26, 16, 28, 16, 38, 60, 60);

            Sheet tt = wb.createSheet("Thông tin xuất");
            int i = 0;
            for (Map.Entry<String, String> e : thongTinXuat.entrySet()) {
                Row row = tt.createRow(i++);
                o(row, 0, e.getKey(), k.chuDam());
                o(row, 1, e.getValue(), k.chu());
            }
            doRong(tt, 26, 80);

            wb.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Không tạo được file Excel", e);
        }
    }
}