# GameSphere database plan

## Sprint 1 core schema

- `users` — application accounts and profile identity.
- `roles` — authorization roles.
- `user_roles` — many-to-many user/role mapping.
- `refresh_tokens` — revocable refresh-token records.

Later domains will add games, platforms, genres, game sources, listings, chat/community, and marketplace entities.

The schema is intentionally introduced incrementally through Flyway migrations so that application code and database changes remain versioned together.
