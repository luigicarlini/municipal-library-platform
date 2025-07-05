// src/test/java/it/comune/library/reservation/config/TxManagerAliasTestConfig.java
package it.comune.library.reservation.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@TestConfiguration
public class TxManagerAliasTestConfig {

    /** alias col nome che i service si aspettano */
    @Bean("transactionManager")
    @Primary                // resta comunque il candidato principale
    PlatformTransactionManager txManagerAlias(
            PlatformTransactionManager jpaTransactionManager) {
        return jpaTransactionManager;     // semplicemente lo restituiamo
    }

    /** il template a mano: con due TM Boot non lo auto-crea più */
    @Bean
    TransactionTemplate txTemplate(PlatformTransactionManager jpaTransactionManager) {
        return new TransactionTemplate(jpaTransactionManager);
    }
}