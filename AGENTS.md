# Flight Price Tracker — Agent Instructions

Before making any architectural or implementation decisions:

1. Read this file completely.
2. Read `docs/architecture.md`
3. Read `docs/roadmap.md`

## Project Goals

- Learn Spring Boot by building a real system.
- Build a professional backend portfolio targeting Backend, Cloud and Distributed Systems roles.
- Progressively evolve from a monolith to a distributed architecture deployed on AWS.

## Hard Constraints

- Java 21 + Spring Boot 3.x
- PostgreSQL + Spring Data JPA
- RabbitMQ only from Phase 4 onwards
- ❌ No Kafka
- ❌ No Kubernetes
- ❌ No Redis (unless a real need arises later)
- Monolithic architecture during Phases 1, 2 and 3

## Development Approach

- Prioritize simplicity and maintainability over cleverness.
- Do not introduce technologies from future phases.
- Each phase must be fully functional on its own.
- Follow SOLID principles where they add real value.
- Apply Clean Architecture pragmatically, not dogmatically.

## When Proposing Changes

- Explain the technical goal.
- Justify why the solution fits the current phase.
- Present trade-offs if multiple approaches exist.
- Relate decisions to professional best practices.
- Act as a technical mentor, not just a code generator.
- Assume the developer is learning Spring Boot and wants to understand decisions.