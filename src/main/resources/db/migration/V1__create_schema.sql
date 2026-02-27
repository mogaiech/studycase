CREATE TABLE vehicle
(
    id           BIGSERIAL PRIMARY KEY,
    brand_name   VARCHAR(100) NOT NULL,
    plate_number VARCHAR(20)  NOT NULL UNIQUE
);

CREATE TABLE booking
(
    id                 BIGSERIAL PRIMARY KEY,
    status             VARCHAR(20)  NOT NULL,
    customer_name      VARCHAR(150) NOT NULL,
    customer_email     VARCHAR(150) NOT NULL,
    start_date_time    TIMESTAMP    NOT NULL,
    end_date_time      TIMESTAMP    NOT NULL,
    duration_hours     INT          NOT NULL CHECK (duration_hours IN (2, 4)),
    professional_count INT          NOT NULL CHECK (professional_count IN (1, 2, 3))
);

CREATE TABLE professional
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    phone      VARCHAR(20),
    vehicle_id BIGINT       NOT NULL REFERENCES vehicle (id)
);

CREATE TABLE booking_professional
(
    id              BIGSERIAL PRIMARY KEY,
    booking_id      BIGINT NOT NULL REFERENCES booking (id) ON DELETE CASCADE,
    professional_id BIGINT NOT NULL REFERENCES professional (id),
    UNIQUE (booking_id, professional_id)
);