# Studycase

A REST API for checking availability and managing bookings, built with **Java**, **Spring Boot** and **PostgreSQL**.  

---

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Language     | Java 17                             |
| Framework    | Spring Boot 4.0.3 (Spring MVC)      |
| Persistence  | Spring Data JPA + Hibernate         |
| Database     | PostgreSQL 17                       |
| Migrations   | Flyway                              |
| API Docs     | SpringDoc OpenAPI (Swagger UI)      |
| Build tool   | Maven                               |
| Utilities    | Lombok                              |

---

## Business Rules

- Bookings can be made **Monday to Thursday, Saturday, Sunday** — **Fridays are not allowed**
- Working hours: **08:00 – 22:00** (start and end must be within this range)
- Duration is either **2 or 4 hours**
- Between **1 and 3 professionals** can be assigned per booking, all from the **same vehicle**
- A **30-minute break** is enforced between consecutive appointments for each professional
- Bookings cannot be created or updated to a **past date/time**

---

## Prerequisites

Make sure the following are installed:

- [Java 17+]
- [Maven]
- [Docker]

---

## Getting Started

### 1. Start the PostgreSQL database

```bash
docker compose up -d
```

This starts a `postgres:17` container with:

| Setting   | Value           |
|-----------|-----------------|
| Host      | `localhost`     |
| Port      | `5433`          |
| Database  | `studycase`     |
| User      | `postgres`      |
| Password  | `postgres`      |

### 2. Run the application

```bash
./mvnw spring-boot:run
```

The application starts on **http://localhost:8080**.

On startup it will automatically:
1. Connect to PostgreSQL on `localhost:5433`
2. Run pending Flyway migrations (schema + data)
3. Start the embedded Tomcat server

### 3. Explore the API

Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

---

## Postman Collection

A ready-to-use Postman collection is available at:

```
postman/Studycase API.postman_collection.json
```

Import it into Postman to test all endpoints immediately.

---

## Stopping dockerized database

Stop the container:

```bash
docker compose down
```

Stop and remove all data (volumes):

```bash
docker compose down -v
```