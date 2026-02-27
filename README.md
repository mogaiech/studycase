# Studycase – Getting Started

## Prerequisites

Make sure you have the following installed on your machine:
- [Java 17+]
- [Maven]
- [Docker]

---

## 1. Start the PostgreSQL Database

The project uses a Dockerized PostgreSQL 17 instance managed via Docker Compose.

```bash
docker compose up -d
```

This will:
- Pull the `postgres:17` image if not already present
- Create a container named `studycase_postgres`
- Create a database named `studycase` with user `postgres` / password `postgres`
- Expose PostgreSQL on **port 5433** 

To verify the database is running:

```bash
docker exec studycase_postgres psql -U postgres -d studycase -c "SELECT 1"
```

## 2. Database migrations using Flyway

Flyway is configured to run automatically on application startup.

Migration scripts are located in:
```
src/main/resources/db/migration/
```

Scripts follow the naming convention: `V{version}__{description}.sql`

No manual action is needed.

---

## 3. Run Spring Boot Application

```bash
./mvnw spring-boot:run
```

The application will start on **http://localhost:8080**.

On startup, Spring Boot will:
1. Connect to the PostgreSQL database on `localhost:5433`
2. Run any pending Flyway migrations
3. Start the embedded Tomcat server

## 4. Stop database

To stop the **PostgreSQL container**:

```bash
docker compose down
```

To stop and **remove all data**:

```bash
docker compose down -v
```