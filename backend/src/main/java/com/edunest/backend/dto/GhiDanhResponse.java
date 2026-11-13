package com.edunest.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

public class GhiDanhResponse {
    private UUID id;
    private UUID lopId;
    private UUID hocVienId;
    private String tenHocVien;
    private LocalDate ngaySinh;
    private String tenPhuHuynh;
    private String sdtZalo;
    private LocalDate ngayBatDau;
    private String trangThai;

    public GhiDanhResponse() {}

    public GhiDanhResponse(UUID id, UUID lopId, UUID hocVienId, String tenHocVien, LocalDate ngaySinh,
                           String tenPhuHuynh, String sdtZalo, LocalDate ngayBatDau, String trangThai) {
        this.id = id;
        this.lopId = lopId;
        this.hocVienId = hocVienId;
        this.tenHocVien = tenHocVien;
        this.ngaySinh = ngaySinh;
        this.tenPhuHuynh = tenPhuHuynh;
        this.sdtZalo = sdtZalo;
        this.ngayBatDau = ngayBatDau;
        this.trangThai = trangThai;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getLopId() { return lopId; }
    public void setLopId(UUID lopId) { this.lopId = lopId; }

    public UUID getHocVienId() { return hocVienId; }
    public void setHocVienId(UUID hocVienId) { this.hocVienId = hocVienId; }

    public String getTenHocVien() { return tenHocVien; }
    public void setTenHocVien(String tenHocVien) { this.tenHocVien = tenHocVien; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getTenPhuHuynh() { return tenPhuHuynh; }
    public void setTenPhuHuynh(String tenPhuHuynh) { this.tenPhuHuynh = tenPhuHuynh; }

    public String getSdtZalo() { return sdtZalo; }
    public void setSdtZalo(String sdtZalo) { this.sdtZalo = sdtZalo; }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}

