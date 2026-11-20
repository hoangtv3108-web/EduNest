package com.edunest.backend.service;

import com.edunest.backend.audit.HanhDongKiemToan;
import com.edunest.backend.audit.NhatKyKiemToanService;
import com.edunest.backend.dto.HoaDonHocPhiResponse;
import com.edunest.backend.dto.TinhHocPhiRequest;
import com.edunest.backend.entity.*;
import com.edunest.backend.repository.*;
import com.edunest.backend.tenant.TenantContext;
import com.edunest.backend.util.ThoiGianVN;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class HocPhiService {

    private final HoaDonHocPhiRepository hoaDonHocPhiRepository;
    private final LopRepository lopRepository;
    private final BuoiHocRepository buoiHocRepository;
    private final GhiDanhRepository ghiDanhRepository;
    private final HocVienRepository hocVienRepository;
    private final PhuHuynhRepository phuHuynhRepository;
    private final NhatKyKiemToanService nhatKy;

    public static final Set<String> TRANG_THAI_HOP_LE = Set.of("CHUA_THANH_TOAN", "DA_THANH_TOAN");

    public HocPhiService(HoaDonHocPhiRepository hoaDonHocPhiRepository,
            LopRepository lopRepository,
            BuoiHocRepository buoiHocRepository,
            GhiDanhRepository ghiDanhRepository,
            HocVienRepository hocVienRepository,
            PhuHuynhRepository phuHuynhRepository,
            NhatKyKiemToanService nhatKy) {
        this.hoaDonHocPhiRepository = hoaDonHocPhiRepository;
        this.lopRepository = lopRepository;
        this.buoiHocRepository = buoiHocRepository;
        this.ghiDanhRepository = ghiDanhRepository;
        this.hocVienRepository = hocVienRepository;
        this.phuHuynhRepository = phuHuynhRepository;
        this.nhatKy = nhatKy;
    }

    /**
     * Kích hoạt tiến trình tính học phí tự động cho lớp học:
     * - Lấy danh sách ghi danh của lớp
     * - Đếm số buổi học của lớp
     * - Nhân số buổi học với học phí mỗi buổi
     * - Phát sinh và lưu hóa đơn vào bảng hoa_don_hoc_phi với tenant_id hiện tại
     */
    @Transactional
    public List<HoaDonHocPhiResponse> tinhHocPhiTuDong(TinhHocPhiRequest req) {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new IllegalStateException("Không xác định được tenant_id của phiên làm việc");
        }

        Lop lop = lopRepository.findById(req.getLopId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học với ID: " + req.getLopId()));

        long soBuoi = buoiHocRepository.countByLopId(lop.getId());
        BigDecimal tongTien = BigDecimal.valueOf(soBuoi).multiply(lop.getHocPhiMoiBuoi());

        List<GhiDanh> dsGhiDanh = ghiDanhRepository.findByLopId(lop.getId());
        List<HoaDonHocPhi> toSave = new ArrayList<>();
        // Hóa đơn đã tồn tại bị đổi số tiền/hạn đóng: cần ghi nhật ký (điểm đau P1: ai
        // sửa học phí?)
        Map<HoaDonHocPhi, Map<String, Object>> daSua = new LinkedHashMap<>();
        int soHoaDonMoi = 0;

        for (GhiDanh gd : dsGhiDanh) {
            HoaDonHocPhi hd = hoaDonHocPhiRepository
                    .findByGhiDanhIdAndKyHocPhi(gd.getId(), req.getKyHocPhi())
                    .orElse(null);

            if (hd != null) {
                // Đã có hóa đơn cho kỳ này -> cập nhật lại số buổi và số tiền
                Map<String, Object> truoc = chupHoaDon(hd);
                hd.setSoBuoi((int) soBuoi);
                hd.setSoTien(tongTien);
                hd.setHanDong(req.getHanDong());
                if (!truoc.equals(chupHoaDon(hd))) {
                    daSua.put(hd, truoc);
                }
                toSave.add(hd);
            } else {
                // Phát sinh mới gắn tenant_id hiện tại
                HoaDonHocPhi moi = new HoaDonHocPhi(
                        tenantId,
                        gd.getId(),
                        req.getKyHocPhi(),
                        tongTien,
                        (int) soBuoi,
                        req.getHanDong());
                toSave.add(moi);
                soHoaDonMoi++;
            }
        }

        hoaDonHocPhiRepository.saveAll(toSave);

        for (Map.Entry<HoaDonHocPhi, Map<String, Object>> e : daSua.entrySet()) {
            nhatKy.ghi(HanhDongKiemToan.SUA_HOA_DON_HOC_PHI, HanhDongKiemToan.DOI_TUONG_HOA_DON,
                    e.getKey().getId(), e.getValue(), chupHoaDon(e.getKey()));
        }
        if (soHoaDonMoi > 0) {
            Map<String, Object> moi = new LinkedHashMap<>();
            moi.put("kyHocPhi", req.getKyHocPhi());
            moi.put("soHoaDonMoi", soHoaDonMoi);
            moi.put("soTien", tongTien);
            moi.put("hanDong", req.getHanDong().toString());
            nhatKy.ghi(HanhDongKiemToan.PHAT_SINH_HOA_DON, HanhDongKiemToan.DOI_TUONG_LOP, lop.getId(), null, moi);
        }
        return layDanhSachHoaDon(req.getLopId(), req.getKyHocPhi());
    }

    /**
     * Lấy danh sách hóa đơn học phí kèm thông tin học viên, lớp, phụ huynh.
     */
    @Transactional(readOnly = true)
    public List<HoaDonHocPhiResponse> layDanhSachHoaDon(UUID lopId, String kyHocPhi) {
        List<HoaDonHocPhi> dsHoaDon = hoaDonHocPhiRepository.findAll();
        List<HoaDonHocPhiResponse> res = new ArrayList<>();

        for (HoaDonHocPhi hd : dsHoaDon) {
            if (kyHocPhi != null && !kyHocPhi.isBlank() && !hd.getKyHocPhi().equalsIgnoreCase(kyHocPhi)) {
                continue;
            }

            GhiDanh gd = ghiDanhRepository.findById(hd.getGhiDanhId()).orElse(null);
            if (gd == null)
                continue;
            if (lopId != null && !gd.getLopId().equals(lopId))
                continue;

            Lop lop = lopRepository.findById(gd.getLopId()).orElse(null);
            HocVien hv = hocVienRepository.findById(gd.getHocVienId()).orElse(null);
            PhuHuynh ph = (hv != null && hv.getPhuHuynhId() != null)
                    ? phuHuynhRepository.findById(hv.getPhuHuynhId()).orElse(null)
                    : null;

            res.add(new HoaDonHocPhiResponse(
                    hd.getId(),
                    hd.getGhiDanhId(),
                    hv != null ? hv.getHoTen() : "Không xác định",
                    lop != null ? lop.getTenLop() : "Không xác định",
                    ph != null ? ph.getHoTen() : null,
                    ph != null ? ph.getSdtZalo() : null,
                    hd.getKyHocPhi(),
                    hd.getSoTien(),
                    hd.getSoBuoi(),
                    hd.getHanDong(),
                    hd.getTrangThai()));
        }
        return res;
    }

    /**
     * Cập nhật trạng thái thanh toán của hóa đơn, ghi nhận ngày thanh toán thực tế
     * (phục vụ tỉ lệ đóng đúng hạn)
     * và ghi nhật ký kiểm toán trong cùng transaction. Đặt lại đúng trạng thái hiện
     * tại thì không làm gì.
     */
    @Transactional
    public void capNhatTrangThai(UUID id, String trangThai) {
        if (!TRANG_THAI_HOP_LE.contains(trangThai)) {
            throw new IllegalArgumentException("Trạng thái hóa đơn không hợp lệ: " + trangThai);
        }
        HoaDonHocPhi hd = hoaDonHocPhiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn với ID: " + id));
        if (trangThai.equals(hd.getTrangThai())) {
            return;
        }
        Map<String, Object> truoc = chupHoaDon(hd);

        boolean daNop = "DA_THANH_TOAN".equals(trangThai);
        hd.setTrangThai(trangThai);
        hd.setNgayThanhToan(daNop ? ThoiGianVN.homNay() : null);
        hoaDonHocPhiRepository.save(hd);

        nhatKy.ghi(daNop ? HanhDongKiemToan.XAC_NHAN_NOP_HOC_PHI : HanhDongKiemToan.HUY_XAC_NHAN_NOP_HOC_PHI,
                HanhDongKiemToan.DOI_TUONG_HOA_DON, hd.getId(), truoc, chupHoaDon(hd));
    }

    /**
     * Ảnh chụp các trường quan trọng của hóa đơn để lưu vào du_lieu_cu /
     * du_lieu_moi.
     */
    private static Map<String, Object> chupHoaDon(HoaDonHocPhi hd) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("kyHocPhi", hd.getKyHocPhi());
        m.put("soTien", hd.getSoTien() == null ? null : hd.getSoTien().setScale(2, java.math.RoundingMode.HALF_UP));
        m.put("soBuoi", hd.getSoBuoi());
        m.put("hanDong", hd.getHanDong() == null ? null : hd.getHanDong().toString());
        m.put("trangThai", hd.getTrangThai());
        m.put("ngayThanhToan", hd.getNgayThanhToan() == null ? null : hd.getNgayThanhToan().toString());
        return m;
    }
}