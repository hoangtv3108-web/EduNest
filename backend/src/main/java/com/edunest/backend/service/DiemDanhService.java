package com.edunest.backend.service;

import com.edunest.backend.dto.CapNhatDiemDanhRequest;
import com.edunest.backend.dto.DiemDanhChiTietRequest;
import com.edunest.backend.dto.DiemDanhRowResponse;
import com.edunest.backend.entity.BuoiHoc;
import com.edunest.backend.entity.DiemDanh;
import com.edunest.backend.entity.GhiDanh;
import com.edunest.backend.entity.HocVien;
import com.edunest.backend.repository.BuoiHocRepository;
import com.edunest.backend.repository.DiemDanhRepository;
import com.edunest.backend.repository.GhiDanhRepository;
import com.edunest.backend.repository.HocVienRepository;
import com.edunest.backend.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class DiemDanhService {

    private final DiemDanhRepository diemDanhRepository;
    private final BuoiHocRepository buoiHocRepository;
    private final GhiDanhRepository ghiDanhRepository;
    private final HocVienRepository hocVienRepository;

    public DiemDanhService(DiemDanhRepository diemDanhRepository,
                           BuoiHocRepository buoiHocRepository,
                           GhiDanhRepository ghiDanhRepository,
                           HocVienRepository hocVienRepository) {
        this.diemDanhRepository = diemDanhRepository;
        this.buoiHocRepository = buoiHocRepository;
        this.ghiDanhRepository = ghiDanhRepository;
        this.hocVienRepository = hocVienRepository;
    }

    /**
     * Lấy bảng điểm danh của một buổi học, bao gồm toàn bộ học viên đã ghi danh vào lớp.
     */
    @Transactional(readOnly = true)
    public List<DiemDanhRowResponse> layBangDiemDanh(UUID buoiHocId) {
        BuoiHoc buoiHoc = buoiHocRepository.findById(buoiHocId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy buổi học với ID: " + buoiHocId));

        // Lấy danh sách học viên ghi danh vào lớp của buổi học này
        List<GhiDanh> ghiDanhList = ghiDanhRepository.findByLopId(buoiHoc.getLopId());

        // Lấy các bản ghi điểm danh đã có của buổi học
        List<DiemDanh> diemDanhList = diemDanhRepository.findByBuoiHocId(buoiHocId);
        Map<UUID, DiemDanh> mapDiemDanh = new HashMap<>();
        for (DiemDanh dd : diemDanhList) {
            mapDiemDanh.put(dd.getHocVienId(), dd);
        }

        List<DiemDanhRowResponse> rows = new ArrayList<>();
        for (GhiDanh gd : ghiDanhList) {
            HocVien hv = hocVienRepository.findById(gd.getHocVienId()).orElse(null);
            String tenHocVien = hv != null ? hv.getHoTen() : "Không xác định";
            DiemDanh dd = mapDiemDanh.get(gd.getHocVienId());

            rows.add(new DiemDanhRowResponse(
                    gd.getHocVienId(),
                    tenHocVien,
                    dd != null ? dd.getTrangThai() : null, // CO_MAT, VANG, CO_PHEP hoặc null nếu chưa điểm danh
                    dd != null ? dd.getGhiChu() : null
            ));
        }
        return rows;
    }

    /**
     * Cập nhật danh sách điểm danh cho buổi học (UPSERT).
     * Gán tenant_id hiện tại từ TenantContext khi thêm mới.
     */
    @Transactional
    public List<DiemDanhRowResponse> capNhatDiemDanh(CapNhatDiemDanhRequest req) {
        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new IllegalStateException("Không xác định được tenant_id của phiên làm việc");
        }

        BuoiHoc buoiHoc = buoiHocRepository.findById(req.getBuoiHocId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy buổi học với ID: " + req.getBuoiHocId()));

        List<DiemDanh> toSave = new ArrayList<>();
        for (DiemDanhChiTietRequest item : req.getDanhSach()) {
            DiemDanh dd = diemDanhRepository.findByBuoiHocIdAndHocVienId(buoiHoc.getId(), item.getHocVienId())
                    .orElse(null);

            if (dd != null) {
                // Đã tồn tại -> Cập nhật
                dd.setTrangThai(item.getTrangThai());
                dd.setGhiChu(item.getGhiChu());
                dd.setThoiDiem(OffsetDateTime.now());
                toSave.add(dd);
            } else {
                // Chưa tồn tại -> Thêm mới với tenant_id
                DiemDanh moi = new DiemDanh(tenantId, buoiHoc.getId(), item.getHocVienId(),
                        item.getTrangThai(), item.getGhiChu());
                toSave.add(moi);
            }
        }

        diemDanhRepository.saveAll(toSave);
        return layBangDiemDanh(req.getBuoiHocId());
    }
}

