package com.edunest.backend.tenant;

import jakarta.persistence.EntityManagerFactory;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Mỗi khi một JPA transaction bắt đầu, gắn mã trung tâm hiện tại vào kết nối:
 *     SELECT set_config('app.tenant_id', '<uuid>', true)
 * Tham số "true" = chỉ có hiệu lực trong transaction này, tự mất khi COMMIT/ROLLBACK, nên
 * không "rò" sang request khác dùng lại cùng kết nối trong connection pool.
 * Chính sách RLS ở PostgreSQL đọc giá trị này (hàm app_tenant_id()).
 */
public class TenantAwareTransactionManager extends JpaTransactionManager {

    public TenantAwareTransactionManager(EntityManagerFactory emf) {
        super(emf);
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);

        UUID tenantId = TenantContext.get();
        if (tenantId == null) {
            return;
        }
        DataSource ds = getDataSource();
        if (ds != null) {
            ConnectionHolder holder =
                    (ConnectionHolder) TransactionSynchronizationManager.getResource(ds);
            if (holder != null && holder.getConnection() != null) {
                try (PreparedStatement ps =
                        holder.getConnection().prepareStatement("select set_config('app.tenant_id', ?, true)")) {
                    ps.setString(1, tenantId.toString());
                    ps.execute();
                } catch (SQLException e) {
                    doCleanupAfterCompletion(transaction);
                    throw new CannotCreateTransactionException("Không gắn được tenant vào transaction", e);
                }
            }
        }
    }
}
