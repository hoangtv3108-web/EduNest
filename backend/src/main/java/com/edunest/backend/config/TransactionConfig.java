package com.edunest.backend.config;

import com.edunest.backend.tenant.TenantAwareTransactionManager;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransactionConfig {

    /** Thay transaction manager mặc định của Spring Boot bằng bản tự gắn tenant vào mỗi transaction. */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new TenantAwareTransactionManager(dataSource);
    }
}
