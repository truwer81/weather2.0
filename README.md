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
- `.env` file created locally

Do not commit real secrets such as API keys to the repository.

---

## 3. Environment configuration

The project uses a single `.env` file for Docker-based environments.

Example `.env`:
```env
POSTGRES_DB=weatherForecast
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
OPEN_WEATHER_API_KEY=your_api_key
SERVER_PORT=8080
```

Important:

- Docker Compose uses `.env` automatically via the `--env-file` flag
- The Spring Boot application started from IntelliJ DOES NOT read `.env` automatically
- For DEV mode, you must provide `OPEN_WEATHER_API_KEY` manually in IntelliJ Run Configuration


## Environment differences

The application uses different database connection strategies depending on the environment:

- DEV (IntelliJ):
    - connects to database via: localhost:5433
    - uses fallback configuration from application.properties

- PROD (Docker):
    - connects to database via: postgres-database:5432
    - connection is provided via DB_URL environment variable in docker-compose

This difference is required because:
- IntelliJ runs on the host machine
- Docker containers communicate through internal Docker network

---

## 4. Running the project locally in IDE mode (DEV)

In this mode:

- PostgreSQL runs in Docker
- the Spring Boot application runs from IntelliJ IDEA

### Start the database
```shell
docker compose --env-file .env -f docker-compose.dev.yml -p weather-dev up -d
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
http://localhost:8080

This requires starting the Spring Boot application manually from IntelliJ IDEA.

### Database connection in DEV mode

You can connect to the DEV database from PgAdmin or IntelliJ using:

- Host: localhost
- Port: 5433
- Database: weatherForecast
- User, password: value from .env (used by Docker container)

---

## 5. Running the project locally in full Docker mode (PROD)

In this mode:

- PostgreSQL runs in Docker
- the Spring Boot application also runs in Docker

### Build the application and start the full stack

```shell
mvn clean package
```

#### First time, with postgres:
```shell
docker compose --env-file .env -f docker-compose.prod.yml -p weather-prod up -d --build
```

#### Without postgres, only app:
```shell
docker compose --env-file .env -f docker-compose.prod.yml -p weather-prod up -d --build weather-app
```

### Stop the full stack
```shell
docker compose -f docker-compose.prod.yml -p weather-prod down
```

### Stop the full stack and remove its volumes
```shell
docker compose -f docker-compose.prod.yml -p weather-prod down -v
```

## Troubleshooting

If the application container restarts repeatedly:

- Check logs:
```shell
  docker compose --env-file .env -f docker-compose.prod.yml -p weather-prod logs -f weather-app
```

- Most common issue:
  wrong database URL (e.g. localhost instead of postgres-database)


### Application URL
When the full Docker stack is running, the application is available at:
http://localhost:8081


### Database connection in PROD mode

You can connect to the PROD database from PgAdmin or IntelliJ using:

- Host: localhost
- Port: 5434
- Database: weatherForecast
- User, password: value from .env

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