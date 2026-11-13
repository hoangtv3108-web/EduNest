package com.edunest.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public class DiemDanhChiTietRequest {

    @NotNull(message = "Học viên không được để trống")
    private UUID hocVienId;

    @NotBlank(message = "Trạng thái điểm danh không được để trống")
    @Pattern(regexp = "CO_MAT|VANG|CO_PHEP", message = "Trạng thái phải là CO_MAT, VANG hoặc CO_PHEP")
    private String trangThai;

    private String ghiChu;

    public DiemDanhChiTietRequest() {}

    public DiemDanhChiTietRequest(UUID hocVienId, String trangThai, String ghiChu) {
        this.hocVienId = hocVienId;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public UUID getHocVienId() { return hocVienId; }
    public void setHocVienId(UUID hocVienId) { this.hocVienId = hocVienId; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}

