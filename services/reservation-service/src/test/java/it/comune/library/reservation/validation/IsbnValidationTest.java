package it.comune.library.reservation.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.comune.library.reservation.ReservationServiceApplication;
import it.comune.library.reservation.dto.BookDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Parametric test: ISBN non validi → 400 Bad Request.
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
class IsbnValidationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @ParameterizedTest
    @ValueSource(strings = {
            "123",                        // troppo corto
            "978ABCDEF0123",              // caratteri non numerici
            "9780000000000"               // checksum errato
    })
    void invalidIsbnReturns400(String badIsbn) throws Exception {
        BookDto dto = new BookDto();
        dto.setTitle("Bad ISBN");
        dto.setAuthor("A");
        dto.setGenre("Test");
        dto.setPublicationYear(2025);
        dto.setIsbn(badIsbn);
        dto.setPrice(BigDecimal.TEN);
        dto.setStockQuantity(1);
        dto.setDeleted(false);

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
               .andExpect(status().isBadRequest());
    }
}
