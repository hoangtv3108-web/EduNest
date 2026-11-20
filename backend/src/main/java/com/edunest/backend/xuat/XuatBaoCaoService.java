package com.edunest.backend.xuat;

import com.edunest.backend.audit.HanhDongKiemToan;
import com.edunest.backend.audit.NguoiThucHien;
import com.edunest.backend.audit.NhatKyKiemToanService;
import com.edunest.backend.audit.NhatKyResponse;
import com.edunest.backend.baocao.BaoCaoService;
import com.edunest.backend.baocao.DoanhThuThangResponse;
import com.edunest.backend.baocao.DongHoaDonBaoCao;
import com.edunest.backend.tenant.TenantContext;
import com.edunest.backend.util.ThoiGianVN;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Xuất báo cáo học phí (Excel) và nhật ký kiểm toán (CSV / JSON / Excel).
 *
 * Mỗi lần xuất đều được ghi lại vào chính nhật ký kiểm toán (ai xuất, định
 * dạng, bộ lọc, số bản ghi, SHA-256)
 * trong cùng transaction, phục vụ truy vết và chống chối bỏ. Mã SHA-256 cũng
 * được trả về ở header
 * "X-SHA256" để bên nhận đối chiếu toàn vẹn file.
 */
@Service
public class XuatBaoCaoService {

    public static final String EXCEL_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final DateTimeFormatter TEM_THOI_GIAN = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public enum DinhDang {
        CSV("csv", "text/csv; charset=UTF-8"),
        JSON("json", "application/json; charset=UTF-8"),
        XLSX("xlsx", EXCEL_TYPE);

        final String duoiFile;
        final String contentType;

        DinhDang(String duoiFile, String contentType) {
            this.duoiFile = duoiFile;
            this.contentType = contentType;
        }

        public static DinhDang tu(String s) {
            String v = s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
            return switch (v) {
                case "csv" -> CSV;
                case "json" -> JSON;
                case "xlsx", "excel" -> XLSX;
                default -> throw new IllegalArgumentException("Định dạng không hỗ trợ: dùng csv, json hoặc xlsx");
            };
        }
    }

    private final BaoCaoService baoCao;
    private final NhatKyKiemToanService nhatKy;
    private final ObjectMapper json;

    public XuatBaoCaoService(BaoCaoService baoCao, NhatKyKiemToanService nhatKy, ObjectMapper json) {
        this.baoCao = baoCao;
        this.nhatKy = nhatKy;
        this.json = json;
    }

    // ---------------------------------------------------------------- 3.4

    @Transactional
    public XuatFile xuatDoanhThuExcel(YearMonth thang) {
        DoanhThuThangResponse tong = baoCao.doanhThuThang(thang);
        List<DongHoaDonBaoCao> dong = baoCao.chiTietHoaDon(thang);
        String xuatLuc = OffsetDateTime.now(ThoiGianVN.MUI_GIO).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        byte[] bytes = ExcelXuat.baoCaoHocPhi(baoCao.tenTrungTam(), thang, tong, dong, xuatLuc);
        XuatFile file = XuatFile.tao(bytes, "bao-cao-hoc-phi-" + thang + ".xlsx", EXCEL_TYPE);

        Map<String, Object> ghi = new LinkedHashMap<>();
        ghi.put("loai", "DOANH_THU_HOC_PHI");
        ghi.put("thang", thang.toString());
        ghi.put("dinhDang", "XLSX");
        ghi.put("soDong", dong.size());
        ghi.put("sha256", file.sha256());
        nhatKy.ghi(HanhDongKiemToan.XUAT_BAO_CAO_HOC_PHI, HanhDongKiemToan.DOI_TUONG_BAO_CAO, null, null, ghi);
        return file;
    }

    // ---------------------------------------------------------------- 3.5

    @Transactional
    public XuatFile xuatNhatKy(DinhDang dinhDang, boolean themBom, String hanhDong, UUID nguoiThucHienId,
            UUID doiTuongId, LocalDate tuNgay, LocalDate denNgay) {
        List<NhatKyResponse> ds = nhatKy.layTatCaDeXuat(hanhDong, nguoiThucHienId, doiTuongId, tuNgay, denNgay);

        NguoiThucHien nguoi = NhatKyKiemToanService.nguoiHienTai();
        OffsetDateTime xuatLuc = OffsetDateTime.now(ThoiGianVN.MUI_GIO);
        Map<String, String> thongTin = new LinkedHashMap<>();
        thongTin.put("he_thong", "EduNest");
        thongTin.put("phien_ban_dinh_dang", "1.0");
        thongTin.put("trung_tam_id", String.valueOf(TenantContext.get()));
        thongTin.put("ten_trung_tam", baoCao.tenTrungTam());
        thongTin.put("xuat_luc", xuatLuc.toString());
        thongTin.put("nguoi_xuat_id", nguoi.id() == null ? "" : nguoi.id().toString());
        thongTin.put("nguoi_xuat_ten", nguoi.ten() == null ? "" : nguoi.ten());
        thongTin.put("bo_loc_hanh_dong", hanhDong == null ? "" : hanhDong);
        thongTin.put("bo_loc_nguoi_thuc_hien_id", nguoiThucHienId == null ? "" : nguoiThucHienId.toString());
        thongTin.put("bo_loc_doi_tuong_id", doiTuongId == null ? "" : doiTuongId.toString());
        thongTin.put("bo_loc_tu_ngay", tuNgay == null ? "" : tuNgay.toString());
        thongTin.put("bo_loc_den_ngay", denNgay == null ? "" : denNgay.toString());
        thongTin.put("so_ban_ghi", String.valueOf(ds.size()));
        thongTin.put("thu_tu_sap_xep", "thoi_gian tang dan");
        thongTin.put("dinh_dang_thoi_gian", "ISO-8601, mui gio +07:00");

        byte[] bytes = switch (dinhDang) {
            case CSV -> csv(ds, themBom);
            case JSON -> jsonBytes(ds, thongTin);
            case XLSX -> ExcelXuat.nhatKy(ds, thongTin);
        };
        String ten = "nhat-ky-kiem-toan-" + xuatLuc.format(TEM_THOI_GIAN) + "." + dinhDang.duoiFile;
        XuatFile file = XuatFile.tao(bytes, ten, dinhDang.contentType);

        Map<String, Object> ghi = new LinkedHashMap<>();
        ghi.put("dinhDang", dinhDang.name());
        ghi.put("soBanGhi", ds.size());
        ghi.put("boLocHanhDong", hanhDong);
        ghi.put("boLocTuNgay", tuNgay == null ? null : tuNgay.toString());
        ghi.put("boLocDenNgay", denNgay == null ? null : denNgay.toString());
        ghi.put("sha256", file.sha256());
        nhatKy.ghi(HanhDongKiemToan.XUAT_NHAT_KY_KIEM_TOAN, HanhDongKiemToan.DOI_TUONG_NHAT_KY, null, null, ghi);
        return file;
    }

    private static byte[] csv(List<NhatKyResponse> ds, boolean themBom) {
        StringBuilder sb = new StringBuilder();
        if (themBom)
            sb.append('\uFEFF'); // để Excel nhận đúng UTF-8 tiếng Việt khi bấm đúp mở file
        sb.append(CsvWriter.dong(CotNhatKy.TIEU_DE));
        for (NhatKyResponse n : ds)
            sb.append(CsvWriter.dong(CotNhatKy.dong(n)));
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] jsonBytes(List<NhatKyResponse> ds, Map<String, String> thongTin) {
        ObjectNode goc = json.createObjectNode();
        ObjectNode meta = goc.putObject("metadata");
        thongTin.forEach((khoa, giaTri) -> meta.put(khoa, giaTri));
        meta.put("so_ban_ghi", ds.size()); // ghi đè bản chuỗi bằng số nguyên cho dễ xử lý

        ArrayNode mang = goc.putArray("ban_ghi");
        for (NhatKyResponse n : ds) {
            ObjectNode o = mang.addObject();
            o.put("id", String.valueOf(n.id()));
            o.put("thoi_gian", CotNhatKy.thoiGian(n));
            o.put("nguoi_thuc_hien_id", n.nguoiThucHienId() == null ? null : n.nguoiThucHienId().toString());
            o.put("nguoi_thuc_hien_ten", n.nguoiThucHienTen());
            o.put("vai_tro", n.vaiTro());
            o.put("hanh_dong", n.hanhDong());
            o.put("doi_tuong_loai", n.doiTuongLoai());
            o.put("doi_tuong_id", n.doiTuongId() == null ? null : n.doiTuongId().toString());
            o.set("du_lieu_cu", n.duLieuCu() == null ? json.nullNode() : n.duLieuCu());
            o.set("du_lieu_moi", n.duLieuMoi() == null ? json.nullNode() : n.duLieuMoi());
        }
        try {
            return json.writerWithDefaultPrettyPrinter().writeValueAsBytes(goc);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Không tạo được JSON", e);
        }
    }
}