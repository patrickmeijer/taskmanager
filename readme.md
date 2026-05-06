# Task Manager API

A RESTful Task Management API built with Java 17 and Spring Boot 4. I built this project to deepen my understanding of backend development with Spring, with a focus on security, clean API design and understanding why certain architectural choices are made.

Mapping between entities and DTOs is implemented manually, without Lombok or MapStruct. I wanted to understand what those libraries actually do before reaching for them.

> **Status:** Mostly feature-complete, with some improvements and documentation still in progress. This project is still evolving as I continue to improve my Java and Spring Boot skills.

---

## Tech Stack

| Layer       | Technology                               |
|-------------|------------------------------------------|
| Language    | Java 17                                  |
| Framework   | Spring Boot 4                            |
| Security    | Spring Security, JWT (jjwt 0.13), BCrypt |
| Persistence | Spring Data JPA, Hibernate               |
| Database    | PostgreSQL (Dockerized)                  |
| Testing     | JUnit 5, Mockito, MockMvc                |
| Build       | Maven                                    |

---

## Architecture

The project uses a **Package-by-Feature** structure instead of the traditional layered approach. Each domain (`user`, `task`, `auth`) owns its controller, service, repository, mapper, and DTOs. This improves cohesion and makes each feature independently navigable and modular.

```
com.patrick.taskmanager
├── auth        # JWT filter, AuthService, AuthController
├── config      # SecurityConfig, DataInitializer
├── exception   # GlobalExceptionHandler, custom exception hierarchy
├── task        # Full task domain
└── user        # Full user domain
```

---

## Key Design Decisions

**Manual mappers and DTOs**
Entities and DTOs are mapped by hand. I chose to do this manually to understand how the mapping actually works before using a library like MapStruct. Sensitive fields like password hashes are never exposed through the API.

**Stateless JWT authentication**
No server-side sessions. Every request is authenticated via a signed JWT. The `JwtAuthenticationFilter` validates the token and populates Spring Security's `SecurityContextHolder` before any authorization decisions are made.

**Role-Based Access Control with ownership validation**
Two roles: `ROLE_ADMIN` and `ROLE_USER`. Method-level security (`@PreAuthorize`) controls endpoint access. Regular users can only manage their own tasks and profile, enforced through a `validateOwnership` check in the service layer and an `isSelf` helper used in `@PreAuthorize` expressions.

**Strict REST semantics**
`PUT` replaces a full resource. `PATCH` is used only where it made sense for this use case: password changes and task status transitions each have their own dedicated endpoint. Status is managed server-side and intentionally excluded from the general task update.

**Centralized exception handling**
A `@ControllerAdvice` maps custom exceptions to consistent JSON error responses with the right HTTP status codes. Internal details are logged server-side and never sent to the client.

**Automated data seeding**
A `DataInitializer` seeds the database with BCrypt-hashed test accounts on startup so anyone cloning the repo can get started without manual setup.

---

## Security Model

| Endpoint                         | USER       | ADMIN    |
|----------------------------------|------------|----------|
| `POST /api/auth/login`           | ✅ Public   | ✅ Public |
| `POST /api/users`                | ✅ Public   | ✅ Public |
| `GET /api/users`                 | ❌          | ✅        |
| `GET /api/users/{id}`            | ✅ Own only | ✅        |
| `PUT /api/users/{id}`            | ✅ Own only | ✅        |
| `PATCH /api/users/{id}/password` | ✅ Own only | ✅        |
| `DELETE /api/users/{id}`         | ❌          | ✅        |
| `GET /api/tasks`                 | ✅ Own only | ✅        |
| `GET /api/tasks/admin/all-tasks` | ❌          | ✅        |
| `POST /api/tasks`                | ✅          | ✅        |
| `PUT /api/tasks/{id}`            | ✅ Own only | ✅        |
| `PATCH /api/tasks/{id}/status`   | ✅ Own only | ✅        |
| `DELETE /api/tasks/{id}`         | ✅ Own only | ✅        |

---

## Running Locally

**Prerequisites:** Java 17+, Maven, Docker

1. Clone the repository
2. Copy the env template and fill in your values:
   ```bash
   cp .env.example .env
   ```
3. Start everything with Docker Compose:
   ```bash
   mvn package -DskipTests
   docker-compose up -d
   ```

That starts both the PostgreSQL database and the application. The `DataInitializer` will seed two accounts on first startup:

| Username   | Password   | Role  |
|------------|------------|-------|
| `admin`    | `admin123` | ADMIN |
| `testuser` | `user123`  | USER  |

> To run only the database and use `mvn spring-boot:run` locally instead, just run `docker-compose up -d postgres-db` and start the app with the `local` profile.

---

## Roadmap

- [x] JWT authentication and stateless security
- [x] Role-based access control with ownership validation
- [x] Custom exception hierarchy with centralized handling
- [x] Integration tests (MockMvc) for controller security scenarios
- [x] Docker setup with Dockerfile and docker-compose
- [ ] Swagger / OpenAPI documentation
- [ ] Extended integration test coverage