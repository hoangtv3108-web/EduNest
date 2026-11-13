package com.edunest.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "diem_danh", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"buoi_hoc_id", "hoc_vien_id"})
})
public class DiemDanh {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "buoi_hoc_id", nullable = false)
    private UUID buoiHocId;

    @Column(name = "hoc_vien_id", nullable = false)
    private UUID hocVienId;

    @Column(name = "trang_thai", nullable = false)
    private String trangThai; // CO_MAT, VANG, CO_PHEP

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "thoi_diem", nullable = false)
    private OffsetDateTime thoiDiem = OffsetDateTime.now();

    public DiemDanh() {}

    public DiemDanh(UUID tenantId, UUID buoiHocId, UUID hocVienId, String trangThai, String ghiChu) {
        this.tenantId = tenantId;
        this.buoiHocId = buoiHocId;
        this.hocVienId = hocVienId;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.thoiDiem = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getBuoiHocId() { return buoiHocId; }
    public void setBuoiHocId(UUID buoiHocId) { this.buoiHocId = buoiHocId; }

    public UUID getHocVienId() { return hocVienId; }
    public void setHocVienId(UUID hocVienId) { this.hocVienId = hocVienId; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public OffsetDateTime getThoiDiem() { return thoiDiem; }
    public void setThoiDiem(OffsetDateTime thoiDiem) { this.thoiDiem = thoiDiem; }
}

