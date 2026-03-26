# Weather 2.0

# 1. Running the project locally in IDE mode

In this mode:
- PostgreSQL runs in Docker
- the Spring Boot application runs from IntelliJ IDEA

### Start the database
```shell
docker compose -f docker-compose.dev.yml -p weather-dev up -d
```
### Stop the database
```shell
docker compose -f docker-compose.dev.yml -p weather-dev down
```
### Stop the database and remove its volume
```shell
docker compose -f docker-compose.dev.yml -p weather-dev down -v
```


# 2 Running the project locally in full Docker mode

In this mode:
PostgreSQL runs in Docker
the Spring Boot application also runs in Docker
Build the application
```shell
mvn clean package
```

### Start the full stack
```shell
docker compose -f docker-compose.full.yml -p weather-full up -d --build
```
### Stop the full stack
```shell
docker compose -f docker-compose.full.yml -p weather-full down
```
### Stop the full stack and remove its volumes
```shell
docker compose -f docker-compose.full.yml -p weather-full down -v
```


**Application URL**

When the full Docker stack is running, the application is available at:

http://localhost:8080

When running in IDE mode, the application is also available at:

http://localhost:8080

provided that the Spring Boot application is started from IntelliJ IDEA.