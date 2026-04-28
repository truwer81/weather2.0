# Weather 2.0

Weather 2.0 is a backend Spring Boot application written in Java.  
It exposes REST endpoints for shared and private locations, current weather, and forecast data, and uses PostgreSQL as its database.

Current application version: 2.2

Project status: actively developed portfolio backend project.

Current scope includes shared locations, authenticated user-specific private locations, OpenWeather integration, weather and forecast retrieval, 
authentication, and admin access separation.

---

## What this project demonstrates

- Java 21 and Spring Boot backend development
- REST API design for weather, forecast, shared locations, and private user locations
- PostgreSQL persistence with Flyway database migrations
- Spring Security authentication and access separation for admin and authenticated users
- OpenWeather integration for geocoding, current weather, and forecast data
- Database-backed caching of weather and forecast responses
- Docker-based local runtime modes
- Automated tests including security and persistence coverage

---

## Domain and access model

The application distinguishes between two types of locations:

- **Shared locations** - globally visible locations managed by administrators and available to all users
- **Private locations** - user-owned locations available only to the authenticated user

Access is separated with Spring Security:

- public users can read shared locations and weather data
- administrators can manage shared locations
- authenticated users can manage their own private locations

---

## Quick start

The project can be started in two local runtime modes.

### DEV mode

In DEV mode, PostgreSQL runs in Docker and the Spring Boot application is started directly from IntelliJ IDEA.

1. Create a local `.env` file based on the example in [Environment configuration](#3-environment-configuration).

2. Start PostgreSQL:

```shell
docker compose --env-file .env -f docker-compose.dev.yml -p weather-dev up -d
```

3. Start the Spring Boot application from IntelliJ IDEA.

4. Open:

```text
http://localhost:8080
```

### Full Docker mode

In full Docker mode, both PostgreSQL and the Spring Boot application run in Docker containers.

1. Build the application:

```shell
mvn clean package
```

2. Start the full stack:

```shell
docker compose --env-file .env -f docker-compose.prod.yml -p weather-prod up -d --build
```

3. Open:

```text
http://localhost:8081
```

---

## 1. Project architecture

The application consists of two main components:

- **weather-app** - a Spring Boot backend application
- **postgres-database** - a PostgreSQL database running in Docker

Depending on the selected mode:

- in **DEV mode**, the application connects from IntelliJ IDEA to a PostgreSQL container exposed on `localhost:5433`
- in **PROD mode**, the application runs in Docker and connects to PostgreSQL through the internal Docker network using the service name `postgres-database:5432`

Database access from tools such as **PgAdmin** or **IntelliJ Database Tools** is possible in both modes through exposed host ports.

These are local runtime modes for starting and verifying the project. They are not separate Spring profiles.

---

## 2. Prerequisites

Before running the project, make sure you have:

- Java 21 installed
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

- Docker Compose uses `.env` through the `--env-file` flag
- The Spring Boot application started from IntelliJ does not read `.env` automatically
- For DEV mode, you must provide `OPEN_WEATHER_API_KEY` manually in IntelliJ Run Configuration

### Environment differences

- DEV mode:
  - application runs from IntelliJ IDEA
  - PostgreSQL runs in Docker
  - application connects to the database through `localhost:5433`
  - database configuration can use fallback values from `application.properties`

- PROD mode:
  - application runs in Docker
  - PostgreSQL runs in Docker
  - application connects to the database through `postgres-database:5432`
  - database connection is provided via the `DB_URL` environment variable in Docker Compose

This difference is required because IntelliJ runs on the host machine, while Docker containers communicate through the internal Docker network.

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

```text
http://localhost:8080
```

This requires starting the Spring Boot application manually from IntelliJ IDEA.

### Database connection in DEV mode

You can connect to the DEV database from PgAdmin or IntelliJ using:

- Host: `localhost`
- Port: `5433`
- Database: `weatherForecast`
- User, password: value from `.env` used by the Docker container

---

## 5. Running the project locally in full Docker mode (PROD)

In this mode:

- PostgreSQL runs in Docker
- the Spring Boot application also runs in Docker

### Build the application

```shell
mvn clean package
```

### Start the full stack

```shell
docker compose --env-file .env -f docker-compose.prod.yml -p weather-prod up -d --build
```

### Start only the application container

Use this when PostgreSQL is already running and you only want to rebuild or restart the application container:

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

### Application URL

When the full Docker stack is running, the application is available at:

```text
http://localhost:8081
```

### Database connection in PROD mode

You can connect to the PROD database from PgAdmin or IntelliJ using:

- Host: `localhost`
- Port: `5434`
- Database: `weatherForecast`
- User, password: value from `.env`

### Troubleshooting

If the application container restarts repeatedly, check logs:

```shell
docker compose --env-file .env -f docker-compose.prod.yml -p weather-prod logs -f weather-app
```

The most common issue is an incorrect database URL, for example using `localhost` instead of `postgres-database` inside Docker.

---

## 6. Running tests

Run the test suite with:

```shell
mvn test
```

The test suite includes selected coverage for:

- repository and persistence behavior
- service-level business rules
- Spring Security access rules
- PostgreSQL integration using Testcontainers

---

## 7. Database initialization

The database schema is managed by Flyway migrations from:

```text
src/main/resources/db/migration
```

If you want to recreate the local database from scratch and replay migrations from an empty volume, use one of the following commands.

For DEV mode:

```shell
docker compose -f docker-compose.dev.yml -p weather-dev down -v
```

For PROD mode:

```shell
docker compose -f docker-compose.prod.yml -p weather-prod down -v
```

The next startup will recreate the database volume and run Flyway migrations again.

---

## 8. REST API overview

The application exposes REST endpoints for shared locations, user-specific private locations, current weather data and weather forecasts. Shared locations are globally visible and managed by administrators, while private locations belong to the authenticated user.

For full endpoint documentation, including example requests and responses, see [docs/api-endpoints.md](docs/api-endpoints.md).

### Endpoint summary

| Method | Endpoint                                | Access               | Description                                             |
|--------|-----------------------------------------|----------------------|---------------------------------------------------------|
| GET    | `/login`                                | public               | Returns the login page                                  |
| POST   | `/login`                                | public               | Performs form-based authentication                      |
| POST   | `/logout`                               | authenticated        | Logs out the current user                               |
| GET    | `/api/auth/me`                          | public/authenticated | Returns current authentication state                    |
| GET    | `/api/locations`                        | public               | Returns shared locations                                |
| POST   | `/api/locations`                        | `ROLE_ADMIN`         | Creates a shared location                               |
| PUT    | `/api/locations/{id}`                   | `ROLE_ADMIN`         | Updates a shared location                               |
| DELETE | `/api/locations/{id}`                   | `ROLE_ADMIN`         | Deletes a shared location                               |
| PUT    | `/api/locations/order`                  | `ROLE_ADMIN`         | Saves shared location display order                     |
| GET    | `/api/locations/search?q={query}`       | public               | Returns location suggestions from OpenWeather geocoding |
| GET    | `/api/weather?locationId={id}`          | public               | Returns current weather for a shared location           |
| GET    | `/api/weather/forecast?locationId={id}` | public               | Returns forecast for a shared location                  |
| GET    | `/api/my/locations`                     | authenticated        | Returns private locations for the current user          |
| POST   | `/api/my/locations`                     | authenticated        | Creates a private location                              |
| PUT    | `/api/my/locations/{id}`                | authenticated        | Updates a private location                              |
| DELETE | `/api/my/locations/{id}`                | authenticated        | Deletes a private location                              |
| PUT    | `/api/my/locations/order`               | authenticated        | Saves private location order                            |
| GET    | `/api/my/locations/{id}/weather`        | authenticated        | Returns current weather for a private location          |
| GET    | `/api/my/locations/{id}/forecast`       | authenticated        | Returns forecast for a private location                 |


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

---

## 9. Additional documentation

- [API endpoints](docs/api-endpoints.md)
- [Local development users](docs/local-development-users.md)

---

## 10. Notes and limitations

This is a portfolio project intended for local development and technical demonstration.

- Secrets are expected to be provided locally through environment variables.
- The project uses local DEV and PROD-like runtime modes rather than production deployment infrastructure.
- User-specific private location features are actively being developed.