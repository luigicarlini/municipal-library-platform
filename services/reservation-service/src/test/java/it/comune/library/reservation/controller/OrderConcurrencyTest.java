package it.comune.library.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.comune.library.reservation.ReservationServiceApplication;
import it.comune.library.reservation.dto.BookDto;
import it.comune.library.reservation.dto.OrderDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Concurrency test:
 * – libro con stock 5
 * – 20 thread acquistano + mark-paid
 * – ci aspettiamo 5 204 e 15 409, stock = 0
 */
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)   // isola il contesto
@SpringBootTest(classes = ReservationServiceApplication.class, properties = {
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.flyway.enabled=false",
        // H2 →  Postgres Testcontainer
        "spring.datasource.url=jdbc:tc:postgresql:15:///test",
        "spring.datasource.username=test",
        "spring.datasource.password=test",
        // ⬇ timeout di shutdown del pool in millisecondi (0 = chiusura immediata)
        "spring.datasource.hikari.shutdown-timeout=0"
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class OrderConcurrencyTest {

    /* ------------ PostgreSQL container condiviso per il test --------- */
    @Container
    static final PostgreSQLContainer<?> PG =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(false);

    /* ------------ Propaghiamo le proprietà PRIMA che parta il contesto */
    @DynamicPropertySource
    static void pgProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",            PG::getJdbcUrl);
        r.add("spring.datasource.username",       PG::getUsername);
        r.add("spring.datasource.password",       PG::getPassword);
        r.add("spring.datasource.driver-class-name",
              () -> "org.postgresql.Driver");               // 🔑 evita lookup H2
        r.add("spring.jpa.hibernate.ddl-auto",    () -> "create-drop");
        r.add("spring.flyway.enabled",            () -> "false");       // nessuna migrazione
    }

    /* ------------------------------------------------------------------ */
    @Autowired MockMvc      mvc;
    @Autowired ObjectMapper mapper;

    /* ISBN-13 random 978-xxxxxxxxx-c ----------------------------------- */
    private static String isbn13() {
        long body = 978_000_000_000L + ThreadLocalRandom.current().nextLong(1_000_000_000L);
        String t   = Long.toString(body);
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int d = t.charAt(i) - '0';
            sum += (i % 2 == 0) ? d : d * 3;
        }
        return t + (10 - sum % 10) % 10;
    }

    /* ------------------------------------------------------------------ */
    @Test
    void concurrentOrdersDepleteStock() throws Exception {

        /* 1️⃣  libro stock 5 ------------------------------------------ */
        BookDto b = new BookDto();
        b.setTitle("Concurrency-Book");
        b.setAuthor("Tester");
        b.setGenre("Tech");
        b.setPublicationYear(2025);
        b.setIsbn(isbn13());
        b.setPrice(new BigDecimal("9.99"));
        b.setStockQuantity(5);

        UUID bookId = mapper.readValue(
                mvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(b)))
                   .andExpect(status().isCreated())
                   .andReturn().getResponse().getContentAsString(),
                BookDto.class).getId();

        /* 2️⃣  20 thread --------------------------------------------- */
        ExecutorService pool = Executors.newFixedThreadPool(20);
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger ko = new AtomicInteger();

        IntStream.range(0, 20).forEach(i -> pool.submit(() -> {
            try {
                long orderId = createOrder(bookId, i);
                int http = mvc.perform(
                                put("/orders/{id}/mark-paid", orderId)
                                        .param("gatewayRef", "PAY-" + i))
                              .andReturn().getResponse().getStatus();
                if (http == 204) ok.incrementAndGet();
                else             ko.incrementAndGet();
            } catch (Exception e) {
                ko.incrementAndGet();
            }
        }));

        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        /* 3️⃣  asserzioni finali -------------------------------------- */
        assertThat(ok.get()).isEqualTo(5);
        assertThat(ko.get()).isEqualTo(15);

        int remaining = mapper.readValue(
                mvc.perform(get("/books/{id}", bookId))
                   .andExpect(status().isOk())
                   .andReturn().getResponse().getContentAsString(),
                BookDto.class).getStockQuantity();

        assertThat(remaining).isZero();
    }

    /* helper ----------------------------------------------------------- */
    private long createOrder(UUID bookId, int idx) throws Exception {
        OrderDto dto = OrderDto.builder()
                .bookId(bookId)
                .patronId((long) idx)
                .quantity(1)
                .build();

        return mapper.readValue(
                mvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                   .andExpect(status().isCreated())
                   .andReturn().getResponse().getContentAsString(),
                OrderDto.class).getId();
    }
}
