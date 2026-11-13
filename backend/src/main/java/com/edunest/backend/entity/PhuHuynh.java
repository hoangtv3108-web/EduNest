package com.edunest.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "phu_huynh")
public class PhuHuynh {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "ho_ten", nullable = false)
    private String hoTen;

    @Column(name = "sdt_zalo")
    private String sdtZalo;

    @Column(name = "email")
    private String email;

    @Column(name = "tai_khoan_id")
    private UUID taiKhoanId;

    @Column(name = "ngay_tao", nullable = false)
    private OffsetDateTime ngayTao = OffsetDateTime.now();

    public PhuHuynh() {}

    public PhuHuynh(UUID tenantId, String hoTen, String sdtZalo, String email) {
        this.tenantId = tenantId;
        this.hoTen = hoTen;
        this.sdtZalo = sdtZalo;
        this.email = email;
        this.ngayTao = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSdtZalo() { return sdtZalo; }
    public void setSdtZalo(String sdtZalo) { this.sdtZalo = sdtZalo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UUID getTaiKhoanId() { return taiKhoanId; }
    public void setTaiKhoanId(UUID taiKhoanId) { this.taiKhoanId = taiKhoanId; }

    public OffsetDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(OffsetDateTime ngayTao) { this.ngayTao = ngayTao; }
}

