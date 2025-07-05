package it.comune.library.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.comune.library.reservation.ReservationServiceApplication;
import it.comune.library.reservation.dto.BookDto;
import it.comune.library.reservation.dto.HoldDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate; // 🔹 nuovo import
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifica che:
 * • la ricerca paginata su /holds ritorni X-Total-Count
 * • il filtro title funzioni
 * • la lunghezza dell’array JSON corrisponda alla size richiesta
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
class HoldPaginationTest {

        @Autowired
        MockMvc mockMvc;
        @Autowired
        ObjectMapper mapper;

        /** ISBN-13 random valido (prefisso 978) */
        private static String randomIsbn13() {
                long body = 978_000_000_000L +
                                ThreadLocalRandom.current().nextLong(1_000_000_000L);
                String twelve = Long.toString(body);
                int sum = 0;
                for (int i = 0; i < 12; i++) {
                        int digit = twelve.charAt(i) - '0';
                        sum += (i % 2 == 0) ? digit : digit * 3;
                }
                int check = (10 - sum % 10) % 10;
                return twelve + check;
        }

        @Test
        void paginationWithTitleFilterReturnsTotalCountHeader() throws Exception {

                /* 1 ─ crea il libro “The Great Gatsby” */
                BookDto book = new BookDto();
                book.setTitle("The Great Gatsby");
                book.setAuthor("F. S. Fitzgerald");
                book.setGenre("Classics");
                book.setPublicationYear(1925);
                book.setIsbn(randomIsbn13());
                book.setPrice(new BigDecimal("11.00"));
                book.setStockQuantity(3);

                String createdBookJson = mockMvc.perform(post("/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(book)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                UUID bookId = mapper.readValue(createdBookJson, BookDto.class).getId();

                /* 2 ─ crea una prenotazione (Hold) per il libro */
                HoldDto hold = new HoldDto();
                hold.setBibId(bookId);
                hold.setPatronId(UUID.randomUUID());
                hold.setPickupBranch("Main");
                hold.setDueDate(LocalDate.now().plusDays(7)); // ← evita NOT-NULL

                mockMvc.perform(post("/holds")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(hold)))
                                .andExpect(status().isOk());

                /* 3 ─ ricerca paginata: filtro per titolo */
                mockMvc.perform(get("/holds")
                                .param("title", "Gatsby")
                                .param("size", "5"))
                                .andExpect(status().isOk())
                                .andExpect(header().string("X-Total-Count", "1"))
                                .andExpect(jsonPath("$.length()").value(1));
        }
}
