# GameSphere Architecture

## Current stack

- Frontend: React 19 + TypeScript + TanStack Router + Vite
- Backend: Spring Boot 3.5 + Java 21
- Database: PostgreSQL
- Database migrations: Flyway
- Local infrastructure: Docker Compose

## High-level flow

```text
Browser (desktop/mobile web)
        |
        | HTTPS / JSON
        v
React frontend
        |
        | REST API /api/v1
        v
Spring Boot backend
        |
        +--> Authentication & authorization
        +--> Games
        +--> Marketplace
        +--> Community
        |
        v
PostgreSQL
```

## Backend package direction

```text
com.gamesphere
├── auth
├── common
├── config
├── games
├── marketplace
├── security
└── users
```

Domain modules will be introduced incrementally. Authentication will be implemented before protected game, marketplace, and community APIs.
