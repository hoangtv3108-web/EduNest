package com.edunest.backend.tenant;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Liệt kê các trung tâm đang hoạt động cho tác vụ nền (scheduler) vốn không có
 * JWT/TenantContext.
 * Bảng tenant có RLS nên cần khai báo app.system_job = 'liet_ke_tenant' (chính
 * sách SELECT-only ở V4).
 * Chỉ dùng để lấy danh sách id; dữ liệu nghiệp vụ của từng tenant vẫn đọc qua
 * RLS với TenantContext riêng.
 */
@Component
public class TenantLister {

    private final JdbcClient jdbc;
    private final TransactionTemplate tx;

    public TenantLister(JdbcClient jdbc, TransactionTemplate tx) {
        this.jdbc = jdbc;
        this.tx = tx;
    }

    public List<UUID> layTenantDangHoatDong() {
        return tx.execute(status -> {
            // is_local = true: chỉ có hiệu lực trong transaction này, không rò sang request
            // khác trong pool
            jdbc.sql("SELECT set_config('app.system_job', 'liet_ke_tenant', true)")
                    .query((rs, n) -> rs.getString(1)).single();
            return jdbc.sql("SELECT id FROM tenant WHERE trang_thai = 'HOAT_DONG' ORDER BY ngay_tao")
                    .query((rs, n) -> rs.getObject("id", UUID.class))
                    .list();
        });
    }
}