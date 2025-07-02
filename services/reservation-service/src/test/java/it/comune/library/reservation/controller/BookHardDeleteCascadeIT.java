package it.comune.library.reservation.controller;

import it.comune.library.reservation.repository.BookRepository;
import it.comune.library.reservation.repository.HoldRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import lombok.extern.slf4j.Slf4j;   // ⭐

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Sql("/sql/hard_delete_cascade_dataset.sql")
@Slf4j                                 // ⭐ abilita log
class BookHardDeleteCascadeIT {

    private static final UUID BOOK_ID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired MockMvc         mvc;
    @Autowired BookRepository  bookRepo;
    @Autowired HoldRepository  holdRepo;

    @Test
    void hardDeleteBook_cascadesHoldRemoval() throws Exception {

        /* pre-condizione */
        log.info("Rows prima del delete = {}", holdRepo.countByBibId(BOOK_ID));

        /* hard-delete via REST */
        mvc.perform(delete("/books/{id}?mode=hard", BOOK_ID))
           .andExpect(status().isNoContent());

        /* post-condizione */
        log.info("Rows dopo  delete    = {}", holdRepo.countByBibId(BOOK_ID));

        assertThat(bookRepo.existsById(BOOK_ID)).isFalse();
        assertThat(holdRepo.countByBibId(BOOK_ID)).isZero();

        /* endpoint /holds non mostra più record */
        mvc.perform(get("/holds").param("bibId", BOOK_ID.toString()))
           .andExpect(status().isOk())
           .andExpect(header().string("X-Total-Count", "0"))
           .andExpect(jsonPath("$.length()").value(0));
    }
}
