package com.edunest.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "hoc_vien")
public class HocVien {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "phu_huynh_id")
    private UUID phuHuynhId;

    @Column(name = "ho_ten", nullable = false)
    private String hoTen;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "ngay_tao", nullable = false)
    private OffsetDateTime ngayTao = OffsetDateTime.now();

    public HocVien() {}

    public HocVien(UUID tenantId, UUID phuHuynhId, String hoTen, LocalDate ngaySinh) {
        this.tenantId = tenantId;
        this.phuHuynhId = phuHuynhId;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.ngayTao = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getPhuHuynhId() { return phuHuynhId; }
    public void setPhuHuynhId(UUID phuHuynhId) { this.phuHuynhId = phuHuynhId; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public OffsetDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(OffsetDateTime ngayTao) { this.ngayTao = ngayTao; }
}

