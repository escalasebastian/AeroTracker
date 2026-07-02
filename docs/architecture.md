# Architecture

## Stack

| Layer       | Technology                             |
|-------------|----------------------------------------|
| Backend     | Java 21, Spring Boot 3.x               |
| Persistence | PostgreSQL, Spring Data JPA, Hibernate |
| Messaging   | RabbitMQ *(Phase 4+)*                  |
| Containers  | Docker, Docker Compose                 |
| CI/CD       | GitHub Actions                         |
| Cloud       | AWS — EC2, RDS → ECS/Fargate           |

---

## Layered Architecture

### Package Structure

```
controller
service
repository
entity
dto
provider
config
exception
scheduler
mapper
```

### Rules

- **Controllers** handle HTTP/Telegram input only — no business logic.
- **Services** contain all business logic.
- **Repositories** manage persistence exclusively.
- External integrations are abstracted behind **interfaces**.
- Clear separation of responsibilities at all times.

---

## Flight Price Provider

The application never depends directly on a specific flights API.

```text
interface FlightPriceProvider
  ├── MockFlightPriceProvider       // used in Phases 1–3
  └── AmadeusFlightPriceProvider    // future implementation
```

**Why:** enables testing without external dependencies, avoids quota limits, and follows OCP and DIP from SOLID.

---

## Data Model

### `User`

| Field          | Type          |
|----------------|---------------|
| id             | Long          |
| telegramUserId | Long          |
| username       | String        |
| createdAt      | LocalDateTime |

### `Route`

Represents a unique flight route. Does **not** store price.

| Field              | Type          |
|--------------------|---------------|
| id                 | Long          |
| originAirport      | String        |
| destinationAirport | String        |
| departureDate      | LocalDate     |
| returnDate         | LocalDate     |
| createdAt          | LocalDateTime |

### `Subscription`

Relationship between a user and a route.

| Field       | Type          |
|-------------|---------------|
| id          | Long          |
| userId      | Long (FK)     |
| routeId     | Long (FK)     |
| targetPrice | BigDecimal    |
| createdAt   | LocalDateTime |

> If multiple users track the same route, the API is queried only **once**.

### `FlightPriceHistory`

| Field     | Type          |
|-----------|---------------|
| id        | Long          |
| routeId   | Long (FK)     |
| price     | BigDecimal    |
| checkedAt | LocalDateTime |

---

## Testing

- **JUnit 5** + **Mockito** from Phase 1.
- Unit tests are added progressively alongside each feature.
- Goal: arrive at the CI/CD phase with an established test suite.