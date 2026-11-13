-- Migration V3: Giai đoạn 2 - Các bảng nghiệp vụ quản lý học viên, lớp, buổi học, ghi danh, điểm danh, hóa đơn

-- 1. Bảng phụ huynh
CREATE TABLE IF NOT EXISTS phu_huynh (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    ho_ten        TEXT NOT NULL,
    sdt_zalo      TEXT,
    email         TEXT,
    tai_khoan_id  UUID REFERENCES tai_khoan(id) ON DELETE SET NULL,
    ngay_tao      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_phu_huynh_tenant ON phu_huynh(tenant_id);
CREATE INDEX IF NOT EXISTS idx_phu_huynh_sdt ON phu_huynh(tenant_id, sdt_zalo);

-- 2. Bảng học viên
CREATE TABLE IF NOT EXISTS hoc_vien (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    phu_huynh_id  UUID REFERENCES phu_huynh(id) ON DELETE SET NULL,
    ho_ten        TEXT NOT NULL,
    ngay_sinh     DATE,
    ngay_tao      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_hoc_vien_tenant ON hoc_vien(tenant_id);

-- 3. Bảng lớp học
CREATE TABLE IF NOT EXISTS lop (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    giao_vien_id      UUID REFERENCES tai_khoan(id) ON DELETE SET NULL,
    ten_lop           TEXT NOT NULL,
    hoc_phi_moi_buoi  NUMERIC(12,2) NOT NULL DEFAULT 0,
    trang_thai        TEXT NOT NULL DEFAULT 'DANG_MO',
    ngay_tao          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_lop_tenant ON lop(tenant_id);

-- 4. Bảng buổi học
CREATE TABLE IF NOT EXISTS buoi_hoc (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    lop_id        UUID NOT NULL REFERENCES lop(id) ON DELETE CASCADE,
    ngay_hoc      DATE NOT NULL,
    gio_bat_dau   TIME NOT NULL,
    ngay_tao      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_buoi_hoc_tenant ON buoi_hoc(tenant_id);
CREATE INDEX IF NOT EXISTS idx_buoi_hoc_lop ON buoi_hoc(lop_id, ngay_hoc);

-- 5. Bảng ghi danh (học viên tham gia lớp học)
CREATE TABLE IF NOT EXISTS ghi_danh (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    hoc_vien_id   UUID NOT NULL REFERENCES hoc_vien(id) ON DELETE CASCADE,
    lop_id        UUID NOT NULL REFERENCES lop(id) ON DELETE CASCADE,
    ngay_bat_dau  DATE NOT NULL DEFAULT CURRENT_DATE,
    trang_thai    TEXT NOT NULL DEFAULT 'DANG_HOC',
    ngay_tao      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_ghi_danh_lop_hoc_vien UNIQUE (lop_id, hoc_vien_id)
);
CREATE INDEX IF NOT EXISTS idx_ghi_danh_tenant ON ghi_danh(tenant_id);
CREATE INDEX IF NOT EXISTS idx_ghi_danh_lop ON ghi_danh(lop_id);

-- 6. Bảng điểm danh
CREATE TABLE IF NOT EXISTS diem_danh (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    buoi_hoc_id   UUID NOT NULL REFERENCES buoi_hoc(id) ON DELETE CASCADE,
    hoc_vien_id   UUID NOT NULL REFERENCES hoc_vien(id) ON DELETE CASCADE,
    trang_thai    TEXT NOT NULL, -- 'CO_MAT', 'VANG', 'CO_PHEP'
    ghi_chu       TEXT,
    thoi_diem     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_diem_danh_buoi_hoc_vien UNIQUE (buoi_hoc_id, hoc_vien_id)
);
CREATE INDEX IF NOT EXISTS idx_diem_danh_tenant ON diem_danh(tenant_id);
CREATE INDEX IF NOT EXISTS idx_diem_danh_buoi ON diem_danh(buoi_hoc_id);

-- 7. Bảng hóa đơn học phí
CREATE TABLE IF NOT EXISTS hoa_don_hoc_phi (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    ghi_danh_id   UUID NOT NULL REFERENCES ghi_danh(id) ON DELETE CASCADE,
    ky_hoc_phi    TEXT NOT NULL,
    so_tien       NUMERIC(12,2) NOT NULL,
    so_buoi       INT NOT NULL DEFAULT 0,
    han_dong      DATE NOT NULL,
    trang_thai    TEXT NOT NULL DEFAULT 'CHUA_THANH_TOAN',
    version       INT NOT NULL DEFAULT 0,
    ngay_tao      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_hoa_don_tenant ON hoa_don_hoc_phi(tenant_id);
CREATE INDEX IF NOT EXISTS idx_hoa_don_ghi_danh ON hoa_don_hoc_phi(ghi_danh_id);

-- Bật Row-Level Security (RLS) cho tất cả các bảng nghiệp vụ theo tenant_id
CALL enable_tenant_rls('phu_huynh', 'tenant_id');
CALL enable_tenant_rls('hoc_vien', 'tenant_id');
CALL enable_tenant_rls('lop', 'tenant_id');
CALL enable_tenant_rls('buoi_hoc', 'tenant_id');
CALL enable_tenant_rls('ghi_danh', 'tenant_id');
CALL enable_tenant_rls('diem_danh', 'tenant_id');
CALL enable_tenant_rls('hoa_don_hoc_phi', 'tenant_id');

