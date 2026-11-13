package com.edunest.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "hoa_don_hoc_phi")
public class HoaDonHocPhi {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "ghi_danh_id", nullable = false)
    private UUID ghiDanhId;

    @Column(name = "ky_hoc_phi", nullable = false)
    private String kyHocPhi;

    @Column(name = "so_tien", nullable = false)
    private BigDecimal soTien;

    @Column(name = "so_buoi", nullable = false)
    private Integer soBuoi = 0;

    @Column(name = "han_dong", nullable = false)
    private LocalDate hanDong;

    @Column(name = "trang_thai", nullable = false)
    private String trangThai = "CHUA_THANH_TOAN";

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @Column(name = "ngay_tao", nullable = false)
    private OffsetDateTime ngayTao = OffsetDateTime.now();

    public HoaDonHocPhi() {}

    public HoaDonHocPhi(UUID tenantId, UUID ghiDanhId, String kyHocPhi, BigDecimal soTien, Integer soBuoi, LocalDate hanDong) {
        this.tenantId = tenantId;
        this.ghiDanhId = ghiDanhId;
        this.kyHocPhi = kyHocPhi;
        this.soTien = soTien;
        this.soBuoi = soBuoi;
        this.hanDong = hanDong;
        this.trangThai = "CHUA_THANH_TOAN";
        this.version = 0;
        this.ngayTao = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getGhiDanhId() { return ghiDanhId; }
    public void setGhiDanhId(UUID ghiDanhId) { this.ghiDanhId = ghiDanhId; }

    public String getKyHocPhi() { return kyHocPhi; }
    public void setKyHocPhi(String kyHocPhi) { this.kyHocPhi = kyHocPhi; }

    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }

    public Integer getSoBuoi() { return soBuoi; }
    public void setSoBuoi(Integer soBuoi) { this.soBuoi = soBuoi; }

    public LocalDate getHanDong() { return hanDong; }
    public void setHanDong(LocalDate hanDong) { this.hanDong = hanDong; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public OffsetDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(OffsetDateTime ngayTao) { this.ngayTao = ngayTao; }
}

