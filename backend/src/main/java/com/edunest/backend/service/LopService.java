package com.edunest.backend.service;

import com.edunest.backend.dto.*;
import com.edunest.backend.entity.BuoiHoc;
import com.edunest.backend.entity.Lop;
import com.edunest.backend.repository.BuoiHocRepository;
import com.edunest.backend.repository.GhiDanhRepository;
import com.edunest.backend.repository.LopRepository;
import com.edunest.backend.tenant.TenantContext;
import com.edunest.backend.util.ThoiGianVN;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
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

    @Transactional
    public LopResponse taoLop(TaoLopRequest req) {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) throw new IllegalStateException("Thiếu tenant_id trong context");

        Lop lop = new Lop(tenantId, req.getTenLop().trim(), req.getHocPhiMoiBuoi());
        lop.setGiaoVienId(req.getGiaoVienId());
        Lop saved = lopRepository.save(lop);

        return new LopResponse(saved.getId(), saved.getTenLop(), saved.getHocPhiMoiBuoi(),
                saved.getTrangThai(), saved.getGiaoVienId(), 0, 0);
    }

    @Transactional
    public LopResponse capNhatLop(UUID id, CapNhatLopRequest req) {
        Lop lop = lopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp ID: " + id));

        lop.setTenLop(req.getTenLop().trim());
        lop.setHocPhiMoiBuoi(req.getHocPhiMoiBuoi());
        if (req.getTrangThai() != null) lop.setTrangThai(req.getTrangThai());
        lop.setGiaoVienId(req.getGiaoVienId());

        Lop saved = lopRepository.save(lop);
        long soBuoi = buoiHocRepository.countByLopId(id);
        long soHocVien = ghiDanhRepository.findByLopId(id).size();

        return new LopResponse(saved.getId(), saved.getTenLop(), saved.getHocPhiMoiBuoi(),
                saved.getTrangThai(), saved.getGiaoVienId(), soBuoi, soHocVien);
    }

    @Transactional
    public void xoaLop(UUID id) {
        if (!lopRepository.existsById(id)) throw new IllegalArgumentException("Không tìm thấy lớp ID: " + id);
        lopRepository.deleteById(id);
    }

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

    @Transactional(readOnly = true)
    public LopResponse layChiTietLop(UUID id) {
        Lop lop = lopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp ID: " + id));
        long soBuoi = buoiHocRepository.countByLopId(id);
        long soHocVien = ghiDanhRepository.findByLopId(id).size();
        return new LopResponse(lop.getId(), lop.getTenLop(), lop.getHocPhiMoiBuoi(),
                lop.getTrangThai(), lop.getGiaoVienId(), soBuoi, soHocVien);
    }

    /**
     * CHẾ ĐỘ 1: Tạo buổi học thủ công (Manual)
     * Có kiểm tra:
     * - Ngày học không rơi vào quá khứ (trừ khi choPhepNgayQuaKhu = true)
     * - Giờ kết thúc phải sau giờ bắt đầu
     * - Chống trùng buổi học trong cùng 1 lớp
     * - Kiểm tra xung đột lịch dạy của giáo viên phụ trách
     */
    @Transactional
    public BuoiHocResponse taoBuoiHocThuCong(TaoBuoiHocThuCongRequest req) {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) throw new IllegalStateException("Thiếu tenant_id trong context");

        Lop lop = lopRepository.findById(req.getLopId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học với ID: " + req.getLopId()));

        if ("KET_THUC".equalsIgnoreCase(lop.getTrangThai())) {
            throw new IllegalArgumentException("Lớp học đã kết thúc, không thể tạo thêm buổi học.");
        }

        // 1. Kiểm tra ngày quá khứ
        LocalDate homNay = ThoiGianVN.homNay();
        if (!req.isChoPhepNgayQuaKhu() && req.getNgayHoc().isBefore(homNay)) {
            throw new IllegalArgumentException("Ngày học (" + req.getNgayHoc() + ") không được ở quá khứ. Hãy bật cờ nhập bù dữ liệu nếu cần.");
        }

        // 2. Tính toán giờ kết thúc
        LocalTime gioKetThuc = req.getGioKetThuc();
        if (gioKetThuc == null) {
            int thoiLuong = req.getThoiLuongPhut() != null && req.getThoiLuongPhut() > 0 ? req.getThoiLuongPhut() : 90;
            gioKetThuc = req.getGioBatDau().plusMinutes(thoiLuong);
        }

        if (!gioKetThuc.isAfter(req.getGioBatDau())) {
            throw new IllegalArgumentException("Giờ kết thúc (" + gioKetThuc + ") phải sau giờ bắt đầu (" + req.getGioBatDau() + ").");
        }

        // 3. Kiểm tra trùng lịch trong cùng lớp
        List<BuoiHoc> trungTrongLop = buoiHocRepository.timBuoiHocTrungTrongLop(
                lop.getId(), req.getNgayHoc(), req.getGioBatDau(), gioKetThuc, null);
        if (!trungTrongLop.isEmpty()) {
            BuoiHoc b = trungTrongLop.get(0);
            throw new IllegalStateException("Lớp " + lop.getTenLop() + " đã có buổi học trùng giờ vào ngày " 
                    + req.getNgayHoc() + " (" + b.getGioBatDau() + " - " + b.getGioKetThuc() + ").");
        }

        // 4. Kiểm tra xung đột lịch dạy của giáo viên
        if (lop.getGiaoVienId() != null) {
            List<BuoiHoc> xungDotGv = buoiHocRepository.timBuoiHocXungDotGiaoVien(
                    lop.getGiaoVienId(), req.getNgayHoc(), req.getGioBatDau(), gioKetThuc, null);
            if (!xungDotGv.isEmpty()) {
                BuoiHoc b = xungDotGv.get(0);
                throw new IllegalStateException("Giáo viên phụ trách đã có lịch dạy trùng vào ngày " 
                        + req.getNgayHoc() + " (" + b.getGioBatDau() + " - " + b.getGioKetThuc() + ").");
            }
        }

        BuoiHoc buoiHoc = new BuoiHoc(tenantId, lop.getId(), req.getNgayHoc(), req.getGioBatDau(), gioKetThuc);
        BuoiHoc saved = buoiHocRepository.save(buoiHoc);

        return new BuoiHocResponse(saved.getId(), saved.getLopId(), saved.getNgayHoc(), saved.getGioBatDau(), saved.getGioKetThuc());
    }

    /**
     * CHẾ ĐỘ 2: Tự động sinh lịch các buổi học theo chu kỳ tuần (Schedule-based)
     * Hỗ trợ:
     * - Bỏ qua ngày quá khứ
     * - Phát hiện xung đột lịch trong lớp hoặc lịch giáo viên
     * - Cơ chế boQuaKhiTrungLich: nếu trùng thì bỏ qua ngày đó và sinh bù buổi kế tiếp
     * - Trả về đầy đủ danh sách sinh thành công và danh sách bị bỏ qua kèm lý do.
     */
    @Transactional
    public SinhBuoiHocKetQuaResponse sinhBuoiHoc(UUID lopId, SinhBuoiHocRequest req) {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) throw new IllegalStateException("Thiếu tenant_id trong context");

        Lop lop = lopRepository.findById(lopId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học với ID: " + lopId));

        if ("KET_THUC".equalsIgnoreCase(lop.getTrangThai())) {
            throw new IllegalArgumentException("Lớp học đã kết thúc, không thể sinh thêm buổi học.");
        }

        LocalDate homNay = ThoiGianVN.homNay();
        if (!req.isChoPhepNgayQuaKhu() && req.getNgayBatDau().isBefore(homNay)) {
            throw new IllegalArgumentException("Ngày bắt đầu (" + req.getNgayBatDau() + ") không được ở quá khứ. Hãy chọn ngày từ hôm nay trở đi.");
        }

        LocalTime gioKetThuc = req.getGioKetThuc();
        if (gioKetThuc == null) {
            int thoiLuong = req.getThoiLuongPhut() != null && req.getThoiLuongPhut() > 0 ? req.getThoiLuongPhut() : 90;
            gioKetThuc = req.getGioBatDau().plusMinutes(thoiLuong);
        }

        if (!gioKetThuc.isAfter(req.getGioBatDau())) {
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu.");
        }

        Set<Integer> targetDays = new HashSet<>(req.getThuTrongTuan());
        List<BuoiHoc> danhSachLuu = new ArrayList<>();
        List<BuoiHocBoQuaItem> danhSachBoQua = new ArrayList<>();

        LocalDate current = req.getNgayBatDau();
        int daSinh = 0;
        int maxLoop = 365; // Chặn lặp vô tận
        int loopCount = 0;

        while (daSinh < req.getSoBuoi() && loopCount < maxLoop) {
            loopCount++;
            int dayOfWeek = current.getDayOfWeek().getValue();

            if (targetDays.contains(dayOfWeek)) {
                // Kiểm tra trùng giờ trong cùng lớp
                List<BuoiHoc> trungTrongLop = buoiHocRepository.timBuoiHocTrungTrongLop(
                        lopId, current, req.getGioBatDau(), gioKetThuc, null);
                
                // Kiểm tra trùng giờ giáo viên
                List<BuoiHoc> xungDotGv = lop.getGiaoVienId() != null
                        ? buoiHocRepository.timBuoiHocXungDotGiaoVien(lop.getGiaoVienId(), current, req.getGioBatDau(), gioKetThuc, null)
                        : List.of();

                if (!trungTrongLop.isEmpty()) {
                    if (req.isBoQuaKhiTrungLich()) {
                        danhSachBoQua.add(new BuoiHocBoQuaItem(current, req.getGioBatDau(), gioKetThuc, "Trùng giờ với buổi học khác của lớp"));
                    } else {
                        throw new IllegalStateException("Trùng lịch trong lớp vào ngày " + current);
                    }
                } else if (!xungDotGv.isEmpty()) {
                    if (req.isBoQuaKhiTrungLich()) {
                        danhSachBoQua.add(new BuoiHocBoQuaItem(current, req.getGioBatDau(), gioKetThuc, "Giáo viên bận dạy lớp khác vào khung giờ này"));
                    } else {
                        throw new IllegalStateException("Giáo viên bị trùng lịch dạy vào ngày " + current);
                    }
                } else {
                    // Hợp lệ -> Thêm vào danh sách tạo
                    danhSachLuu.add(new BuoiHoc(tenantId, lopId, current, req.getGioBatDau(), gioKetThuc));
                    daSinh++;
                }
            }
            current = current.plusDays(1);
        }

        List<BuoiHoc> saved = buoiHocRepository.saveAll(danhSachLuu);
        List<BuoiHocResponse> resThanhCong = new ArrayList<>();
        for (BuoiHoc b : saved) {
            resThanhCong.add(new BuoiHocResponse(b.getId(), b.getLopId(), b.getNgayHoc(), b.getGioBatDau(), b.getGioKetThuc()));
        }

        return new SinhBuoiHocKetQuaResponse(req.getSoBuoi(), resThanhCong.size(), danhSachBoQua.size(), resThanhCong, danhSachBoQua);
    }

    @Transactional(readOnly = true)
    public List<BuoiHocResponse> layDanhSachBuoiHoc(UUID lopId) {
        List<BuoiHoc> list = buoiHocRepository.findByLopIdOrderByNgayHocAscGioBatDauAsc(lopId);
        List<BuoiHocResponse> response = new ArrayList<>();
        for (BuoiHoc bh : list) {
            response.add(new BuoiHocResponse(bh.getId(), bh.getLopId(), bh.getNgayHoc(), bh.getGioBatDau(), bh.getGioKetThuc()));
        }
        return response;
    }
}
