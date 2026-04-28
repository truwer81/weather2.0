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



## 7. REST API overview

The application exposes REST endpoints for shared locations, user-specific private locations, current weather data and weather forecasts. Shared locations are globally visible and managed by administrators, while private locations belong to the authenticated user.

For full endpoint documentation, including example requests and responses, see [docs/api-endpoints.md](docs/api-endpoints.md).

### Endpoint summary

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/login` | public | Returns the login page |
| POST | `/login` | public | Performs form-based authentication |
| POST | `/logout` | authenticated | Logs out the current user |
| GET | `/api/auth/me` | public/authenticated | Returns current authentication state |
| GET | `/api/locations` | public | Returns shared locations |
| POST | `/api/locations` | `ROLE_ADMIN` | Creates a shared location |
| PUT | `/api/locations/{id}` | `ROLE_ADMIN` | Updates a shared location |
| DELETE | `/api/locations/{id}` | `ROLE_ADMIN` | Deletes a shared location |
| PUT | `/api/locations/order` | `ROLE_ADMIN` | Saves shared location display order |
| GET | `/api/locations/search?q={query}` | public | Returns location suggestions from OpenWeather geocoding |
| GET | `/api/weather?locationId={id}` | public | Returns current weather for a shared location |
| GET | `/api/weather/forecast?locationId={id}` | public | Returns forecast for a shared location |
| GET | `/api/my/locations` | authenticated | Returns private locations for the current user |
| POST | `/api/my/locations` | authenticated | Creates a private location |
| PUT | `/api/my/locations/{id}` | authenticated | Updates a private location |
| DELETE | `/api/my/locations/{id}` | authenticated | Deletes a private location |
| PUT | `/api/my/locations/order` | authenticated | Saves private location order |
| GET | `/api/my/locations/{id}/weather` | authenticated | Returns current weather for a private location |
| GET | `/api/my/locations/{id}/forecast` | authenticated | Returns forecast for a private location |

### Example: current weather

Example request:
```http
GET /api/weather?locationId=1 HTTP/1.1
Host: localhost:8080
Accept: application/json
```

Example response:
```json
{
  "locationId": 1,
  "name": "Warsaw",
  "country": "Poland",
  "region": "Mazowieckie",
  "latitude": 52.2297,
  "longitude": 21.0122,
  "providerTimestamp": "2026-04-28T09:00:00",
  "description": "broken clouds",
  "temperature": 14.8,
  "feelsLike": 13.9,
  "humidity": 58,
  "pressure": 1018.0,
  "windSpeed": 4.2,
  "windDeg": 210.0,
  "cloudsPercentage": 63.0
}
```

### Example: admin-only shared location creation

Example request:
```http
POST /api/locations HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "name": "Warsaw",
  "country": "Poland",
  "region": "Mazowieckie",
  "longitude": 21.0122,
  "latitude": 52.2297
}
```

Example response:
```json
{
  "id": 1,
  "name": "Warsaw",
  "country": "Poland",
  "region": "Mazowieckie",
  "longitude": 21.0122,
  "latitude": 52.2297,
  "sortOrder": 1
}
```

### Standard error response

Most API errors are returned in a consistent JSON format.

Example response:
```json
{
  "timestamp": "2026-04-28T10:17:31.112",
  "status": 404,
  "error": "Not Found",
  "message": "Location with id 999 not found",
  "path": "/api/weather"
}
```


## Local development users

For manual local testing, example non-admin users can be inserted into the local database with the SQL stored in `dev-tools/local-users.sql`.
pass: 'User1Test!' / 'User2Test!'
These accounts are intended for local development only and should not be used in shared or production environments.
```shell
INSERT INTO app_users (username, password_hash, enabled)
SELECT 'user1', '$2a$12$XWfc2p14XqQe8Fct.O8P2.1/yILxu1vEkoQ76O1TXpX.WkNjDZFGW', TRUE
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'user1');

INSERT INTO app_users (username, password_hash, enabled)
SELECT 'user2', '$2a$12$4tRbPusFkyB8gIzUQf8vxujElAG4cvFfNIPm5yzXNbOjPgpOC5d3q', TRUE
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'user2');
docker compose -f docker-compose.prod.yml -p weather-prod down -v
```
