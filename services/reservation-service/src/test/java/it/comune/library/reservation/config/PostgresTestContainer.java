package it.comune.library.reservation.config;

import org.springframework.boot.test.context.TestConfiguration;
// import org.springframework.test.context.DynamicPropertyRegistry;
// import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import com.zaxxer.hikari.HikariDataSource;
// import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
// import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

/**
 * Un singolo container PostgreSQL ri-usato da tutti i test Spring Boot.
 * <p>
 *  • Parte al primo test, poi rimane in esecuzione (withReuse=true).<br>
 *  • Espone i parametri JDBC tramite un {@link HikariDataSource} registrato come {@code DataSource} di Spring.
 */
@TestConfiguration
public class PostgresTestContainer {

    /** Immagine Postgres “leggera” */
    private static final DockerImageName PG_IMAGE = DockerImageName.parse("postgres:15-alpine");

    /** Container singleton riutilizzabile */
    public static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(PG_IMAGE)
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("secret")
                    .withReuse(true);           // ⇦ accende la cache di Testcontainers

    static {
        POSTGRES.start();                      // parte una sola volta per l’intera JVM di test
    }

    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(POSTGRES.getJdbcUrl());
        ds.setUsername(POSTGRES.getUsername());
        ds.setPassword(POSTGRES.getPassword());
        // opzionale: tuning min/max pool per i test
        ds.setMaximumPoolSize(5);
        return ds;
    }

    /**
     * TransactionManager esplicito per evitare fallback al JPA‐TX-manager predefinito
     * quando si eseguono test puramente JDBC.
     */
    @Bean
    @DependsOn("dataSource")
    public PlatformTransactionManager txManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}