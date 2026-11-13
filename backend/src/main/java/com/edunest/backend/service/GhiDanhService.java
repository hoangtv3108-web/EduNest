package com.edunest.backend.service;

import com.edunest.backend.dto.GhiDanhRequest;
import com.edunest.backend.dto.GhiDanhResponse;
import com.edunest.backend.entity.GhiDanh;
import com.edunest.backend.entity.HocVien;
import com.edunest.backend.entity.PhuHuynh;
import com.edunest.backend.repository.GhiDanhRepository;
import com.edunest.backend.repository.HocVienRepository;
import com.edunest.backend.repository.LopRepository;
import com.edunest.backend.repository.PhuHuynhRepository;
import com.edunest.backend.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class GhiDanhService {

    private final GhiDanhRepository ghiDanhRepository;
    private final LopRepository lopRepository;
    private final HocVienRepository hocVienRepository;
    private final PhuHuynhRepository phuHuynhRepository;

    public GhiDanhService(GhiDanhRepository ghiDanhRepository,
                          LopRepository lopRepository,
                          HocVienRepository hocVienRepository,
                          PhuHuynhRepository phuHuynhRepository) {
        this.ghiDanhRepository = ghiDanhRepository;
        this.lopRepository = lopRepository;
        this.hocVienRepository = hocVienRepository;
        this.phuHuynhRepository = phuHuynhRepository;
    }

    /**
     * Thêm một hoặc nhiều học viên vào lớp học.
     * Gán tenant_id hiện tại từ TenantContext và ngăn chặn ghi danh trùng lặp.
     */
    @Transactional
    public List<GhiDanhResponse> ghiDanh(GhiDanhRequest req) {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new IllegalStateException("Không xác định được tenant_id của phiên làm việc");
        }

        if (!lopRepository.existsById(req.getLopId())) {
            throw new IllegalArgumentException("Không tìm thấy lớp học với ID: " + req.getLopId());
        }

        List<GhiDanh> toSave = new ArrayList<>();
        for (UUID hocVienId : req.getHocVienIds()) {
            if (!hocVienRepository.existsById(hocVienId)) {
                continue; // bỏ qua nếu học viên không tồn tại trong tenant
            }
            if (ghiDanhRepository.existsByLopIdAndHocVienId(req.getLopId(), hocVienId)) {
                continue; // bỏ qua nếu đã ghi danh vào lớp rồi
            }
            toSave.add(new GhiDanh(tenantId, hocVienId, req.getLopId(), req.getNgayBatDau()));
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
                    gd.getTrangThai()
            ));
        }
        return res;
    }

    /**
     * Hủy ghi danh học viên khỏi lớp.
     */
    @Transactional
    public void huyGhiDanh(UUID id) {
        if (!ghiDanhRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy bản ghi ghi danh với ID: " + id);
        }
        ghiDanhRepository.deleteById(id);
    }
}

