package com.edunest.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "lop")
public class Lop {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "giao_vien_id")
    private UUID giaoVienId;

    @Column(name = "ten_lop", nullable = false)
    private String tenLop;

    @Column(name = "hoc_phi_moi_buoi", nullable = false)
    private BigDecimal hocPhiMoiBuoi = BigDecimal.ZERO;

    @Column(name = "trang_thai", nullable = false)
    private String trangThai = "DANG_MO";

    @Column(name = "ngay_tao", nullable = false)
    private OffsetDateTime ngayTao = OffsetDateTime.now();

    public Lop() {}

    public Lop(UUID tenantId, String tenLop, BigDecimal hocPhiMoiBuoi) {
        this.tenantId = tenantId;
        this.tenLop = tenLop;
        this.hocPhiMoiBuoi = hocPhiMoiBuoi;
        this.trangThai = "DANG_MO";
        this.ngayTao = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getGiaoVienId() { return giaoVienId; }
    public void setGiaoVienId(UUID giaoVienId) { this.giaoVienId = giaoVienId; }

    public String getTenLop() { return tenLop; }
    public void setTenLop(String tenLop) { this.tenLop = tenLop; }

    public BigDecimal getHocPhiMoiBuoi() { return hocPhiMoiBuoi; }
    public void setHocPhiMoiBuoi(BigDecimal hocPhiMoiBuoi) { this.hocPhiMoiBuoi = hocPhiMoiBuoi; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public OffsetDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(OffsetDateTime ngayTao) { this.ngayTao = ngayTao; }
}

