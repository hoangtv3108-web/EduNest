package com.edunest.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class CapNhatDiemDanhRequest {

    @NotNull(message = "Buổi học không được để trống")
    private UUID buoiHocId;

    @NotEmpty(message = "Danh sách điểm danh không được để trống")
    @Valid
    private List<DiemDanhChiTietRequest> danhSach;

    public CapNhatDiemDanhRequest() {}

    public CapNhatDiemDanhRequest(UUID buoiHocId, List<DiemDanhChiTietRequest> danhSach) {
        this.buoiHocId = buoiHocId;
        this.danhSach = danhSach;
    }

    public UUID getBuoiHocId() { return buoiHocId; }
    public void setBuoiHocId(UUID buoiHocId) { this.buoiHocId = buoiHocId; }

    public List<DiemDanhChiTietRequest> getDanhSach() { return danhSach; }
    public void setDanhSach(List<DiemDanhChiTietRequest> danhSach) { this.danhSach = danhSach; }
}

