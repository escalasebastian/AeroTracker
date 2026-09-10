# AeroTracker - Flight Price Monitoring Backend

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Message%20Broker-FF6600.svg)
![AWS](https://img.shields.io/badge/AWS-Serverless%20%7C%20ECS%20Fargate-232F3E.svg)
![Telegram](https://img.shields.io/badge/Telegram-Bot%20API-0088cc.svg)

AeroTracker is a Telegram bot developed as a robust flight price tracker. This project started as a backend portfolio designed to demonstrate proficiency in building distributed and cloud-native systems, evolving from an initial monolith into a microservices-oriented architecture deployed on AWS as a 100% serverless platform. The account runs under AWS's credit-based Free Tier rather than the legacy 12-month tier, so Fargate and RDS are not free by default; the infrastructure is described in Terraform and switched off between demos through a single `platform_enabled` variable, which stops every task and deletes the database, to stay near-zero cost.

## 🚀 Current Status

**Phase 7 Completed (AWS Serverless Deployment).** 

The application is currently deployed in production using **AWS ECS with AWS Fargate** and **Amazon RDS**. The current iteration uses a simulated flight provider (`MockFlightPriceProvider`), which will soon be replaced by a real flight pricing API (evaluating providers with a usable free tier, such as Duffel or SerpApi). 

The bot supports the following main commands via Telegram:
- `/start` - Starts the interaction with the bot.
- `/price MAD AMS 2027-03-15` - Checks the price of a one-way flight.
- `/price MAD AMS 2027-03-15 2027-03-22` - Checks the price of a round-trip flight.

## 🎯 What This Project Demonstrates

This project serves as a technical showcase of advanced Backend and Cloud capabilities:

- **Java 21 & Spring Boot 3.x:** Use of the latest language features and dependency injection.
- **Microservices Architecture:** Decoupled into 5 independent services (`api`, `scheduler`, `price-checker`, `notification`, and `rabbitmq`).
- **Event-Driven Architecture:** Use of RabbitMQ for asynchronous messaging between microservices.
- **Cloud Infrastructure (AWS):** Secure networking (VPC, Public/Private Subnets), serverless compute with ECS Fargate, and relational persistence with RDS.
- **Service Discovery & Private DNS:** Secure internal communication using AWS Cloud Map (ECS Service Discovery).
- **Long Polling & Security:** Refactored to Telegram Long Polling to avoid exposing incoming public ports, improving security and removing the need for a public load balancer.
- **SOLID Principles & Clean Architecture:** Application of Dependency Inversion to facilitate switching flight providers in the future.

## 🏗️ Architecture Overview

AeroTracker follows a distributed system design. The general operation flow is as follows:

1. **User Interaction:** The user sends a command to the Telegram bot.
2. **API Microservice (`aerotracker-api`):** Actively listens to Telegram using secure Long Polling (no webhooks).
3. **Persistence:** The API validates the command and reads/writes subscriptions directly in PostgreSQL; it does not talk to RabbitMQ.
4. **Event Broker (`rabbitmq`) and Workers (`scheduler`, `price-checker`, `notification`):**
   - The *Scheduler* runs on a fixed interval and publishes a check request per tracked route to RabbitMQ.
   - The *Price Checker* consumes those requests, communicating transparently (via Interface) with the `MockFlightPriceProvider` (upcoming real API integration), and publishes the result.
   - The *Notification* service consumes price results and price-drop alerts and sends them back to the user via the Telegram API.
5. **Database (`Amazon RDS`):** User profiles and monitoring subscriptions are stored in a private PostgreSQL database, inaccessible from the internet.
6. **Observability:** All logs are centralized and monitored through AWS CloudWatch.

## 🗺️ Roadmap

The project has followed an iterative and progressive evolution.

| Phase | Status | Focus | Main Technologies |
| :--- | :---: | :--- | :--- |
| **Phase 1** | ✅ Completed | Manual Price Queries | Spring Boot, Telegram API, Mock Data |
| **Phase 2** | ✅ Completed | Persistence and Subscriptions | PostgreSQL, Spring Data JPA |
| **Phase 3** | ✅ Completed | Automatic Monitoring | Spring Scheduling (`@Scheduled`) |
| **Phase 4** | ✅ Completed | Distributed Architecture | RabbitMQ, Event-Driven Design |
| **Phase 5** | ✅ Completed | Dockerization | Docker, Docker Compose |
| **Phase 6** | ✅ Completed | CI/CD Automation | GitHub Actions, GHCR |
| **Phase 7** | ✅ Completed | AWS Deployment (Serverless) | AWS ECS (Fargate), RDS, CloudWatch, VPC |
| **Phase 8** | ✅ Completed | Infrastructure as Code | Terraform, SSM Parameter Store |

*Planned next steps: Replace the MockProvider with a real flight pricing API.*

## 🛠️ Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.x
- **Database:** PostgreSQL 16
- **Message Broker:** RabbitMQ
- **Containers:** Docker
- **CI/CD:** GitHub Actions, GitHub Container Registry
- **Cloud Computing:** Amazon Web Services (ECS Fargate, RDS, CloudWatch, VPC)

## 💻 Local Development

To run AeroTracker in your local environment:

### Prerequisites
- JDK 21 installed.
- Docker and Docker Compose installed.
- A valid Telegram bot token (create it using [BotFather](https://t.me/botfather)).

### Setup Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/AeroTracker.git
   cd AeroTracker
   ```

2. **Configure environment variables:**
   You need to export your Telegram bot token.
   - *On Linux / macOS:*
     ```bash
     export TELEGRAM_BOT_TOKEN="your_token_here"
     ```
   - *On Windows (PowerShell):*
     ```powershell
     $env:TELEGRAM_BOT_TOKEN="your_token_here"
     ```

3. **Spin up local dependencies and start the project:**
   Since it is now a microservices architecture, the easiest way is to use Docker Compose to spin up PostgreSQL, RabbitMQ, and the services:
   ```bash
   docker-compose up --build
   ```

*(Note: Because the project uses Long Polling, there is no need to expose local ports with tools like ngrok to receive Telegram updates).*

## 🧪 Testing

To run the unit test suite (and future integration tests with Testcontainers):

```bash
./mvnw clean test
```

## 🔐 Security Notes & Project Purpose

- **Secrets Management:** Never commit the `TELEGRAM_BOT_TOKEN` or database credentials to version control systems. Always use environment variables or secret managers (like AWS Secrets Manager).
- **Cloud Security:** The production database is isolated in a private subnet. Any direct maintenance requires temporarily starting the Bastion Host (EC2) to establish an SSH tunnel.
- **Purpose:** AeroTracker was built with an educational and demonstrative focus, prioritizing simplicity, maintainability, and the application of Clean Architecture, to showcase competencies in modern software engineering and professional-grade cloud computing.
