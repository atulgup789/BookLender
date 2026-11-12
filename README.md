# BookLender

A book lending API. Data is stored in an H2 file database, so nothing else needs to be installed.

## Run

Requires Java 21.

```bash
mvn spring-boot:run
```

The app runs on http://localhost:8220. Data is saved in `data/booklender.mv.db` and is kept when the app stops.

## Users

| Username | Password | Can do |
|---|---|---|
| `admin` | `admin` | Everything, including delete |
| `librarian` | `librarian` | Add and update books and members. Borrow and return |
| `member` | `member` | Read. Borrow and return |

Use HTTP Basic auth. In a browser, open http://localhost:8220/books and enter a username and password. In Postman, use the **Basic Auth** tab. With curl:

```bash
curl -u admin:admin http://localhost:8220/books
```

## Endpoints

| Method | Path | Who |
|---|---|---|
| GET | `/books`, `/books/{id}` | any user |
| POST | `/books` | admin, librarian |
| PUT | `/books/{id}` | admin, librarian |
| DELETE | `/books/{id}` | admin |
| GET | `/members`, `/members/{id}` | any user |
| POST | `/members` | admin, librarian |
| PUT | `/members/{id}` | admin, librarian |
| DELETE | `/members/{id}` | admin |
| GET | `/loans`, `/loans/{id}` | any user |
| POST | `/loans` | any user (borrow) |
| POST | `/loans/{id}/return` | any user |

Example body for `POST /books`:

```json
{
  "title": "Dune",
  "author": "Frank Herbert",
  "isbn": "9780441172719",
  "totalCopies": 3
}
```

Members take `name` and `email`. Borrow takes `bookId` and `memberId`.

## Borrowing rules

- A member can have at most 3 active loans.
- A member with an overdue loan cannot borrow.
- Loans are due 14 days after borrowing.
- A book cannot be borrowed when no copies are free.

The limits are set in `src/main/resources/application.properties` under `booklender.borrowing`.

Books or members with loans cannot be deleted.

## Other pages

- Swagger UI: http://localhost:8220/swagger-ui.html
- Health: http://localhost:8220/actuator/health
- H2 console: http://localhost:8220/h2-console (JDBC URL `jdbc:h2:file:./data/booklender;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_ON_EXIT=FALSE`, user `booklender`, password `booklender`)

Only one app instance can use the database file at a time.

## Tests

```bash
mvn test
```

The integration test uses Docker. The unit tests do not.
