package com.lexhive.booklender.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record BookRequest(
        @NotBlank(message = "title is required") String title,
        @NotBlank(message = "author is required") String author,
        @NotBlank(message = "isbn is required") String isbn,
        @PositiveOrZero(message = "totalCopies cannot be negative") int totalCopies) {

    public BookRequest {
        title = trim(title);
        author = trim(author);
        isbn = trim(isbn);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
