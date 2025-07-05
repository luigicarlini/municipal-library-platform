package it.comune.library.reservation.controller;

import it.comune.library.reservation.config.PostgresTestContainer;
import it.comune.library.reservation.config.TxManagerAliasTestConfig;
import it.comune.library.reservation.service.EventPublisher; // <<—
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // <<—
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.PlatformTransactionManager;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import({PostgresTestContainer.class, TxManagerAliasTestConfig.class})
@Sql("/sql/hold_return_dataset.sql")
class HoldReturnIT {

    @MockBean EventPublisher eventPublisher;   // ok così

    @Autowired MockMvc mvc;

    @Test
    void markReturn_flowOk() throws Exception {
        UUID id = UUID.fromString("44444444-4444-4444-4444-444444444444");

        mvc.perform(put("/holds/{id}/return", id))
            .andExpect(status().isNoContent());

        mvc.perform(get("/holds").param("status", "READY"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "0"));
    }
}
