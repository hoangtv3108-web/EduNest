package com.edunest.backend.dto;

public class ImportExcelResponse {
    private int soPhuHuynhTaoMoi;
    private int soHocVienTaoMoi;
    private String thongBao;

    public ImportExcelResponse() {}

    public ImportExcelResponse(int soPhuHuynhTaoMoi, int soHocVienTaoMoi, String thongBao) {
        this.soPhuHuynhTaoMoi = soPhuHuynhTaoMoi;
        this.soHocVienTaoMoi = soHocVienTaoMoi;
        this.thongBao = thongBao;
    }

    public int getSoPhuHuynhTaoMoi() { return soPhuHuynhTaoMoi; }
    public void setSoPhuHuynhTaoMoi(int soPhuHuynhTaoMoi) { this.soPhuHuynhTaoMoi = soPhuHuynhTaoMoi; }

    public int getSoHocVienTaoMoi() { return soHocVienTaoMoi; }
    public void setSoHocVienTaoMoi(int soHocVienTaoMoi) { this.soHocVienTaoMoi = soHocVienTaoMoi; }

    public String getThongBao() { return thongBao; }
    public void setThongBao(String thongBao) { this.thongBao = thongBao; }
}

