package com.edunest.backend.dto;

import java.util.List;

public class SinhBuoiHocKetQuaResponse {
    private int tongSoYeuCau;
    private int soBuoiThanhCong;
    private int soBuoiBiBoQua;
    private List<BuoiHocResponse> danhSachThanhCong;
    private List<BuoiHocBoQuaItem> danhSachBiBoQua;

    public SinhBuoiHocKetQuaResponse() {}

    public SinhBuoiHocKetQuaResponse(int tongSoYeuCau, int soBuoiThanhCong, int soBuoiBiBoQua,
                                     List<BuoiHocResponse> danhSachThanhCong,
                                     List<BuoiHocBoQuaItem> danhSachBiBoQua) {
        this.tongSoYeuCau = tongSoYeuCau;
        this.soBuoiThanhCong = soBuoiThanhCong;
        this.soBuoiBiBoQua = soBuoiBiBoQua;
        this.danhSachThanhCong = danhSachThanhCong;
        this.danhSachBiBoQua = danhSachBiBoQua;
    }

    public int getTongSoYeuCau() { return tongSoYeuCau; }
    public void setTongSoYeuCau(int tongSoYeuCau) { this.tongSoYeuCau = tongSoYeuCau; }

    public int getSoBuoiThanhCong() { return soBuoiThanhCong; }
    public void setSoBuoiThanhCong(int soBuoiThanhCong) { this.soBuoiThanhCong = soBuoiThanhCong; }

    public int getSoBuoiBiBoQua() { return soBuoiBiBoQua; }
    public void setSoBuoiBiBoQua(int soBuoiBiBoQua) { this.soBuoiBiBoQua = soBuoiBiBoQua; }

    public List<BuoiHocResponse> getDanhSachThanhCong() { return danhSachThanhCong; }
    public void setDanhSachThanhCong(List<BuoiHocResponse> danhSachThanhCong) { this.danhSachThanhCong = danhSachThanhCong; }

    public List<BuoiHocBoQuaItem> getDanhSachBiBoQua() { return danhSachBiBoQua; }
    public void setDanhSachBiBoQua(List<BuoiHocBoQuaItem> danhSachBiBoQua) { this.danhSachBiBoQua = danhSachBiBoQua; }
}

