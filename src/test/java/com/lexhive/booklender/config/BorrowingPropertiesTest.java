package com.lexhive.booklender.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BorrowingPropertiesTest {

    @Test
    void keepsTheConfiguredLimits() {
        BorrowingProperties properties = new BorrowingProperties(3, 14);

        assertThat(properties.maxActiveLoans()).isEqualTo(3);
        assertThat(properties.loanDurationDays()).isEqualTo(14);
    }

    @Test
    void rejectsALimitBelowOne() {
        assertThatThrownBy(() -> new BorrowingProperties(0, 14))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
