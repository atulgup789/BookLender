package com.lexhive.booklender.service;

import com.lexhive.booklender.domain.Book;
import com.lexhive.booklender.dto.BookRequest;
import com.lexhive.booklender.repository.BookRepository;
import com.lexhive.booklender.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public BookService(BookRepository bookRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
    }

    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    public Book getBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }

    @Transactional
    public Book createBook(BookRequest request) {
        if (bookRepository.findByIsbn(request.isbn()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A book with this ISBN already exists");
        }
        Book book = new Book(
                null,
                request.title(),
                request.author(),
                request.isbn(),
                request.totalCopies(),
                request.totalCopies());
        Book saved = bookRepository.save(book);
        log.info("Created book {} ({})", saved.getId(), saved.getIsbn());
        return saved;
    }

    @Transactional
    public Book updateBook(Long id, BookRequest request) {
        Book book = getBook(id);
        if (bookRepository.existsByIsbnAndIdNot(request.isbn(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A book with this ISBN already exists");
        }
        int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
        if (request.totalCopies() < borrowedCopies) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "totalCopies cannot be less than the number of copies currently on loan");
        }
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.totalCopies() - borrowedCopies);
        Book saved = bookRepository.save(book);
        log.info("Updated book {}", saved.getId());
        return saved;
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
        if (loanRepository.existsByBookId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Book has loans and cannot be deleted");
        }
        bookRepository.deleteById(id);
        log.info("Deleted book {}", id);
    }
}
