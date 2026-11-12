package com.lexhive.booklender.service;

import com.lexhive.booklender.config.BorrowingProperties;
import com.lexhive.booklender.domain.Book;
import com.lexhive.booklender.domain.Loan;
import com.lexhive.booklender.dto.BorrowRequest;
import com.lexhive.booklender.repository.BookRepository;
import com.lexhive.booklender.repository.LoanRepository;
import com.lexhive.booklender.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowingProperties borrowingProperties;
    private final Clock clock;

    @Autowired
    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       MemberRepository memberRepository,
                       BorrowingProperties borrowingProperties) {
        this(loanRepository, bookRepository, memberRepository, borrowingProperties, Clock.systemUTC());
    }

    LoanService(LoanRepository loanRepository,
                BookRepository bookRepository,
                MemberRepository memberRepository,
                BorrowingProperties borrowingProperties,
                Clock clock) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.borrowingProperties = borrowingProperties;
        this.clock = clock;
    }

    public List<Loan> getLoans() {
        return loanRepository.findAll();
    }

    public Loan getLoan(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
    }

    @Transactional
    public Loan borrow(BorrowRequest request) {
        if (memberRepository.findByIdForUpdate(request.memberId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }
        Book book = bookRepository.findByIdForUpdate(request.bookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        if (book.getAvailableCopies() < 1) {
            log.warn("Borrow rejected for book {}: no copies are available", book.getId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No copies of this book are available");
        }

        long activeLoans = loanRepository.countByMemberIdAndReturnedAtIsNull(request.memberId());
        if (activeLoans >= borrowingProperties.maxActiveLoans()) {
            log.warn("Borrow rejected for member {}: {} active loans", request.memberId(), activeLoans);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Member has reached the maximum number of active loans");
        }

        LocalDateTime borrowedAt = LocalDateTime.now(clock);
        if (loanRepository.existsByMemberIdAndReturnedAtIsNullAndDueDateBefore(request.memberId(), borrowedAt)) {
            log.warn("Borrow rejected for member {}: overdue loan", request.memberId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Member has an overdue loan and cannot borrow");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        Loan loan = loanRepository.save(new Loan(
                null,
                book.getId(),
                request.memberId(),
                borrowedAt,
                borrowedAt.plusDays(borrowingProperties.loanDurationDays()),
                null));
        log.info("Member {} borrowed book {} as loan {} due {}", request.memberId(), book.getId(), loan.getId(), loan.getDueDate());
        return loan;
    }

    @Transactional
    public Loan returnLoan(Long id) {
        Loan existing = getLoan(id);
        if (memberRepository.findByIdForUpdate(existing.getMemberId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }
        Book book = bookRepository.findByIdForUpdate(existing.getBookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        Loan loan = loanRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
        if (loan.getReturnedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Loan is already returned");
        }
        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "All copies of this book are already marked available");
        }
        loan.setReturnedAt(LocalDateTime.now(clock));
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        log.info("Returned loan {} for book {}", loan.getId(), book.getId());
        return loan;
    }
}
