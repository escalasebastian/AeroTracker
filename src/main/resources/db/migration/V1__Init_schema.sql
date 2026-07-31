-- V1__Init_schema.sql

CREATE TABLE users
(
    id               BIGSERIAL PRIMARY KEY,
    telegram_user_id BIGINT    NOT NULL UNIQUE,
    username         VARCHAR(255),
    created_at       TIMESTAMP NOT NULL
);

CREATE TABLE routes
(
    id             BIGSERIAL PRIMARY KEY,
    origin         VARCHAR(3) NOT NULL,
    destination    VARCHAR(3) NOT NULL,
    departure_date DATE       NOT NULL,
    return_date    DATE,
    CONSTRAINT uk_route_flight_details UNIQUE (origin, destination, departure_date, return_date)
);

CREATE TABLE subscriptions
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT         NOT NULL REFERENCES users (id),
    route_id     BIGINT         NOT NULL REFERENCES routes (id),
    target_price NUMERIC(10, 2) NOT NULL,
    active       BOOLEAN        NOT NULL DEFAULT true,
    created_at   TIMESTAMP      NOT NULL,
    CONSTRAINT uk_subscription_user_route UNIQUE (user_id, route_id)
);

CREATE TABLE flight_price_history
(
    id         BIGSERIAL PRIMARY KEY,
    route_id   BIGINT         NOT NULL REFERENCES routes (id),
    price      NUMERIC(10, 2) NOT NULL,
    checked_at TIMESTAMP      NOT NULL
);