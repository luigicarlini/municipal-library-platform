package it.comune.library.reservation.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/** Container PostgreSQL condiviso da tutti i test */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestContainer {

    /** parte una sola volta per l’intera JVM */
    private static final PostgreSQLContainer<?> PG =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("postgres");

    static {
        PG.start();
    }

    /** espone le property PRIMA che Spring costruisca il DataSource */
    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry reg) {
        reg.add("spring.datasource.url",      PG::getJdbcUrl);
        reg.add("spring.datasource.username", PG::getUsername);
        reg.add("spring.datasource.password", PG::getPassword);
    }
}
