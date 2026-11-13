package com.edunest.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class LopResponse {
    private UUID id;
    private String tenLop;
    private BigDecimal hocPhiMoiBuoi;
    private String trangThai;
    private UUID giaoVienId;
    private long soBuoiHoc;
    private long soHocVien;

    public LopResponse() {}

    public LopResponse(UUID id, String tenLop, BigDecimal hocPhiMoiBuoi, String trangThai,
                       UUID giaoVienId, long soBuoiHoc, long soHocVien) {
        this.id = id;
        this.tenLop = tenLop;
        this.hocPhiMoiBuoi = hocPhiMoiBuoi;
        this.trangThai = trangThai;
        this.giaoVienId = giaoVienId;
        this.soBuoiHoc = soBuoiHoc;
        this.soHocVien = soHocVien;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenLop() { return tenLop; }
    public void setTenLop(String tenLop) { this.tenLop = tenLop; }

    public BigDecimal getHocPhiMoiBuoi() { return hocPhiMoiBuoi; }
    public void setHocPhiMoiBuoi(BigDecimal hocPhiMoiBuoi) { this.hocPhiMoiBuoi = hocPhiMoiBuoi; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public UUID getGiaoVienId() { return giaoVienId; }
    public void setGiaoVienId(UUID giaoVienId) { this.giaoVienId = giaoVienId; }

    public long getSoBuoiHoc() { return soBuoiHoc; }
    public void setSoBuoiHoc(long soBuoiHoc) { this.soBuoiHoc = soBuoiHoc; }

    public long getSoHocVien() { return soHocVien; }
    public void setSoHocVien(long soHocVien) { this.soHocVien = soHocVien; }
}

