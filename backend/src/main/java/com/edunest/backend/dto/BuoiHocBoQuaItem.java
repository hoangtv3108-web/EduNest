package com.edunest.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class BuoiHocBoQuaItem {
    private LocalDate ngayHoc;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private String lyDo;

    public BuoiHocBoQuaItem() {}

    public BuoiHocBoQuaItem(LocalDate ngayHoc, LocalTime gioBatDau, LocalTime gioKetThuc, String lyDo) {
        this.ngayHoc = ngayHoc;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.lyDo = lyDo;
    }

    public LocalDate getNgayHoc() { return ngayHoc; }
    public void setNgayHoc(LocalDate ngayHoc) { this.ngayHoc = ngayHoc; }

    public LocalTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalTime gioBatDau) { this.gioBatDau = gioBatDau; }

    public LocalTime getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(LocalTime gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }
}

