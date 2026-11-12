package com.lexhive.booklender.controller;

import com.lexhive.booklender.domain.Loan;
import com.lexhive.booklender.dto.BorrowRequest;
import com.lexhive.booklender.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/loans")
@Tag(name = " Loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    @Operation(summary = "List  loans")
    public List<Loan> getLoans() {
        return loanService.getLoans();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a loan by the id")
    public Loan getLoan( @PathVariable Long id) {
        return loanService.getLoan(id);
    }

    @PostMapping
    @Operation(summary = "Borrow a book for a member")
    public ResponseEntity<Loan> borrow(@Valid @RequestBody BorrowRequest request) {
        Loan loan = loanService.borrow(request);
        return ResponseEntity.created(URI.create("/loans/" + loan.getId())).body(loan);
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Return a borrowed book")
    public Loan returnLoan(@PathVariable Long id) {
        return loanService.returnLoan(id);
    }
}
