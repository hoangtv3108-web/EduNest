package com.edunest.backend.dto;

import java.util.UUID;

public class DiemDanhRowResponse {
    private UUID hocVienId;
    private String tenHocVien;
    private String trangThai; // CO_MAT, VANG, CO_PHEP, hoặc null
    private String ghiChu;

    public DiemDanhRowResponse() {}

    public DiemDanhRowResponse(UUID hocVienId, String tenHocVien, String trangThai, String ghiChu) {
        this.hocVienId = hocVienId;
        this.tenHocVien = tenHocVien;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public UUID getHocVienId() { return hocVienId; }
    public void setHocVienId(UUID hocVienId) { this.hocVienId = hocVienId; }

    public String getTenHocVien() { return tenHocVien; }
    public void setTenHocVien(String tenHocVien) { this.tenHocVien = tenHocVien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}

