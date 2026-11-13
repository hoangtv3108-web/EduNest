package com.edunest.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class HoaDonHocPhiResponse {
    private UUID id;
    private UUID ghiDanhId;
    private String tenHocVien;
    private String tenLop;
    private String tenPhuHuynh;
    private String sdtZalo;
    private String kyHocPhi;
    private BigDecimal soTien;
    private int soBuoi;
    private LocalDate hanDong;
    private String trangThai;

    public HoaDonHocPhiResponse() {}

    public HoaDonHocPhiResponse(UUID id, UUID ghiDanhId, String tenHocVien, String tenLop,
                                String tenPhuHuynh, String sdtZalo, String kyHocPhi,
                                BigDecimal soTien, int soBuoi, LocalDate hanDong, String trangThai) {
        this.id = id;
        this.ghiDanhId = ghiDanhId;
        this.tenHocVien = tenHocVien;
        this.tenLop = tenLop;
        this.tenPhuHuynh = tenPhuHuynh;
        this.sdtZalo = sdtZalo;
        this.kyHocPhi = kyHocPhi;
        this.soTien = soTien;
        this.soBuoi = soBuoi;
        this.hanDong = hanDong;
        this.trangThai = trangThai;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getGhiDanhId() { return ghiDanhId; }
    public void setGhiDanhId(UUID ghiDanhId) { this.ghiDanhId = ghiDanhId; }

    public String getTenHocVien() { return tenHocVien; }
    public void setTenHocVien(String tenHocVien) { this.tenHocVien = tenHocVien; }

    public String getTenLop() { return tenLop; }
    public void setTenLop(String tenLop) { this.tenLop = tenLop; }

    public String getTenPhuHuynh() { return tenPhuHuynh; }
    public void setTenPhuHuynh(String tenPhuHuynh) { this.tenPhuHuynh = tenPhuHuynh; }

    public String getSdtZalo() { return sdtZalo; }
    public void setSdtZalo(String sdtZalo) { this.sdtZalo = sdtZalo; }

    public String getKyHocPhi() { return kyHocPhi; }
    public void setKyHocPhi(String kyHocPhi) { this.kyHocPhi = kyHocPhi; }

    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }

    public int getSoBuoi() { return soBuoi; }
    public void setSoBuoi(int soBuoi) { this.soBuoi = soBuoi; }

    public LocalDate getHanDong() { return hanDong; }
    public void setHanDong(LocalDate hanDong) { this.hanDong = hanDong; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}

