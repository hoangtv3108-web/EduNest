-- Kích hoạt extension sinh UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. Bảng tenant (trung tâm)
CREATE TABLE tenant (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ten_trung_tam  TEXT NOT NULL,
    ngay_tao       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    trang_thai     TEXT NOT NULL DEFAULT 'HOAT_DONG'
);

-- 2. Bảng tai_khoan (người dùng của trung tâm)
CREATE TABLE tai_khoan (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    keycloak_sub  TEXT NOT NULL UNIQUE,  -- claim 'sub' trong token JWT của Keycloak
    email         TEXT,
    ho_ten        TEXT NOT NULL,
    vai_tro       TEXT NOT NULL,         -- CENTER_ADMIN, TEACHER, PARENT
    ngay_tao      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Index tăng tốc truy vấn theo tenant
CREATE INDEX idx_tai_khoan_tenant ON tai_khoan(tenant_id);