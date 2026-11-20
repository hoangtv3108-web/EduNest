package com.edunest.backend.dto;

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

    private Integer thoiLuongPhut = 90; // Mặc định 90 phút nếu không truyền
    private LocalTime gioKetThuc;

    private boolean boQuaKhiTrungLich = true; // Nếu trùng thì bỏ qua ngày đó và sinh bù buổi kế tiếp
    private boolean choPhepNgayQuaKhu = false; // Mặc định không cho phép ngày trong quá khứ

    public SinhBuoiHocRequest() {}

    public SinhBuoiHocRequest(LocalDate ngayBatDau, int soBuoi, List<Integer> thuTrongTuan, LocalTime gioBatDau) {
        this.ngayBatDau = ngayBatDau;
        this.soBuoi = soBuoi;
        this.thuTrongTuan = thuTrongTuan;
        this.gioBatDau = gioBatDau;
        this.thoiLuongPhut = 90;
    }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public int getSoBuoi() { return soBuoi; }
    public void setSoBuoi(int soBuoi) { this.soBuoi = soBuoi; }

    public List<Integer> getThuTrongTuan() { return thuTrongTuan; }
    public void setThuTrongTuan(List<Integer> thuTrongTuan) { this.thuTrongTuan = thuTrongTuan; }

    public LocalTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalTime gioBatDau) { this.gioBatDau = gioBatDau; }

    public Integer getThoiLuongPhut() { return thoiLuongPhut; }
    public void setThoiLuongPhut(Integer thoiLuongPhut) { this.thoiLuongPhut = thoiLuongPhut; }

    public LocalTime getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(LocalTime gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public boolean isBoQuaKhiTrungLich() { return boQuaKhiTrungLich; }
    public void setBoQuaKhiTrungLich(boolean boQuaKhiTrungLich) { this.boQuaKhiTrungLich = boQuaKhiTrungLich; }

    public boolean isChoPhepNgayQuaKhu() { return choPhepNgayQuaKhu; }
    public void setChoPhepNgayQuaKhu(boolean choPhepNgayQuaKhu) { this.choPhepNgayQuaKhu = choPhepNgayQuaKhu; }
}
