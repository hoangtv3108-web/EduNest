-- Hàm đọc session variable an toàn (trả về NULL nếu chưa set hoặc rỗng)
CREATE OR REPLACE FUNCTION app_tenant_id() RETURNS UUID
LANGUAGE sql STABLE AS $$
    SELECT NULLIF(current_setting('app.tenant_id', true), '')::UUID;
$$;

-- Thủ tục tự động kích hoạt RLS cho bất kỳ bảng nào có tenant_id
CREATE OR REPLACE PROCEDURE enable_tenant_rls(p_table REGCLASS, p_column TEXT DEFAULT 'tenant_id')
LANGUAGE plpgsql AS $$
BEGIN
    -- 1. Bật RLS
    EXECUTE format('ALTER TABLE %s ENABLE ROW LEVEL SECURITY;', p_table);
    
    -- 2. Bắt buộc RLS cả với chủ bảng (Table Owner / Application User)
    EXECUTE format('ALTER TABLE %s FORCE ROW LEVEL SECURITY;', p_table);

    -- 3. Tạo chính sách lọc cả chiều đọc (USING) và chiều ghi (WITH CHECK)
    EXECUTE format('DROP POLICY IF EXISTS tenant_isolation_policy ON %s;', p_table);
    EXECUTE format(
        'CREATE POLICY tenant_isolation_policy ON %s ' ||
        'FOR ALL ' ||
        'USING (%I = (SELECT app_tenant_id())) ' ||
        'WITH CHECK (%I = (SELECT app_tenant_id()));',
        p_table, p_column, p_column
    );
END;
$$;

-- Áp dụng cho bảng tenant (so khớp cột id) và tai_khoan (so khớp tenant_id)
CALL enable_tenant_rls('tenant', 'id');
CALL enable_tenant_rls('tai_khoan', 'tenant_id');