// src/test/java/it/comune/library/reservation/controller/MarkPaidHappyPathTest.java
package it.comune.library.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.comune.library.reservation.ReservationServiceApplication;
import it.comune.library.reservation.dto.BookDto;
import it.comune.library.reservation.dto.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** End-to-end: CREATED → PAID + stock-1 */
@SpringBootTest(
        classes = ReservationServiceApplication.class,
        properties = {
                // se stai disabilitando Flyway:
                "spring.flyway.enabled=false",
                "spring.jpa.hibernate.ddl-auto=create",

                // 👇 forza il driver corretto
                "spring.datasource.driver-class-name=org.postgresql.Driver"
        }
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")   // ok, le override precedenti vincono
class MarkPaidHappyPathTest {

    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private UUID bookId;
    private Long  orderId;
    private final int initialStock = 5;
    private final int qty = 1;

    private static String isbn() {
        long body = 978_000_000_000L + ThreadLocalRandom.current().nextLong(1_000_000_000L);
        // checksum
        String twelve = Long.toString(body);
        int sum=0; for (int i=0;i<12;i++){int d=twelve.charAt(i)-'0'; sum+=(i%2==0)?d:d*3;}
        return twelve + (10 - sum%10)%10;
    }

    @BeforeEach
    void setup() throws Exception {
        // 1) crea libro
        BookDto b = new BookDto();
        b.setTitle("Stock-Book");
        b.setAuthor("Shop");
        b.setGenre("Test");
        b.setPublicationYear(2025);
        b.setIsbn(isbn());
        b.setPrice(new BigDecimal("10.00"));
        b.setStockQuantity(initialStock);

        String bookJson = mvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(b)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        bookId = mapper.readValue(bookJson, BookDto.class).getId();

        // 2) crea ordine CREATED
        OrderDto o = new OrderDto();
        o.setBookId(bookId);
        o.setPatronId(1L);
        o.setQuantity(qty);

        String orderJson = mvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(o)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        orderId = mapper.readTree(orderJson).get("id").asLong();
    }

    @Test
    void markPaidHappyPath() throws Exception {
        // 3) mark-paid → 204
        mvc.perform(put("/orders/{id}/mark-paid", orderId)
                .param("gatewayRef", "PAY-OK"))
           .andExpect(status().isNoContent());

        // 4) verifica stato ordine = PAID
        mvc.perform(get("/orders/{id}", orderId))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("PAID"));

        // 5) verifica stock = initial - qty
        String bookJson = mvc.perform(get("/books/{id}", bookId))
                             .andReturn().getResponse().getContentAsString();
        int stockNow = mapper.readTree(bookJson).get("stockQuantity").asInt();
        assertThat(stockNow).isEqualTo(initialStock - qty);
    }
}
