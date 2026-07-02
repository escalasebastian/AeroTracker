# Roadmap

## Phase 0 — Setup

**Goal:** Prepare the environment and understand the integrations.

- Create Telegram bot via BotFather. Obtain `BOT_TOKEN` and `CHAT_ID`. Test HTTP message sending.
- Research Amadeus API and Kiwi API — do **not** integrate yet.
- Review Spring Boot basics: Controllers, Services, Dependency Injection, DTOs, Configuration Properties.
- Review Docker: Dockerfile, Docker Compose, environment variables.

---

## Phase 1 — Manual Price Query

**Goal:** Build the first functional version.

**Architecture:** `Telegram → Spring Boot → MockFlightPriceProvider`

**Commands:**

- `/start` — show help
- `/price MAD AMS 2026-08-10` — one-way price
- `/price MAD AMS 2026-08-10 2026-08-17` — round-trip price

**Learn:** Telegram integration, DTOs, service design, decoupled providers, basic testing.

**Done when:** user can request prices from Telegram.

---

## Phase 2 — Persistence and Subscriptions

**Goal:** Introduce persistent state.

**Architecture:** `Telegram → Spring Boot → PostgreSQL`

**Commands:**

- `/track` — save a route subscription
- `/untrack` — remove a subscription
- `/list` — list active subscriptions

**Learn:** PostgreSQL, JPA, Hibernate, entity relationships, data migrations.

**Done when:** users can manage persistent route subscriptions.

---

## Phase 3 — Automatic Monitoring

**Goal:** Eliminate the need for manual price queries.

**Architecture:** `Scheduler → Spring Boot → PostgreSQL → FlightPriceProvider → Telegram`

**Features:**

- Periodic job with `@Scheduled`
- Query all subscribed routes
- Record price history
- Detect price drops
- Send automatic alerts via Telegram

**Learn:** `@Scheduled`, background processing, historical data comparison, automation.

**Done when:** alerts fire automatically without user intervention.

---

## Phase 4 — Distributed Architecture

**Goal:** Introduce asynchronous communication between services.

**Services:**

| Service               | Responsibility                  |
|-----------------------|---------------------------------|
| Scheduler Service     | Generates check requests        |
| Price Checker Service | Queries prices and emits events |
| Notification Service  | Sends messages to Telegram      |

**Communication:** RabbitMQ

**Learn:** RabbitMQ, event-driven architecture, async communication, basic microservices.

**Done when:** services communicate exclusively through queues.

---

## Phase 5 — Full Dockerization

**Goal:** Run the entire system locally with Docker Compose.

**Components:** PostgreSQL · RabbitMQ · Scheduler Service · Price Checker Service · Notification Service

**Learn:** Docker Compose networking, environment variables, containerization.

---

## Phase 6 — CI/CD

**Goal:** Automate validation and deployment.

**GitHub Actions pipeline:** `Build → Test → Docker Build → Publish images`

**Learn:** continuous integration, automation, software quality gates.

---

## Phase 7 — AWS Deployment

**Goal:** Deploy the system to the cloud.

**Initially:** EC2 + RDS (PostgreSQL)

**Later:** ECS + Fargate + CloudWatch

**Learn:** cloud deployment, networking, observability, security.