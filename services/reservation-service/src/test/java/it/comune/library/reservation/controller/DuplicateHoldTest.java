package it.comune.library.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.comune.library.reservation.ReservationServiceApplication;
import it.comune.library.reservation.dto.BookDto;
import it.comune.library.reservation.dto.HoldDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifica che una seconda prenotazione (Hold) - stesso patron + stesso libro -
 * sia respinta con 409 Conflict.
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
class DuplicateHoldTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    private UUID bookId;
    private final UUID patronId = UUID.randomUUID();

    /** genera sempre un ISBN-13 valido che inizia con 978 */
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
    void setUp() throws Exception {
        /* crea un libro “vivo” da prenotare */
        BookDto book = new BookDto();
        book.setTitle("Duplicate-Book");
        book.setAuthor("Tester");
        book.setGenre("Test");
        book.setPublicationYear(2025);
        book.setIsbn(randomValidIsbn13());
        book.setPrice(new BigDecimal("15.00"));
        book.setStockQuantity(2);
        book.setDeleted(false);

        String json = mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(book)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        bookId = mapper.readValue(json, BookDto.class).getId();
    }

    @Test
    void duplicateHoldReturns409() throws Exception {
        /* DTO comune alle due chiamate */
        HoldDto hold = new HoldDto();
        hold.setPatronId(patronId);
        hold.setBibId(bookId);
        hold.setPickupBranch("CENTRALE");

        /* prima prenotazione → 200 OK */
        mockMvc.perform(post("/holds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(hold)))
                .andExpect(status().isOk());

        /* seconda prenotazione identica → 409 Conflict */
        mockMvc.perform(post("/holds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(hold)))
                .andExpect(status().isConflict());
    }
}
