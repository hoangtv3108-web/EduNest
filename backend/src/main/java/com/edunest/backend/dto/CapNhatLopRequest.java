package com.edunest.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public class CapNhatLopRequest {

    @NotBlank(message = "Tên lớp không được để trống")
    @Size(max = 200, message = "Tên lớp tối đa 200 ký tự")
    private String tenLop;

    @NotNull(message = "Học phí mỗi buổi không được để trống")
    @DecimalMin(value = "0.0", message = "Học phí phải >= 0")
    @DecimalMax(value = "10000000", message = "Học phí mỗi buổi tối đa 10.000.000")
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

