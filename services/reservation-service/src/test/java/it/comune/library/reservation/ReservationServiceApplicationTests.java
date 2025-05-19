package it.comune.library.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
        // ➊ disable Kafka auto-configuration so we don’t need a broker in unit tests
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        // ➋ point Spring Data at an in-memory H2 DB instead of Postgres
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
    }
)
class ReservationServiceApplicationTests {

    @Test
    void contextLoads() {
        // will succeed if the Spring context starts
    }
}

