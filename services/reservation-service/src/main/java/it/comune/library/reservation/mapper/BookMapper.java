package it.comune.library.reservation.mapper;

import it.comune.library.reservation.domain.Book;
import it.comune.library.reservation.dto.BookDto;
import org.springframework.stereotype.Component;

/**
 * 🔁 Mapper tra l'entità Book e il suo DTO BookDto.
 */
@Component
public class BookMapper {

    public BookDto toDto(Book book) {
        BookDto dto = new BookDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setGenre(book.getGenre());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setIsbn(book.getIsbn()); // ✅ << AGGIUNGI QUESTA RIGA
        return dto;
    }

    public Book toEntity(BookDto dto) {
        Book book = new Book();
        book.setId(dto.getId());
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setGenre(dto.getGenre());
        book.setPublicationYear(dto.getPublicationYear());
        book.setIsbn(dto.getIsbn()); // ✅ << AGGIUNGI QUESTA RIGA
        return book;
    }
}

