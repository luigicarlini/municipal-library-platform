package it.comune.library.reservation.service;

import it.comune.library.reservation.domain.Book;
import it.comune.library.reservation.repository.BookRepository;
import it.comune.library.reservation.repository.HoldRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final HoldRepository holdRepository;

    /**
     * Soft-delete: ➊ flagga il libro, ➋ annulla le hold *PLACED* collegate.
     */
    @Transactional
    public void softDeleteBook(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                                  .orElseThrow(() -> new EntityNotFoundException("Book " + bookId));

        if (book.isDeleted()) {          // già cancellato → noop idempotente
            return;
        }

        book.setDeleted(true);           // ➊ flag
        holdRepository.cancelActiveHoldsForBook(bookId);   // ➋ bulk update → CANCELLED (nuovo metodo)
    }
}
