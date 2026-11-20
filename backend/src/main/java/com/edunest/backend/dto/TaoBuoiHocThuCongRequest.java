package com.edunest.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class TaoBuoiHocThuCongRequest {

    @NotNull(message = "Lớp học không được để trống")
    private UUID lopId;

    @NotNull(message = "Ngày học không được để trống")
    private LocalDate ngayHoc;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime gioBatDau;

    private Integer thoiLuongPhut = 90;
    private LocalTime gioKetThuc;

    private boolean choPhepNgayQuaKhu = false;

    public TaoBuoiHocThuCongRequest() {}

    public TaoBuoiHocThuCongRequest(UUID lopId, LocalDate ngayHoc, LocalTime gioBatDau, Integer thoiLuongPhut) {
        this.lopId = lopId;
        this.ngayHoc = ngayHoc;
        this.gioBatDau = gioBatDau;
        this.thoiLuongPhut = thoiLuongPhut != null ? thoiLuongPhut : 90;
    }

    public UUID getLopId() { return lopId; }
    public void setLopId(UUID lopId) { this.lopId = lopId; }

    public LocalDate getNgayHoc() { return ngayHoc; }
    public void setNgayHoc(LocalDate ngayHoc) { this.ngayHoc = ngayHoc; }

    public LocalTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalTime gioBatDau) { this.gioBatDau = gioBatDau; }

    public Integer getThoiLuongPhut() { return thoiLuongPhut; }
    public void setThoiLuongPhut(Integer thoiLuongPhut) { this.thoiLuongPhut = thoiLuongPhut; }

    public LocalTime getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(LocalTime gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public boolean isChoPhepNgayQuaKhu() { return choPhepNgayQuaKhu; }
    public void setChoPhepNgayQuaKhu(boolean choPhepNgayQuaKhu) { this.choPhepNgayQuaKhu = choPhepNgayQuaKhu; }
}

