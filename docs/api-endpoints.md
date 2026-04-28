# API endpoints

The application exposes REST endpoints for shared locations, user-specific private locations, current weather data and weather forecasts. Shared locations are globally visible and managed by administrators, while private locations belong to the authenticated user.

A subset of endpoints is currently used by the frontend. Additional backend endpoints are implemented and documented below, but are not yet exposed in the shipped UI.

## Endpoints used by the current frontend

### `GET /login`
- Purpose: returns the login page

Example request:
```http
GET /login HTTP/1.1
Host: localhost:8080
```

Example response:
```http
HTTP/1.1 200 OK
Content-Type: text/html
```

### `POST /login`
- Purpose: performs form-based authentication handled by Spring Security

Example request:
```http
POST /login HTTP/1.1
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded

username=user1&password=User1Test!
```

Example response:
```http
HTTP/1.1 302 Found
Location: /
```

### `POST /logout`
- Purpose: logs out the current user and redirects to the home page

Example request:
```http
POST /logout HTTP/1.1
Host: localhost:8080
```

Example response:
```http
HTTP/1.1 302 Found
Location: /
```

### `GET /api/auth/me`
- Purpose: returns current authentication state, username and roles

Example request:
```http
GET /api/auth/me HTTP/1.1
Host: localhost:8080
Accept: application/json
```

Example response:
```json
{
  "authenticated": true,
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

### `GET /api/locations`
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

### `POST /api/locations`
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

### `PUT /api/locations/{id}`
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

### `DELETE /api/locations/{id}`
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

### `PUT /api/locations/order`
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

### `GET /api/locations/search?q={query}`
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

### `GET /api/weather?locationId={locationId}`
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

### `GET /api/weather/forecast?locationId={locationId}`
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

### `GET /api/my/locations`
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

## Additional backend endpoints currently not called by the shipped UI

### `POST /api/my/locations`
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

### `PUT /api/my/locations/{id}`
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

### `DELETE /api/my/locations/{id}`
- Purpose: deletes a private location for the authenticated user
- Access: authenticated user

Example response:
```http
HTTP/1.1 204 No Content
```

### `PUT /api/my/locations/order`
- Purpose: saves private location display order for the authenticated user
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

### `GET /api/my/locations/{id}/weather`
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

### `GET /api/my/locations/{id}/forecast`
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

## Standard error response

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
