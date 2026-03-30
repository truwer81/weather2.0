# Weather 2.0

Weather 2.0 is a backend Spring Boot application written in Java.  
It exposes a simple REST API for weather-related operations and uses PostgreSQL as its database.

The project can be run in two modes:

- **DEV mode** – PostgreSQL runs in Docker, while the Spring Boot application is started directly from IntelliJ IDEA
- **PROD mode** – both PostgreSQL and the Spring Boot application run in Docker containers

This setup allows convenient development in the IDE and easy verification of the final containerized version.

---

## 1. Project architecture

The application consists of two main components:

- **weather-app** – a Spring Boot backend application
- **postgres-database** – a PostgreSQL database running in Docker

Depending on the selected mode:

- in **DEV mode**, the application connects from IntelliJ IDEA to a PostgreSQL container exposed on `localhost:5433`
- in **PROD mode**, the application runs in Docker and connects to PostgreSQL through the internal Docker network using the service name `postgres-database:5432`

Database access from tools such as **PgAdmin** or **IntelliJ Database Tools** is possible in both modes through exposed host ports.

---

## 2. Prerequisites

Before running the project, make sure you have:

- Java installed
- Maven installed
- Docker and Docker Compose installed
- IntelliJ IDEA installed
- `.env.dev` and `.env.prod` files created locally

Do not commit real secrets such as API keys to the repository.

---

## 3. Environment files

The project uses two separate environment files:

- `.env.dev` – configuration for IDE mode
- `.env.prod` – configuration for full Docker mode

### `.env.dev`
Used when:
- PostgreSQL runs in Docker
- the application runs from IntelliJ IDEA

### `.env.prod`
Used when:
- PostgreSQL runs in Docker
- the application also runs in Docker

---

## 4. Running the project locally in IDE mode (DEV)

In this mode:

- PostgreSQL runs in Docker
- the Spring Boot application runs from IntelliJ IDEA

### Start the database
```shell
docker compose --env-file .env.dev -f docker-compose.dev.yml -p weather-dev up -d
```

### Stop the database
```shell
docker compose -f docker-compose.dev.yml -p weather-dev down
```

### Stop the database and remove its volume
```shell
docker compose -f docker-compose.dev.yml -p weather-dev down -v
```

### Application URL
When running in IDE mode, the application is available at:
'http://localhost:8080'

This requires starting the Spring Boot application manually from IntelliJ IDEA.

### Database connection in DEV mode

You can connect to the DEV database from PgAdmin or IntelliJ using:

- Host: localhost
- Port: 5433
- Database: weatherForecast
- User, password: value from .env.dev

---

## 5. Running the project locally in full Docker mode (PROD)

In this mode:

- PostgreSQL runs in Docker
- the Spring Boot application also runs in Docker

### Build the application and Start the full stack

```shell
mvn clean package
```
```shell
docker compose --env-file .env.prod -f docker-compose.prod.yml -p weather-prod up -d --build
```

### Stop the full stack
```shell
docker compose -f docker-compose.prod.yml -p weather-prod down
```

### Stop the full stack and remove its volumes
```shell
docker compose -f docker-compose.prod.yml -p weather-prod down -v
```

### Application URL
When the full Docker stack is running, the application is available at:
'http://localhost:8081'

Database connection in PROD mode

You can connect to the PROD database from PgAdmin or IntelliJ using:

- Host: localhost
- Port: 5434
- Database: weatherForecast
- User, password: value from .env.prod

---

## 6. Notes about database initialization

The database container uses initialization scripts mounted into /docker-entrypoint-initdb.d/.

These scripts are executed only when the PostgreSQL data volume is created for the first time.

This means that if you change schema.sql or data.sql, the changes may not be applied automatically to an already existing database volume.

To recreate the database from scratch, use:
```shell
docker compose -f docker-compose.dev.yml -p weather-dev down -v
```
or
```shell
docker compose -f docker-compose.prod.yml -p weather-prod down -v
```