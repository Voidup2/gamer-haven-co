# GameSphere Backend

Spring Boot backend for the GameSphere gaming platform.

## Requirements

- Java 21
- Maven 3.9+
- Docker Desktop (recommended for PostgreSQL)

## Start PostgreSQL

From the repository root:

```bash
docker compose -f docker/docker-compose.yml up -d
```

## Run the backend

From `backend/`:

```bash
mvn spring-boot:run
```

The API runs on `http://localhost:8080`.

Health endpoint:

```text
GET /api/v1/health
```

Expected response:

```json
{
  "success": true,
  "message": "GameSphere backend is running",
  "data": {
    "status": "UP",
    "service": "gamesphere-backend"
  }
}
```
