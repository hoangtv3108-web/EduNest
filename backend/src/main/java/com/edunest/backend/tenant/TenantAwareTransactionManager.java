package com.edunest.backend.tenant;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Mỗi khi một transaction bắt đầu, gắn mã trung tâm hiện tại vào kết nối:
 *     SELECT set_config('app.tenant_id', '<uuid>', true)
 * Tham số "true" = chỉ có hiệu lực trong transaction này, tự mất khi COMMIT/ROLLBACK, nên
 * không "rò" sang request khác dùng lại cùng kết nối trong connection pool.
 * Chính sách RLS ở PostgreSQL đọc giá trị này (hàm app_tenant_id()).
 *
 * Hệ quả: mọi truy cập bảng có RLS phải nằm trong @Transactional. Ngoài transaction thì
 * không có tenant => PostgreSQL trả 0 hàng (an toàn theo hướng "đóng").
 */
public class TenantAwareTransactionManager extends DataSourceTransactionManager {

    public TenantAwareTransactionManager(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);

        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            return;
        }
        ConnectionHolder holder =
                (ConnectionHolder) TransactionSynchronizationManager.getResource(obtainDataSource());
        try (PreparedStatement ps =
                holder.getConnection().prepareStatement("select set_config('app.tenant_id', ?, true)")) {
            ps.setString(1, tenantId.toString());
            ps.execute();
        } catch (SQLException e) {
            doCleanupAfterCompletion(transaction); // trả kết nối, tránh rò rỉ
            throw new CannotCreateTransactionException("Không gắn được tenant vào transaction", e);
        }
    }
}
