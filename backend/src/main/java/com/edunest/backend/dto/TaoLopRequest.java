package com.edunest.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class TaoLopRequest {

    @NotBlank(message = "Tên lớp không được để trống")
    private String tenLop;

    @NotNull(message = "Học phí mỗi buổi không được để trống")
    @DecimalMin(value = "0.0", message = "Học phí phải >= 0")
    private BigDecimal hocPhiMoiBuoi;

    private UUID giaoVienId;

    public TaoLopRequest() {}

    public TaoLopRequest(String tenLop, BigDecimal hocPhiMoiBuoi, UUID giaoVienId) {
        this.tenLop = tenLop;
        this.hocPhiMoiBuoi = hocPhiMoiBuoi;
        this.giaoVienId = giaoVienId;
    }

    public String getTenLop() { return tenLop; }
    public void setTenLop(String tenLop) { this.tenLop = tenLop; }

    public BigDecimal getHocPhiMoiBuoi() { return hocPhiMoiBuoi; }
    public void setHocPhiMoiBuoi(BigDecimal hocPhiMoiBuoi) { this.hocPhiMoiBuoi = hocPhiMoiBuoi; }

    public UUID getGiaoVienId() { return giaoVienId; }
    public void setGiaoVienId(UUID giaoVienId) { this.giaoVienId = giaoVienId; }
}

