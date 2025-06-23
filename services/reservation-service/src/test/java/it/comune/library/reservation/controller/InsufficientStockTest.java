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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica che un ordine con quantity > stockQuantity
 * venga respinto con 409 Conflict.
 */
@SpringBootTest(classes = ReservationServiceApplication.class, properties = {
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.flyway.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class InsufficientStockTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    private UUID bookId;

    /** ISBN-13 valido che inizia per 978 */
    private static String randomValidIsbn13() {
        long base = 978_000_000_000L +
                ThreadLocalRandom.current().nextLong(1_000_000_000L);
        String twelve = Long.toString(base);
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = twelve.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int check = (10 - sum % 10) % 10;
        return twelve + check;
    }

    @BeforeEach
    void createBook() throws Exception {
        BookDto dto = new BookDto();
        dto.setTitle("Low-Stock");
        dto.setAuthor("Shop");
        dto.setGenre("Test");
        dto.setPublicationYear(2025);
        dto.setIsbn(randomValidIsbn13());
        dto.setPrice(new BigDecimal("15.00"));
        dto.setStockQuantity(2);            // stock = 2
        dto.setDeleted(false);

        String json = mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        bookId = mapper.readValue(json, BookDto.class).getId();
    }

    @Test
    void orderAboveStockIsRejected() throws Exception {
        OrderDto order = new OrderDto();
        order.setBookId(bookId);
        order.setPatronId(1L);
        order.setQuantity(3);               // > stockQuantity (2)

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(order)))
               .andExpect(status().isConflict());   // 409 da RestExceptionAdvice
    }
}
