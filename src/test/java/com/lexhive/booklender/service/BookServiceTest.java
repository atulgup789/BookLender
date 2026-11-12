package com.lexhive.booklender.service;

import com.lexhive.booklender.domain.Book;
import com.lexhive.booklender.dto.BookRequest;
import com.lexhive.booklender.repository.BookRepository;
import com.lexhive.booklender.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanRepository loanRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository, loanRepository);
    }

    @Test
    void createBookStartsWithEveryCopyAvailable() {
        when(bookRepository.findByIsbn("9780441172719")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book created = bookService.createBook(new BookRequest(" Dune ", " Frank Herbert ", " 9780441172719 ", 3));

        assertThat(created.getTitle()).isEqualTo("Dune");
        assertThat(created.getIsbn()).isEqualTo("9780441172719");
        assertThat(created.getTotalCopies()).isEqualTo(3);
        assertThat(created.getAvailableCopies()).isEqualTo(3);
    }

    @Test
    void updateBookKeepsCopiesThatAreOnLoan() {
        Book book = new Book(1L, "Dune", "Frank Herbert", "9780441172719", 3, 1);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbnAndIdNot("9780441172719", 1L)).thenReturn(false);
        when(bookRepository.save(book)).thenReturn(book);

        Book updated = bookService.updateBook(1L, new BookRequest("Dune", "Frank Herbert", "9780441172719", 4));

        assertThat(updated.getTotalCopies()).isEqualTo(4);
        assertThat(updated.getAvailableCopies()).isEqualTo(2);
    }

    @Test
    void deleteBookIsRejectedWhenTheBookHasLoans() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        when(loanRepository.existsByBookId(1L)).thenReturn(true);

        assertThatThrownBy(() -> bookService.deleteBook(1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        verify(bookRepository, never()).deleteById(1L);
    }
}
