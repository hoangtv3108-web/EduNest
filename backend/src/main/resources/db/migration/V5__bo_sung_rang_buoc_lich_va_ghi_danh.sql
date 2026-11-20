-- Migration V5: Ràng buộc nâng cao cho Buổi học và Ghi danh học viên

-- 1. Bổ sung cột gio_ket_thuc cho bảng buoi_hoc (mặc định nếu null là gio_bat_dau + 90 phút)
ALTER TABLE buoi_hoc ADD COLUMN IF NOT EXISTS gio_ket_thuc TIME;

UPDATE buoi_hoc 
SET gio_ket_thuc = (gio_bat_dau + INTERVAL '90 minutes')::TIME 
WHERE gio_ket_thuc IS NULL;

ALTER TABLE buoi_hoc ALTER COLUMN gio_ket_thuc SET NOT NULL;

-- Ràng buộc giờ kết thúc phải sau giờ bắt đầu
ALTER TABLE buoi_hoc 
    ADD CONSTRAINT ck_buoi_hoc_gio_hop_le 
    CHECK (gio_ket_thuc > gio_bat_dau) NOT VALID;

-- Index chống trùng buổi học trong cùng 1 lớp (cùng ngày và giờ bắt đầu)
CREATE UNIQUE INDEX IF NOT EXISTS uq_buoi_hoc_lop_ngay_gio 
    ON buoi_hoc(lop_id, ngay_hoc, gio_bat_dau);

-- Index hỗ trợ kiểm tra xung đột khung giờ buổi học theo tenant
CREATE INDEX IF NOT EXISTS idx_buoi_hoc_xung_dot 
    ON buoi_hoc(tenant_id, ngay_hoc, gio_bat_dau, gio_ket_thuc);

-- 2. Ràng buộc ghi danh: Chặn một học viên có nhiều hơn 1 bản ghi 'DANG_HOC' trong toàn trung tâm (chặn chéo lớp)
CREATE UNIQUE INDEX IF NOT EXISTS uq_ghi_danh_hoc_vien_dang_hoc 
    ON ghi_danh(tenant_id, hoc_vien_id) 
    WHERE trang_thai = 'DANG_HOC';

-- Ràng buộc trạng thái ghi danh hợp lệ
ALTER TABLE ghi_danh 
    ADD CONSTRAINT ck_ghi_danh_trang_thai 
    CHECK (trang_thai IN ('DANG_HOC', 'TAM_NGHI', 'DA_NGHI', 'HOAN_THANH')) NOT VALID;

