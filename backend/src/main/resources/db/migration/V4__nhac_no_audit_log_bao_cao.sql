-- Migration V4: Giai đoạn 3 - Nhắc học phí tự động, Nhật ký kiểm toán, Báo cáo cuối tháng

-- ============================================================================
-- 1. hoa_don_hoc_phi: ghi nhận ngày thanh toán thực tế (cần để tính "tỉ lệ đóng đúng hạn")
-- ============================================================================
ALTER TABLE hoa_don_hoc_phi ADD COLUMN IF NOT EXISTS ngay_thanh_toan DATE;

-- NOT VALID: chỉ kiểm tra các lần ghi mới, không làm hỏng dữ liệu cũ (API cũ từng nhận chuỗi tuỳ ý).
ALTER TABLE hoa_don_hoc_phi
    ADD CONSTRAINT ck_hoa_don_trang_thai
    CHECK (trang_thai IN ('CHUA_THANH_TOAN', 'DA_THANH_TOAN')) NOT VALID;

-- Scheduler nhắc nợ chỉ quét hóa đơn chưa thanh toán theo hạn đóng
CREATE INDEX IF NOT EXISTS idx_hoa_don_nhac_no
    ON hoa_don_hoc_phi (tenant_id, han_dong) WHERE trang_thai = 'CHUA_THANH_TOAN';
-- Báo cáo tháng gom theo han_dong
CREATE INDEX IF NOT EXISTS idx_hoa_don_han_dong ON hoa_don_hoc_phi (tenant_id, han_dong);

-- ============================================================================
-- 2. lich_su_nhac_no: chống gửi trùng (idempotency) + lưu vết từng lần nhắc
-- ============================================================================
CREATE TABLE IF NOT EXISTS lich_su_nhac_no (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    hoa_don_id    UUID NOT NULL REFERENCES hoa_don_hoc_phi(id) ON DELETE CASCADE,
    ngay_nhac     DATE NOT NULL,
    kenh_gui      TEXT NOT NULL,                       -- EMAIL, ZALO_ZNS
    trang_thai    TEXT NOT NULL DEFAULT 'DANG_GUI',    -- DANG_GUI, DA_GUI, LOI
    nguoi_nhan    TEXT,                                -- đã che bớt (vd: ph***@gmail.com, 09*****678)
    chi_tiet_loi  TEXT,
    ngay_tao      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_nhac_no_kenh CHECK (kenh_gui IN ('EMAIL', 'ZALO_ZNS')),
    CONSTRAINT ck_nhac_no_trang_thai CHECK (trang_thai IN ('DANG_GUI', 'DA_GUI', 'LOI')),
    -- Chốt chặn cuối cùng ở tầng CSDL: một hóa đơn / một kênh / một ngày chỉ có tối đa 1 lần nhắc,
    -- kể cả khi chạy nhiều instance backend cùng lúc.
    CONSTRAINT uq_nhac_no_hoa_don_kenh_ngay UNIQUE (hoa_don_id, kenh_gui, ngay_nhac)
);
CREATE INDEX IF NOT EXISTS idx_nhac_no_tenant ON lich_su_nhac_no(tenant_id, ngay_nhac DESC);
CREATE INDEX IF NOT EXISTS idx_nhac_no_hoa_don ON lich_su_nhac_no(hoa_don_id, kenh_gui);

CALL enable_tenant_rls('lich_su_nhac_no', 'tenant_id');

-- ============================================================================
-- 3. nhat_ky_kiem_toan: nhật ký kiểm toán (OWASP ASVS V7 - ghi nhật ký)
-- ============================================================================
CREATE TABLE IF NOT EXISTS nhat_ky_kiem_toan (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id            UUID NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    nguoi_thuc_hien_id   UUID,           -- claim 'sub' của JWT (NULL nếu do hệ thống tự chạy)
    nguoi_thuc_hien_ten  TEXT,           -- tên hiển thị tại thời điểm thao tác (để tra cứu cho dễ đọc)
    vai_tro              TEXT,           -- CENTER_ADMIN, TEACHER, ...
    hanh_dong            TEXT NOT NULL,  -- XAC_NHAN_NOP_HOC_PHI, SUA_DIEM_DANH, ...
    doi_tuong_loai       TEXT NOT NULL,  -- HOA_DON, DIEM_DANH, LOP
    doi_tuong_id         UUID,
    du_lieu_cu           JSONB,
    du_lieu_moi          JSONB,
    thoi_gian            TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_nkkt_tenant_thoi_gian ON nhat_ky_kiem_toan (tenant_id, thoi_gian DESC);
CREATE INDEX IF NOT EXISTS idx_nkkt_tenant_hanh_dong ON nhat_ky_kiem_toan (tenant_id, hanh_dong);
CREATE INDEX IF NOT EXISTS idx_nkkt_doi_tuong ON nhat_ky_kiem_toan (tenant_id, doi_tuong_id);

CALL enable_tenant_rls('nhat_ky_kiem_toan', 'tenant_id');

-- Nhật ký chỉ-ghi-thêm (append-only): tài khoản ứng dụng không được sửa/xoá dòng nhật ký (ASVS 7.3).
REVOKE UPDATE, DELETE ON nhat_ky_kiem_toan FROM edunest_app;

-- ============================================================================
-- 4. Cho phép tác vụ nền (scheduler) liệt kê các trung tâm đang hoạt động
-- ============================================================================
-- Bảng tenant có RLS (id = app_tenant_id()) nên khi chưa có ngữ cảnh tenant sẽ không thấy hàng nào.
-- Chính sách bổ sung dưới đây CHỈ cho phép SELECT (không INSERT/UPDATE/DELETE), và chỉ khi transaction
-- tự khai báo app.system_job = 'liet_ke_tenant' (xem TenantLister). Sau khi có danh sách, scheduler
-- xử lý từng tenant trong transaction riêng với app.tenant_id tương ứng, nên RLS vẫn áp dụng đầy đủ
-- cho mọi bảng dữ liệu nghiệp vụ.
DROP POLICY IF EXISTS tenant_system_list_policy ON tenant;
CREATE POLICY tenant_system_list_policy ON tenant
    FOR SELECT
    USING (current_setting('app.system_job', true) = 'liet_ke_tenant');