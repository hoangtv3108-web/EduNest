package com.edunest.backend.service;

import com.edunest.backend.dto.GhiDanhRequest;
import com.edunest.backend.dto.GhiDanhResponse;
import com.edunest.backend.entity.GhiDanh;
import com.edunest.backend.entity.HocVien;
import com.edunest.backend.entity.Lop;
import com.edunest.backend.entity.PhuHuynh;
import com.edunest.backend.repository.GhiDanhRepository;
import com.edunest.backend.repository.HocVienRepository;
import com.edunest.backend.repository.HoaDonHocPhiRepository;
import com.edunest.backend.repository.LopRepository;
import com.edunest.backend.repository.PhuHuynhRepository;
import com.edunest.backend.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class GhiDanhService {

    private final GhiDanhRepository ghiDanhRepository;
    private final LopRepository lopRepository;
    private final HocVienRepository hocVienRepository;
    private final PhuHuynhRepository phuHuynhRepository;
    private final HoaDonHocPhiRepository hoaDonHocPhiRepository;

    public GhiDanhService(GhiDanhRepository ghiDanhRepository,
            LopRepository lopRepository,
            HocVienRepository hocVienRepository,
            PhuHuynhRepository phuHuynhRepository,
            HoaDonHocPhiRepository hoaDonHocPhiRepository) {
        this.ghiDanhRepository = ghiDanhRepository;
        this.lopRepository = lopRepository;
        this.hocVienRepository = hocVienRepository;
        this.phuHuynhRepository = phuHuynhRepository;
        this.hoaDonHocPhiRepository = hoaDonHocPhiRepository;
    }

    /**
     * Thêm một hoặc nhiều học viên vào lớp học.
     * RÀNG BUỘC NGHIỆP VỤ BẮT BUỘC:
     * 1. Lớp học phải đang ở trạng thái 'DANG_MO'.
     * 2. Học viên không được trùng trong cùng một lớp.
     * 3. CHẶN CHÉO LỚP: Nếu học viên đang có trạng thái 'DANG_HOC' ở bất kỳ lớp nào
     * khác,
     * Backend bắt buộc từ chối và ném lỗi Conflict (409) rõ ràng.
     */
    @Transactional
    public List<GhiDanhResponse> ghiDanh(GhiDanhRequest req) {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new IllegalStateException("Không xác định được tenant_id của phiên làm việc");
        }

        Lop lop = lopRepository.findById(req.getLopId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học với ID: " + req.getLopId()));

        if (!"DANG_MO".equalsIgnoreCase(lop.getTrangThai())) {
            throw new IllegalArgumentException(
                    "Lớp học '" + lop.getTenLop() + "' hiện không ở trạng thái mở để ghi danh.");
        }

        LocalDate ngayBatDau = req.getNgayBatDau() != null ? req.getNgayBatDau() : LocalDate.now();
        List<GhiDanh> toSave = new ArrayList<>();

        for (UUID hocVienId : req.getHocVienIds()) {
            HocVien hv = hocVienRepository.findById(hocVienId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy học viên với ID: " + hocVienId));

            // 1. Kiểm tra trùng trong cùng lớp
            boolean daCoTrongLop = ghiDanhRepository.existsByLopIdAndHocVienIdAndTrangThai(
                    req.getLopId(), hocVienId, "DANG_HOC");
            if (daCoTrongLop) {
                throw new IllegalStateException("Học viên '" + hv.getHoTen() + "' đã có tên trong danh sách lớp này.");
            }

            // 2. Chặn chéo lớp: Kiểm tra học viên có đang học lớp nào khác trong toàn trung
            // tâm không
            Optional<GhiDanh> dangHocLopKhac = ghiDanhRepository.findByHocVienIdAndTrangThai(hocVienId, "DANG_HOC");
            if (dangHocLopKhac.isPresent()) {
                GhiDanh gdCu = dangHocLopKhac.get();
                Lop lopCu = lopRepository.findById(gdCu.getLopId()).orElse(null);
                String tenLopCu = lopCu != null ? lopCu.getTenLop() : gdCu.getLopId().toString();

                throw new IllegalStateException("Học viên '" + hv.getHoTen()
                        + "' hiện đang theo học tại lớp '" + tenLopCu + "'. "
                        + "Theo quy định hệ thống, học viên chỉ được ghi danh ở 1 lớp hoạt động tại một thời điểm.");
            }

            toSave.add(new GhiDanh(tenantId, hocVienId, req.getLopId(), ngayBatDau));
        }

        if (!toSave.isEmpty()) {
            ghiDanhRepository.saveAll(toSave);
        }

        return layDanhSachGhiDanhTheoLop(req.getLopId());
    }

    /**
     * Lấy danh sách học viên đã ghi danh vào lớp kèm thông tin phụ huynh.
     */
    @Transactional(readOnly = true)
    public List<GhiDanhResponse> layDanhSachGhiDanhTheoLop(UUID lopId) {
        List<GhiDanh> list = ghiDanhRepository.findByLopId(lopId);
        List<GhiDanhResponse> res = new ArrayList<>();

        for (GhiDanh gd : list) {
            HocVien hv = hocVienRepository.findById(gd.getHocVienId()).orElse(null);
            PhuHuynh ph = (hv != null && hv.getPhuHuynhId() != null)
                    ? phuHuynhRepository.findById(hv.getPhuHuynhId()).orElse(null)
                    : null;

            res.add(new GhiDanhResponse(
                    gd.getId(),
                    gd.getLopId(),
                    gd.getHocVienId(),
                    hv != null ? hv.getHoTen() : "Không xác định",
                    hv != null ? hv.getNgaySinh() : null,
                    ph != null ? ph.getHoTen() : null,
                    ph != null ? ph.getSdtZalo() : null,
                    gd.getNgayBatDau(),
                    gd.getTrangThai()));
        }
        return res;
    }

    /**
     * Hủy/kết thúc ghi danh học viên.
     * Bảo toàn dữ liệu: Nếu học viên đã có hóa đơn phát sinh, chuyển trạng thái sang 'DA_NGHI'
     * để không làm gãy khóa ngoại lịch sử thanh toán hay điểm danh.
     */
    @Transactional
    public void huyGhiDanh(UUID id) {
        GhiDanh gd = ghiDanhRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi ghi danh với ID: " + id));

        // Kiểm tra xem đã có hóa đơn học phí gắn với ghi danh này chưa
        boolean daCoHoaDon = !hoaDonHocPhiRepository.findByGhiDanhId(id).isEmpty();

        if (daCoHoaDon) {
            // Bảo toàn dữ liệu kế toán: Đổi trạng thái sang DA_NGHI
            gd.setTrangThai("DA_NGHI");
            ghiDanhRepository.save(gd);
        } else {
            // Chưa có dữ liệu tài chính phát sinh: Cho phép xóa cứng
            ghiDanhRepository.deleteById(id);
        }
    }

    /**
     * Đổi trạng thái ghi danh (ví dụ: DANG_HOC, TAM_NGHI, DA_NGHI, HOAN_THANH).
     */
    @Transactional
    public void doiTrangThai(UUID id, String trangThaiMoi) {
        GhiDanh gd = ghiDanhRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi ghi danh với ID: " + id));

        List<String> hopLe = List.of("DANG_HOC", "TAM_NGHI", "DA_NGHI", "HOAN_THANH");
        if (!hopLe.contains(trangThaiMoi)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + trangThaiMoi);
        }

        // Nếu chuyển về DANG_HOC, cần kiểm tra xem học viên có đang học lớp nào khác không
        if ("DANG_HOC".equals(trangThaiMoi) && !"DANG_HOC".equals(gd.getTrangThai())) {
            Optional<GhiDanh> lopKhac = ghiDanhRepository.findByHocVienIdAndTrangThai(gd.getHocVienId(), "DANG_HOC");
            if (lopKhac.isPresent() && !lopKhac.get().getId().equals(gd.getId())) {
                throw new IllegalStateException("Học viên này đang theo học ở một lớp khác, không thể kích hoạt lại trạng thái ĐANG HỌC.");
            }
        }

        gd.setTrangThai(trangThaiMoi);
        ghiDanhRepository.save(gd);
    }
}
