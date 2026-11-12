CREATE TABLE books (
    id               BIGSERIAL    PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    author           VARCHAR(255) NOT NULL,
    isbn             VARCHAR(255) NOT NULL,
    total_copies     INTEGER      NOT NULL,
    available_copies INTEGER      NOT NULL,
    CONSTRAINT uq_books_isbn UNIQUE (isbn),
    CONSTRAINT ck_books_total_copies_non_negative CHECK (total_copies >= 0),
    CONSTRAINT ck_books_available_copies_non_negative CHECK (available_copies >= 0),
    CONSTRAINT ck_books_available_not_above_total CHECK (available_copies <= total_copies)
);

CREATE TABLE members (
    id    BIGSERIAL    PRIMARY KEY,
    name  VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    CONSTRAINT uq_members_email UNIQUE (email)
);

CREATE TABLE loans (
    id          BIGSERIAL    PRIMARY KEY,
    book_id     BIGINT       NOT NULL,
    member_id   BIGINT       NOT NULL,
    borrowed_at TIMESTAMP(6) NOT NULL,
    due_date    TIMESTAMP(6) NOT NULL,
    returned_at TIMESTAMP(6),
    CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_loans_member FOREIGN KEY (member_id) REFERENCES members (id)
);

CREATE INDEX idx_loans_book_id ON loans (book_id);
CREATE INDEX idx_loans_member_id ON loans (member_id);
