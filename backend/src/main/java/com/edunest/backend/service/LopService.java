package com.edunest.backend.service;

import com.edunest.backend.dto.*;
import com.edunest.backend.entity.BuoiHoc;
import com.edunest.backend.entity.Lop;
import com.edunest.backend.repository.BuoiHocRepository;
import com.edunest.backend.repository.GhiDanhRepository;
import com.edunest.backend.repository.LopRepository;
import com.edunest.backend.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class LopService {

    private final LopRepository lopRepository;
    private final BuoiHocRepository buoiHocRepository;
    private final GhiDanhRepository ghiDanhRepository;

    public LopService(LopRepository lopRepository,
                      BuoiHocRepository buoiHocRepository,
                      GhiDanhRepository ghiDanhRepository) {
        this.lopRepository = lopRepository;
        this.buoiHocRepository = buoiHocRepository;
        this.ghiDanhRepository = ghiDanhRepository;
    }

    /**
     * Tạo lớp học mới và tự động gán tenant_id từ TenantContext.
     */
    @Transactional
    public LopResponse taoLop(TaoLopRequest req) {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new IllegalStateException("Không xác định được tenant_id của phiên làm việc");
        }

        Lop lop = new Lop(tenantId, req.getTenLop().trim(), req.getHocPhiMoiBuoi());
        lop.setGiaoVienId(req.getGiaoVienId());
        Lop saved = lopRepository.save(lop);

        return new LopResponse(saved.getId(), saved.getTenLop(), saved.getHocPhiMoiBuoi(),
                saved.getTrangThai(), saved.getGiaoVienId(), 0, 0);
    }

    /**
     * Cập nhật thông tin lớp học.
     */
    @Transactional
    public LopResponse capNhatLop(UUID id, CapNhatLopRequest req) {
        Lop lop = lopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học với ID: " + id));

        lop.setTenLop(req.getTenLop().trim());
        lop.setHocPhiMoiBuoi(req.getHocPhiMoiBuoi());
        if (req.getTrangThai() != null) {
            lop.setTrangThai(req.getTrangThai());
        }
        lop.setGiaoVienId(req.getGiaoVienId());

        Lop saved = lopRepository.save(lop);
        long soBuoi = buoiHocRepository.countByLopId(id);
        long soHocVien = ghiDanhRepository.findByLopId(id).size();

        return new LopResponse(saved.getId(), saved.getTenLop(), saved.getHocPhiMoiBuoi(),
                saved.getTrangThai(), saved.getGiaoVienId(), soBuoi, soHocVien);
    }

    /**
     * Xóa lớp học theo ID.
     */
    @Transactional
    public void xoaLop(UUID id) {
        if (!lopRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy lớp học với ID: " + id);
        }
        lopRepository.deleteById(id);
    }

    /**
     * Lấy danh sách lớp học của tenant hiện tại kèm số buổi học và số học viên đã ghi danh.
     */
    @Transactional(readOnly = true)
    public List<LopResponse> layDanhSachLop() {
        List<Lop> lops = lopRepository.findAll();
        List<LopResponse> res = new ArrayList<>();

        for (Lop lop : lops) {
            long soBuoi = buoiHocRepository.countByLopId(lop.getId());
            long soHocVien = ghiDanhRepository.findByLopId(lop.getId()).size();
            res.add(new LopResponse(lop.getId(), lop.getTenLop(), lop.getHocPhiMoiBuoi(),
                    lop.getTrangThai(), lop.getGiaoVienId(), soBuoi, soHocVien));
        }
        return res;
    }

    /**
     * Lấy chi tiết một lớp học.
     */
    @Transactional(readOnly = true)
    public LopResponse layChiTietLop(UUID id) {
        Lop lop = lopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học với ID: " + id));
        long soBuoi = buoiHocRepository.countByLopId(id);
        long soHocVien = ghiDanhRepository.findByLopId(id).size();
        return new LopResponse(lop.getId(), lop.getTenLop(), lop.getHocPhiMoiBuoi(),
                lop.getTrangThai(), lop.getGiaoVienId(), soBuoi, soHocVien);
    }

    /**
     * Tự động sinh lịch các buổi học (buoi_hoc) dựa trên ngày bắt đầu, số buổi và các thứ trong tuần.
     */
    @Transactional
    public List<BuoiHocResponse> sinhBuoiHoc(UUID lopId, SinhBuoiHocRequest req) {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new IllegalStateException("Không xác định được tenant_id của phiên làm việc");
        }

        if (!lopRepository.existsById(lopId)) {
            throw new IllegalArgumentException("Lớp học không tồn tại với ID: " + lopId);
        }

        Set<Integer> targetDays = new HashSet<>(req.getThuTrongTuan());
        List<BuoiHoc> danhSachMoi = new ArrayList<>();

        LocalDate current = req.getNgayBatDau();
        int daSinh = 0;

        // Vòng lặp tìm các ngày khớp với thứ trong tuần (1 = Mon ... 7 = Sun) cho đến khi đủ số buổi
        while (daSinh < req.getSoBuoi()) {
            int dayOfWeek = current.getDayOfWeek().getValue();
            if (targetDays.contains(dayOfWeek)) {
                BuoiHoc buoiHoc = new BuoiHoc(tenantId, lopId, current, req.getGioBatDau());
                danhSachMoi.add(buoiHoc);
                daSinh++;
            }
            current = current.plusDays(1);
        }

        List<BuoiHoc> savedList = buoiHocRepository.saveAll(danhSachMoi);
        List<BuoiHocResponse> response = new ArrayList<>();
        for (BuoiHoc bh : savedList) {
            response.add(new BuoiHocResponse(bh.getId(), bh.getLopId(), bh.getNgayHoc(), bh.getGioBatDau()));
        }
        return response;
    }

    /**
     * Lấy danh sách buổi học của một lớp.
     */
    @Transactional(readOnly = true)
    public List<BuoiHocResponse> layDanhSachBuoiHoc(UUID lopId) {
        List<BuoiHoc> list = buoiHocRepository.findByLopIdOrderByNgayHocAscGioBatDauAsc(lopId);
        List<BuoiHocResponse> response = new ArrayList<>();
        for (BuoiHoc bh : list) {
            response.add(new BuoiHocResponse(bh.getId(), bh.getLopId(), bh.getNgayHoc(), bh.getGioBatDau()));
        }
        return response;
    }
}

