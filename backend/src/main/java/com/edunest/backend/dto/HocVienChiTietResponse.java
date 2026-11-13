package com.edunest.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

public class HocVienChiTietResponse {
    private UUID id;
    private String hoTen;
    private LocalDate ngaySinh;
    private UUID phuHuynhId;
    private String tenPhuHuynh;
    private String sdtZalo;
    private String email;

    public HocVienChiTietResponse() {}

    public HocVienChiTietResponse(UUID id, String hoTen, LocalDate ngaySinh, UUID phuHuynhId,
                                  String tenPhuHuynh, String sdtZalo, String email) {
        this.id = id;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.phuHuynhId = phuHuynhId;
        this.tenPhuHuynh = tenPhuHuynh;
        this.sdtZalo = sdtZalo;
        this.email = email;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public UUID getPhuHuynhId() { return phuHuynhId; }
    public void setPhuHuynhId(UUID phuHuynhId) { this.phuHuynhId = phuHuynhId; }

    public String getTenPhuHuynh() { return tenPhuHuynh; }
    public void setTenPhuHuynh(String tenPhuHuynh) { this.tenPhuHuynh = tenPhuHuynh; }

    public String getSdtZalo() { return sdtZalo; }
    public void setSdtZalo(String sdtZalo) { this.sdtZalo = sdtZalo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

