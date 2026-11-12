package com.lexhive.booklender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "booklender.borrowing")
public record BorrowingProperties(int maxActiveLoans, int loanDurationDays) {

    public BorrowingProperties {
        if (maxActiveLoans < 1) {
            throw new IllegalArgumentException("booklender.borrowing.max-active-loans must be at least 1");
        }
        if (loanDurationDays < 1) {
            throw new IllegalArgumentException("booklender.borrowing.loan-duration-days must be at least 1");
        }
    }
}
