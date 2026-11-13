package com.edunest.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class CapNhatLopRequest {

    @NotBlank(message = "Tên lớp không được để trống")
    private String tenLop;

    @NotNull(message = "Học phí mỗi buổi không được để trống")
    @DecimalMin(value = "0.0", message = "Học phí phải >= 0")
    private BigDecimal hocPhiMoiBuoi;

    private String trangThai; // DANG_MO, KET_THUC
    private UUID giaoVienId;

    public CapNhatLopRequest() {}

    public String getTenLop() { return tenLop; }
    public void setTenLop(String tenLop) { this.tenLop = tenLop; }

    public BigDecimal getHocPhiMoiBuoi() { return hocPhiMoiBuoi; }
    public void setHocPhiMoiBuoi(BigDecimal hocPhiMoiBuoi) { this.hocPhiMoiBuoi = hocPhiMoiBuoi; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public UUID getGiaoVienId() { return giaoVienId; }
    public void setGiaoVienId(UUID giaoVienId) { this.giaoVienId = giaoVienId; }
}

