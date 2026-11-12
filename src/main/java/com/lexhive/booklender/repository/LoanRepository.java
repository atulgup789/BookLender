package com.lexhive.booklender.repository;

import com.lexhive.booklender.domain.Loan;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Loan l where l.id = :id")
    Optional<Loan> findByIdForUpdate(@Param("id") Long id);

    boolean existsByBookId(Long bookId);

    boolean existsByMemberId(Long memberId);

    long countByMemberIdAndReturnedAtIsNull(Long memberId);

    boolean existsByMemberIdAndReturnedAtIsNullAndDueDateBefore(Long memberId, LocalDateTime dueDate);
}
