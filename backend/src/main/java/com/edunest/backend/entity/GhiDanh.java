package com.edunest.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ghi_danh", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"lop_id", "hoc_vien_id"})
})
public class GhiDanh {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "hoc_vien_id", nullable = false)
    private UUID hocVienId;

    @Column(name = "lop_id", nullable = false)
    private UUID lopId;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDate ngayBatDau = LocalDate.now();

    @Column(name = "trang_thai", nullable = false)
    private String trangThai = "DANG_HOC";

    @Column(name = "ngay_tao", nullable = false)
    private OffsetDateTime ngayTao = OffsetDateTime.now();

    public GhiDanh() {}

    public GhiDanh(UUID tenantId, UUID hocVienId, UUID lopId, LocalDate ngayBatDau) {
        this.tenantId = tenantId;
        this.hocVienId = hocVienId;
        this.lopId = lopId;
        this.ngayBatDau = ngayBatDau != null ? ngayBatDau : LocalDate.now();
        this.trangThai = "DANG_HOC";
        this.ngayTao = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getHocVienId() { return hocVienId; }
    public void setHocVienId(UUID hocVienId) { this.hocVienId = hocVienId; }

    public UUID getLopId() { return lopId; }
    public void setLopId(UUID lopId) { this.lopId = lopId; }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public OffsetDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(OffsetDateTime ngayTao) { this.ngayTao = ngayTao; }
}

