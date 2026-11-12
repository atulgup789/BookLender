package com.lexhive.booklender.service;

import com.lexhive.booklender.config.BorrowingProperties;
import com.lexhive.booklender.domain.Book;
import com.lexhive.booklender.domain.Loan;
import com.lexhive.booklender.domain.Member;
import com.lexhive.booklender.dto.BorrowRequest;
import com.lexhive.booklender.repository.BookRepository;
import com.lexhive.booklender.repository.LoanRepository;
import com.lexhive.booklender.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 12, 10, 0);

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    private LoanService loanService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-12T10:00:00Z"), ZoneOffset.UTC);
        loanService = new LoanService(
                loanRepository,
                bookRepository,
                memberRepository,
                new BorrowingProperties(3, 14),
                clock);
    }

    @Test
    void borrowSetsTheDueDateAndTakesOneCopy() {
        Book book = new Book(5L, "Dune", "Frank Herbert", "9780441172719", 2, 2);
        when(memberRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(new Member(8L, "Ada", "ada@example.com")));
        when(bookRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(book));
        when(loanRepository.countByMemberIdAndReturnedAtIsNull(8L)).thenReturn(0L);
        when(loanRepository.existsByMemberIdAndReturnedAtIsNullAndDueDateBefore(8L, NOW)).thenReturn(false);
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            loan.setId(20L);
            return loan;
        });

        Loan loan = loanService.borrow(new BorrowRequest(5L, 8L));

        assertThat(loan.getId()).isEqualTo(20L);
        assertThat(loan.getBorrowedAt()).isEqualTo(NOW);
        assertThat(loan.getDueDate()).isEqualTo(NOW.plusDays(14));
        assertThat(loan.getReturnedAt()).isNull();
        assertThat(book.getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void borrowIsRejectedWhenTheMemberAlreadyHasTheMaximumActiveLoans() {
        Book book = new Book(5L, "Dune", "Frank Herbert", "9780441172719", 2, 2);
        when(memberRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(new Member(8L, "Ada", "ada@example.com")));
        when(bookRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(book));
        when(loanRepository.countByMemberIdAndReturnedAtIsNull(8L)).thenReturn(3L);

        assertThatThrownBy(() -> loanService.borrow(new BorrowRequest(5L, 8L)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(book.getAvailableCopies()).isEqualTo(2);
    }

    @Test
    void returnLoanMarksTheLoanReturnedAndPutsTheCopyBack() {
        Loan loan = new Loan(20L, 5L, 8L, NOW.minusDays(2), NOW.plusDays(12), null);
        Book book = new Book(5L, "Dune", "Frank Herbert", "9780441172719", 2, 1);
        when(loanRepository.findById(20L)).thenReturn(Optional.of(loan));
        when(memberRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(new Member(8L, "Ada", "ada@example.com")));
        when(bookRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(book));
        when(loanRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(loan));

        Loan returned = loanService.returnLoan(20L);

        assertThat(returned.getReturnedAt()).isEqualTo(NOW);
        assertThat(book.getAvailableCopies()).isEqualTo(2);
    }
}
