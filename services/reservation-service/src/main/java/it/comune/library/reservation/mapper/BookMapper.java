package it.comune.library.reservation.mapper;

import it.comune.library.reservation.domain.Book;
import it.comune.library.reservation.dto.BookDto;
import org.springframework.stereotype.Component;

/**
 * 🔁 Mapper tra l'entità {@link Book} e il suo DTO {@link BookDto}.
 */
@Component
public class BookMapper {

    /* ===== Entity ➜ DTO ===== */

    public BookDto toDto(Book book) {
        if (book == null) {
            return null;
        }
        BookDto dto = new BookDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setGenre(book.getGenre());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setIsbn(book.getIsbn());

        // campi Book-shop
        dto.setPrice(book.getPrice());
        dto.setStockQuantity(book.getStockQuantity());

        return dto;
    }

    /* ===== DTO ➜ Entity (nuova entità) ===== */

    public Book toEntity(BookDto dto) {
        if (dto == null) {
            return null;
        }
        Book book = new Book();
        book.setId(dto.getId());
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setGenre(dto.getGenre());
        book.setPublicationYear(dto.getPublicationYear());
        book.setIsbn(dto.getIsbn());

        // campi Book-shop
        book.setPrice(dto.getPrice());
        book.setStockQuantity(dto.getStockQuantity());

        return book;
    }

    /* ===== Merge DTO ➜ Entity (update) ===== */

    /**
     * Copia nei campi mutabili dell'entità gestita ({@code target})
     * i valori presenti nel DTO ({@code source}).
     * <p>Usato negli update per preservare <i>id</i>, <i>version</i>
     * e lo stato di persistenza già noto a JPA.</p>
     */
    public void updateEntity(Book target, BookDto source) {
        if (target == null || source == null) {
            return;
        }
        target.setTitle(source.getTitle());
        target.setAuthor(source.getAuthor());
        target.setGenre(source.getGenre());
        target.setPublicationYear(source.getPublicationYear());
        target.setIsbn(source.getIsbn());
        target.setPrice(source.getPrice());
        target.setStockQuantity(source.getStockQuantity());
    }
}