package com.edunest.backend.dto;

import java.util.List;

public class ExcelPreviewResponse {
    private int tongSoDong;
    private int soDongHopLe;
    private int soDongLoi;
    private List<ExcelPreviewRow> chiTiet;

    public ExcelPreviewResponse() {}

    public ExcelPreviewResponse(int tongSoDong, int soDongHopLe, int soDongLoi, List<ExcelPreviewRow> chiTiet) {
        this.tongSoDong = tongSoDong;
        this.soDongHopLe = soDongHopLe;
        this.soDongLoi = soDongLoi;
        this.chiTiet = chiTiet;
    }

    public int getTongSoDong() { return tongSoDong; }
    public void setTongSoDong(int tongSoDong) { this.tongSoDong = tongSoDong; }

    public int getSoDongHopLe() { return soDongHopLe; }
    public void setSoDongHopLe(int soDongHopLe) { this.soDongHopLe = soDongHopLe; }

    public int getSoDongLoi() { return soDongLoi; }
    public void setSoDongLoi(int soDongLoi) { this.soDongLoi = soDongLoi; }

    public List<ExcelPreviewRow> getChiTiet() { return chiTiet; }
    public void setChiTiet(List<ExcelPreviewRow> chiTiet) { this.chiTiet = chiTiet; }
}

