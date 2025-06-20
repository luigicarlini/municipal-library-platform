package it.comune.library.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.comune.library.reservation.ReservationServiceApplication;
import it.comune.library.reservation.dto.BookDto;
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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifica OCC via REST:
 * 1) due client leggono la stessa versione,
 * 2) il primo aggiorna con successo,
 * 3) il secondo riceve 409 Conflict.
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
class BookControllerConcurrencyTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;

    /** Restituisce sempre un ISBN-13 valido che inizia per 978 */
    private static String randomValidIsbn13() {
        long body = 978_000_000_000L + // prefisso 978
                ThreadLocalRandom.current().nextLong(1_000_000_000L); // altre 9 cifre
        String twelve = Long.toString(body); // 12 cifre

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = twelve.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int check = (10 - sum % 10) % 10;
        return twelve + check; // 13-cifre corretto
    }

    @Test
    void testOptimisticLockingWithMockMvc() throws Exception {

        /* 1 – crea libro -------------------------------------------------- */
        BookDto dto = new BookDto();
        dto.setTitle("OCC-Book");
        dto.setAuthor("Author A");
        dto.setGenre("Fiction");
        dto.setPublicationYear(2024);
        dto.setIsbn(randomValidIsbn13());
        dto.setPrice(new BigDecimal("25.00"));
        dto.setStockQuantity(5);
        dto.setDeleted(false);

        String createdJson = mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .accept("application/hal+json")
                .content(mapper.writeValueAsString(dto)))
                .andDo(print()) // stampa l’intero MvcResult
                .andExpect(status().isCreated()) // 201
                .andReturn()
                .getResponse()
                .getContentAsString();

        BookDto created = mapper.readValue(createdJson, BookDto.class);
        UUID bookId = created.getId();

        /* 2 – due client leggono la stessa versione ----------------------- */
        BookDto clientA = getBook(bookId);
        BookDto clientB = getBook(bookId);

        /* 3 – client A aggiorna con successo ------------------------------ */
        clientA.setTitle("Updated by A");

        mockMvc.perform(put("/books/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept("application/hal+json")
                .content(mapper.writeValueAsString(clientA)))
                .andExpect(status().isOk());

        /* 4 – client B tenta con versione vecchia → 409 ------------------- */
        clientB.setTitle("Updated by B");

        mockMvc.perform(put("/books/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept("application/hal+json")
                .content(mapper.writeValueAsString(clientB)))
                .andExpect(status().isConflict()) // 409
                .andExpect(jsonPath("$.message",
                        containsString("Optimistic"))); // messaggio OCC
    }

    /* GET helper */
    private BookDto getBook(UUID id) throws Exception {
        String json = mockMvc.perform(get("/books/{id}", id)
                .accept("application/hal+json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return mapper.readValue(json, BookDto.class);
    }
}