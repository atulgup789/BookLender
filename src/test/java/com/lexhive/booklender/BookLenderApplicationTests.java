package com.lexhive.booklender;

import com.lexhive.booklender.domain.Book;
import com.lexhive.booklender.domain.Loan;
import com.lexhive.booklender.domain.Member;
import com.lexhive.booklender.repository.BookRepository;
import com.lexhive.booklender.repository.LoanRepository;
import com.lexhive.booklender.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BookLenderApplicationTests {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void persistsBookMemberAndLoan() {
        Book book = bookRepository.save(new Book(null, "Dune", "Frank Herbert", "978-0-441-17271-9", 3, 3));
        Member member = memberRepository.save(new Member(null, "Ada Lovelace", "ada@example.com"));
        LocalDateTime borrowedAt = LocalDateTime.of(2026, 11, 7, 10, 0);

        Loan loan = loanRepository.save(new Loan(
                null,
                book.getId(),
                member.getId(),
                borrowedAt,
                borrowedAt.plusDays(14),
                null));

        Loan stored = loanRepository.findById(loan.getId()).orElseThrow();
        assertThat(stored.getBookId()).isEqualTo(book.getId());
        assertThat(stored.getMemberId()).isEqualTo(member.getId());
        assertThat(stored.getBorrowedAt()).isEqualTo(borrowedAt);
        assertThat(stored.getDueDate()).isEqualTo(borrowedAt.plusDays(14));
        assertThat(stored.getReturnedAt()).isNull();
    }

    @Test
    void rejectsLoanWhenBookDoesNotExist() {
        Member member = memberRepository.save(new Member(null, "Grace Hopper", "grace@example.com"));
        LocalDateTime borrowedAt = LocalDateTime.of(2026, 11, 7, 11, 0);

        assertThatThrownBy(() -> loanRepository.saveAndFlush(new Loan(
                null,
                999_999L,
                member.getId(),
                borrowedAt,
                borrowedAt.plusDays(14),
                null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
