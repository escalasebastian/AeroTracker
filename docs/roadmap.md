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
- `/price MAD AMS 2027-03-15` — one-way price
- `/price MAD AMS 2027-03-15 2027-03-22` — round-trip price

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

- **Phase 7.1:** AWS IAM, Custom VPC, Subnets, Security Groups & RDS PostgreSQL 16
- **Phase 7.2:** EC2 Compute Deployment with Docker Compose & Caddy HTTPS Reverse Proxy
- **Phase 7.3:** Serverless Container Migration with AWS ECS + Fargate & Service Discovery
- **Phase 7.4:** Cloud Observability & Monitoring with AWS CloudWatch (Dashboards & Alarms)

**Status:** Completed
**Learn:** cloud deployment, serverless container orchestration, networking, observability, security.

---

## Phase 8 — Infrastructure as Code

**Goal:** Make the AWS infrastructure reproducible and eliminate idle cost.

- Adopt the Phase 7 infrastructure (VPC, ECS Fargate, RDS, Cloud Map, CloudWatch, bastion) into Terraform using `import` blocks, so the account state is fully described as code without recreating anything.
- Move `DB_PASSWORD` and `TELEGRAM_BOT_TOKEN` out of plain ECS environment variables into SSM Parameter Store.
- Restore the RDS instance from its final snapshot as a Terraform-managed resource.
- Parameterize `desired_count` so every ECS service defaults to 0 (platform switched off, near-zero cost) and can be brought up on demand with `terraform apply -var="desired_count=1"`.

**Learn:** Infrastructure as Code, importing unmanaged cloud resources, secrets management, cost-aware infrastructure design.

**Done when:** `terraform plan` reports no unexpected changes against the live account, and the platform can be started and stopped through a single variable.
