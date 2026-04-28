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



## 7. Endpoint inventory

This section lists the HTTP endpoints used by the application.

### 7.1 Endpoints used by the current frontend

#### `GET /login`
- Purpose: returns the login page

Example request:
```http
GET /login HTTP/1.1
Host: localhost:8080
```

#### `POST /login`
- Purpose: performs form-based authentication handled by Spring Security

#### `POST /logout`
- Purpose: logs out the current user and redirects to the home page

#### `GET /api/auth/me`
- Purpose: returns current authentication state, username and roles

#### `GET /api/locations`
- Purpose: returns shared locations visible to everyone

Example request:
```http
GET /api/locations HTTP/1.1
Host: localhost:8080
Accept: application/json
```

Example response:
```json
[
  {
    "id": 1,
    "name": "Warsaw",
    "country": "Poland",
    "region": "Mazowieckie",
    "longitude": 21.0122,
    "latitude": 52.2297,
    "sortOrder": 1
  }
]
```

#### `POST /api/locations`
- Purpose: creates a shared location
- Access: `ROLE_ADMIN`

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

#### `PUT /api/locations/{id}`
- Purpose: updates a shared location
- Access: `ROLE_ADMIN`

Example request:
```http
PUT /api/locations/1 HTTP/1.1
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

#### `DELETE /api/locations/{id}`
- Purpose: deletes a shared location
- Access: `ROLE_ADMIN`

Example request:
```http
DELETE /api/locations/1 HTTP/1.1
Host: localhost:8080
```

Example response:
```http
HTTP/1.1 204 No Content
```

#### `PUT /api/locations/order`
- Purpose: saves shared location display order
- Access: `ROLE_ADMIN`

Example request:
```http
PUT /api/locations/order HTTP/1.1
Host: localhost:8080
Content-Type: application/json

[
  { "locationId": 3, "sortOrder": 1 },
  { "locationId": 1, "sortOrder": 2 },
  { "locationId": 2, "sortOrder": 3 }
]
```

Example response:
```json
[
  {
    "id": 3,
    "name": "Gdansk",
    "country": "Poland",
    "region": "Pomorskie",
    "longitude": 18.6466,
    "latitude": 54.3520,
    "sortOrder": 1
  }
]
```

#### `GET /api/locations/search?q={query}`
- Purpose: returns location suggestions from OpenWeather geocoding

Example request:
```http
GET /api/locations/search?q=war HTTP/1.1
Host: localhost:8080
Accept: application/json
```

Example response:
```json
[
  {
    "label": "Warsaw, Mazowieckie, PL",
    "name": "Warsaw",
    "region": "Mazowieckie",
    "country": "PL",
    "latitude": 52.2297,
    "longitude": 21.0122
  }
]
```

#### `GET /api/weather?locationId={locationId}`
- Purpose: returns current weather for a shared location

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

#### `GET /api/weather/forecast?locationId={locationId}`
- Purpose: returns forecast for a shared location

Example request:
```http
GET /api/weather/forecast?locationId=1 HTTP/1.1
Host: localhost:8080
Accept: application/json
```

Example response:
```json
[
  {
    "dateTime": "2026-04-28T12:00:00",
    "temperature": 15.1,
    "feelsLike": 14.2,
    "pressure": 1017.0,
    "humidity": 61.0,
    "windSpeed": 4.5,
    "windDirection": 220.0,
    "rainVolume": 0.0,
    "snowVolume": 0.0,
    "description": "light rain",
    "precipitationProbability": 0.26,
    "cloudsAll": 71.0
  }
]
```

#### `GET /api/my/locations`
- Purpose: returns private locations for the authenticated user
- Access: authenticated user

Example request:
```http
GET /api/my/locations HTTP/1.1
Host: localhost:8080
Accept: application/json
Cookie: JSESSIONID=...
```

Example response:
```json
[
  {
    "id": 24,
    "name": "Berlin",
    "country": "Germany",
    "region": "Berlin",
    "longitude": 13.405,
    "latitude": 52.52,
    "sortOrder": 1
  }
]
```

### 7.2 Additional backend endpoints currently not called by the shipped UI

#### `POST /api/my/locations`
- Purpose: creates a private location for the authenticated user
- Access: authenticated user

Request body:
```json
{
  "name": "Berlin",
  "country": "Germany",
  "region": "Berlin",
  "longitude": 13.4050,
  "latitude": 52.5200
}
```

Response body:
```json
{
  "id": 24,
  "name": "Berlin",
  "country": "Germany",
  "region": "Berlin",
  "longitude": 13.405,
  "latitude": 52.52,
  "sortOrder": 1
}
```

#### `PUT /api/my/locations/{id}`
- Purpose: updates a private location for the authenticated user
- Access: authenticated user

Request body:
```json
{
  "name": "Berlin",
  "country": "Germany",
  "region": "Berlin",
  "longitude": 13.4050,
  "latitude": 52.5200
}
```

Response body:
```json
{
  "id": 24,
  "name": "Berlin",
  "country": "Germany",
  "region": "Berlin",
  "longitude": 13.405,
  "latitude": 52.52,
  "sortOrder": 1
}
```

#### `DELETE /api/my/locations/{id}`
- Purpose: deletes a private location for the authenticated user
- Access: authenticated user

Example response:
```http
HTTP/1.1 204 No Content
```

#### `PUT /api/my/locations/order`
- Purpose: saves private location order for the authenticated user
- Access: authenticated user

Request body:
```json
[
  { "locationId": 24, "sortOrder": 1 },
  { "locationId": 25, "sortOrder": 2 }
]
```

Response body:
```json
[
  {
    "id": 24,
    "name": "Berlin",
    "country": "Germany",
    "region": "Berlin",
    "longitude": 13.405,
    "latitude": 52.52,
    "sortOrder": 1
  }
]
```

#### `GET /api/my/locations/{id}/weather`
- Purpose: returns current weather for one of the authenticated user's private locations
- Access: authenticated user

Example response:
```json
{
  "locationId": 24,
  "name": "Berlin",
  "country": "Germany",
  "region": "Berlin",
  "latitude": 52.52,
  "longitude": 13.405,
  "providerTimestamp": "2026-04-28T09:00:00",
  "description": "clear sky",
  "temperature": 16.3,
  "feelsLike": 15.7,
  "humidity": 49,
  "pressure": 1016.0,
  "windSpeed": 3.8,
  "windDeg": 195.0,
  "cloudsPercentage": 4.0
}
```

#### `GET /api/my/locations/{id}/forecast`
- Purpose: returns forecast for one of the authenticated user's private locations
- Access: authenticated user

Example response:
```json
[
  {
    "dateTime": "2026-04-28T12:00:00",
    "temperature": 17.0,
    "feelsLike": 16.4,
    "pressure": 1015.0,
    "humidity": 52.0,
    "windSpeed": 4.0,
    "windDirection": 210.0,
    "rainVolume": 0.0,
    "snowVolume": 0.0,
    "description": "few clouds",
    "precipitationProbability": 0.08,
    "cloudsAll": 18.0
  }
]
```

### 7.3 Standard error response

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
