package com.edunest.backend.service;

import com.edunest.backend.dto.HoaDonHocPhiResponse;
import com.edunest.backend.dto.TinhHocPhiRequest;
import com.edunest.backend.entity.*;
import com.edunest.backend.repository.*;
import com.edunest.backend.tenant.TenantContext;
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

    public HocPhiService(HoaDonHocPhiRepository hoaDonHocPhiRepository,
                          LopRepository lopRepository,
                          BuoiHocRepository buoiHocRepository,
                          GhiDanhRepository ghiDanhRepository,
                          HocVienRepository hocVienRepository,
                          PhuHuynhRepository phuHuynhRepository) {
        this.hoaDonHocPhiRepository = hoaDonHocPhiRepository;
        this.lopRepository = lopRepository;
        this.buoiHocRepository = buoiHocRepository;
        this.ghiDanhRepository = ghiDanhRepository;
        this.hocVienRepository = hocVienRepository;
        this.phuHuynhRepository = phuHuynhRepository;
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

        for (GhiDanh gd : dsGhiDanh) {
            HoaDonHocPhi hd = hoaDonHocPhiRepository
                    .findByGhiDanhIdAndKyHocPhi(gd.getId(), req.getKyHocPhi())
                    .orElse(null);

            if (hd != null) {
                // Đã có hóa đơn cho kỳ này -> cập nhật lại số buổi và số tiền
                hd.setSoBuoi((int) soBuoi);
                hd.setSoTien(tongTien);
                hd.setHanDong(req.getHanDong());
                toSave.add(hd);
            } else {
                // Phát sinh mới gắn tenant_id hiện tại
                HoaDonHocPhi moi = new HoaDonHocPhi(
                        tenantId,
                        gd.getId(),
                        req.getKyHocPhi(),
                        tongTien,
                        (int) soBuoi,
                        req.getHanDong()
                );
                toSave.add(moi);
            }
        }

        hoaDonHocPhiRepository.saveAll(toSave);
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
            if (gd == null) continue;
            if (lopId != null && !gd.getLopId().equals(lopId)) continue;

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
                    hd.getTrangThai()
            ));
        }
        return res;
    }

    /**
     * Cập nhật trạng thái thanh toán của hóa đơn.
     */
    @Transactional
    public void capNhatTrangThai(UUID id, String trangThai) {
        HoaDonHocPhi hd = hoaDonHocPhiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn với ID: " + id));
        hd.setTrangThai(trangThai);
        hoaDonHocPhiRepository.save(hd);
    }
}

