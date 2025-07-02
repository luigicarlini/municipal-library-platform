package it.comune.library.reservation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers          // fa partire/fermare il container in automatico
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class HoldPaginationIT {

    @Container            // un solo container per l’intera classe
    static final PostgreSQLContainer<?> PG =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("postgres");

    /** Inietta l’URL prima che Spring costruisca il DataSource */
    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",      PG::getJdbcUrl);
        r.add("spring.datasource.username", PG::getUsername);
        r.add("spring.datasource.password", PG::getPassword);
    }

    @org.springframework.beans.factory.annotation.Autowired
    MockMvc mvc;

    @Test
    @Sql(scripts = "/sql/holds_pagination_dataset.sql",
         executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void paginatedFilteredHolds() throws Exception {

        var res = mvc.perform(get("/holds")
                     .param("status", "PLACED")
                     .param("page",   "0")
                     .param("size",   "10")
                     .accept("application/json"))
                     .andExpect(status().isOk())
                     .andExpect(header().string("X-Total-Count", matchesRegex("\\d+")))
                     .andExpect(jsonPath("$.length()").value(10))
                     .andExpect(jsonPath("$[*].status", everyItem(is("PLACED"))))
                     .andReturn();

        assertThat(Integer.parseInt(res.getResponse()
                                       .getHeader("X-Total-Count")))
                .isGreaterThanOrEqualTo(1);
    }
}
