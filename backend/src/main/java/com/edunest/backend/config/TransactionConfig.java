package com.edunest.backend.config;

import com.edunest.backend.tenant.TenantAwareTransactionManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransactionConfig {

    /** Thay transaction manager mặc định của Spring Boot bằng bản tự gắn tenant vào mỗi transaction. */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new TenantAwareTransactionManager(emf);
    }
}
