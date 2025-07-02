package it.comune.library.reservation.controller;

import it.comune.library.reservation.domain.HoldStatus;
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

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Sql("/sql/soft_delete_cascade_dataset.sql")
class BookSoftDeleteCascadeIT {

    private static final UUID BOOK_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired MockMvc mvc;
    @Autowired HoldRepository holdRepo;

    @Test
    void softDeleteBook_cascadesHoldCancelled() throws Exception {

        /* 1) soft-delete del libro */
        mvc.perform(delete("/books/{id}", BOOK_ID))
           .andExpect(status().isNoContent());

        /* 2) nel DB risultano 3 hold CANCELLED */
        long cancelled = holdRepo
                .countByStatusAndBookId(HoldStatus.CANCELLED, BOOK_ID);
        assertThat(cancelled).isEqualTo(3);

        /* 3) /holds?status=CANCELLED non mostra più quelle hold */
        mvc.perform(get("/holds").param("status", "CANCELLED"))
           .andExpect(status().isOk())
           .andExpect(header().string("X-Total-Count", "0"))
           .andExpect(jsonPath("$.length()").value(0));
    }
}