package com.lexhive.booklender.dto;

import jakarta.validation.constraints.NotNull;

public record BorrowRequest(
        @NotNull(message ="bookId is required") Long bookId,
        @NotNull(message = "memberId is required") Long memberId) {
}
