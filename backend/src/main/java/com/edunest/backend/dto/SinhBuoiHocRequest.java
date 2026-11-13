package com.edunest.backend.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class SinhBuoiHocRequest {

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate ngayBatDau;

    @Min(value = 1, message = "Số buổi học phải >= 1")
    private int soBuoi;

    @NotEmpty(message = "Vui lòng chọn ít nhất một thứ trong tuần")
    private List<Integer> thuTrongTuan; // 1 = Thứ Hai, 2 = Thứ Ba, ..., 7 = Chủ Nhật

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime gioBatDau;

    public SinhBuoiHocRequest() {}

    public SinhBuoiHocRequest(LocalDate ngayBatDau, int soBuoi, List<Integer> thuTrongTuan, LocalTime gioBatDau) {
        this.ngayBatDau = ngayBatDau;
        this.soBuoi = soBuoi;
        this.thuTrongTuan = thuTrongTuan;
        this.gioBatDau = gioBatDau;
    }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public int getSoBuoi() { return soBuoi; }
    public void setSoBuoi(int soBuoi) { this.soBuoi = soBuoi; }

    public List<Integer> getThuTrongTuan() { return thuTrongTuan; }
    public void setThuTrongTuan(List<Integer> thuTrongTuan) { this.thuTrongTuan = thuTrongTuan; }

    public LocalTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalTime gioBatDau) { this.gioBatDau = gioBatDau; }
}

