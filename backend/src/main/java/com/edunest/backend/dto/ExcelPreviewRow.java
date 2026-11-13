package com.edunest.backend.dto;

public class ExcelPreviewRow {
    private int rowNumber;
    private String hoTenPhuHuynh;
    private String sdtZalo;
    private String email;
    private String hoTenHocVien;
    private String ngaySinh;
    private boolean hopLe;
    private String loi;

    public ExcelPreviewRow() {}

    public ExcelPreviewRow(int rowNumber, String hoTenPhuHuynh, String sdtZalo, String email,
                           String hoTenHocVien, String ngaySinh, boolean hopLe, String loi) {
        this.rowNumber = rowNumber;
        this.hoTenPhuHuynh = hoTenPhuHuynh;
        this.sdtZalo = sdtZalo;
        this.email = email;
        this.hoTenHocVien = hoTenHocVien;
        this.ngaySinh = ngaySinh;
        this.hopLe = hopLe;
        this.loi = loi;
    }

    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }

    public String getHoTenPhuHuynh() { return hoTenPhuHuynh; }
    public void setHoTenPhuHuynh(String hoTenPhuHuynh) { this.hoTenPhuHuynh = hoTenPhuHuynh; }

    public String getSdtZalo() { return sdtZalo; }
    public void setSdtZalo(String sdtZalo) { this.sdtZalo = sdtZalo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHoTenHocVien() { return hoTenHocVien; }
    public void setHoTenHocVien(String hoTenHocVien) { this.hoTenHocVien = hoTenHocVien; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public boolean isHopLe() { return hopLe; }
    public void setHopLe(boolean hopLe) { this.hopLe = hopLe; }

    public String getLoi() { return loi; }
    public void setLoi(String loi) { this.loi = loi; }
}

