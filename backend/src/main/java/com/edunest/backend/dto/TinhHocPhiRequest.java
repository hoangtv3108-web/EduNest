package com.edunest.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public class TinhHocPhiRequest {

    @NotNull(message = "Lớp học không được để trống")
    private UUID lopId;

    @NotBlank(message = "Kỳ học phí không được để trống (ví dụ: Tháng 11/2026)")
    @Size(max = 50, message = "Kỳ học phí tối đa 50 ký tự")
    private String kyHocPhi;

    @NotNull(message = "Hạn đóng học phí không được để trống")
    private LocalDate hanDong;

    public TinhHocPhiRequest() {}

    public TinhHocPhiRequest(UUID lopId, String kyHocPhi, LocalDate hanDong) {
        this.lopId = lopId;
        this.kyHocPhi = kyHocPhi;
        this.hanDong = hanDong;
    }

    public UUID getLopId() { return lopId; }
    public void setLopId(UUID lopId) { this.lopId = lopId; }

    public String getKyHocPhi() { return kyHocPhi; }
    public void setKyHocPhi(String kyHocPhi) { this.kyHocPhi = kyHocPhi; }

    public LocalDate getHanDong() { return hanDong; }
    public void setHanDong(LocalDate hanDong) { this.hanDong = hanDong; }
}

