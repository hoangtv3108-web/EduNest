package com.edunest.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "buoi_hoc")
public class BuoiHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "lop_id", nullable = false)
    private UUID lopId;

    @Column(name = "ngay_hoc", nullable = false)
    private LocalDate ngayHoc;

    @Column(name = "gio_bat_dau", nullable = false)
    private LocalTime gioBatDau;

    @Column(name = "ngay_tao", nullable = false)
    private OffsetDateTime ngayTao = OffsetDateTime.now();

    public BuoiHoc() {}

    public BuoiHoc(UUID tenantId, UUID lopId, LocalDate ngayHoc, LocalTime gioBatDau) {
        this.tenantId = tenantId;
        this.lopId = lopId;
        this.ngayHoc = ngayHoc;
        this.gioBatDau = gioBatDau;
        this.ngayTao = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getLopId() { return lopId; }
    public void setLopId(UUID lopId) { this.lopId = lopId; }

    public LocalDate getNgayHoc() { return ngayHoc; }
    public void setNgayHoc(LocalDate ngayHoc) { this.ngayHoc = ngayHoc; }

    public LocalTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalTime gioBatDau) { this.gioBatDau = gioBatDau; }

    public OffsetDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(OffsetDateTime ngayTao) { this.ngayTao = ngayTao; }
}

