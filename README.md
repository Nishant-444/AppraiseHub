# AppraiseHub - Employee Appraisal Platform

[![Backend](https://img.shields.io/badge/Backend-Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](backend)
[![Frontend](https://img.shields.io/badge/Frontend-Next.js-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)](frontend)

**Version:** 0.0.1-SNAPSHOT  
**Status:** Development  
**Local API Endpoint:** [http://localhost:8080](http://localhost:8080)  
**Tech Stack:** Java 25, Spring Boot 4.0.4, Spring Security, JPA/Hibernate, MySQL, JWT, Next.js 16.2.0, React 19.2.4,
TypeScript 5.7.3, Tailwind CSS 4.2, Docker

![Java](https://img.shields.io/badge/Java-25-007396?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4169E1?style=for-the-badge&logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Next.js](https://img.shields.io/badge/Next.js-16.2.0-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)
![React](https://img.shields.io/badge/React-19.2.4-61DAFB?style=for-the-badge&logo=react&logoColor=000000)
![TypeScript](https://img.shields.io/badge/TypeScript-5.7.3-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![Tailwind](https://img.shields.io/badge/Tailwind%20CSS-4.2-38B2AC?style=for-the-badge&logo=tailwindcss&logoColor=white)

---

## Project Overview

AppraiseHub is a full-stack employee appraisal platform with a Spring Boot REST API and a Next.js dashboard. It supports
appraisal cycles, goals, role-based access, and reporting, built with a clean layered monolith backend and a modern
component-driven frontend.

**Modules:**

- **Backend:** Spring Boot API with JWT authentication, JPA persistence, and email notifications.
- **Frontend:** Next.js app with React 19, TypeScript, Tailwind CSS, and Radix UI components.

---

## System Architecture

The backend follows a clean layered monolith with clear separation of controller, service, and repository layers.
Authentication is stateless via JWT, and DTOs isolate persistence entities from API responses.

**High-Level Flow:**

```
Client UI (Next.js) -> Spring Boot API -> MySQL
```

---

## Core Technology Stack

| Layer     | Technology                   | Version         | Purpose                               |
|-----------|------------------------------|-----------------|---------------------------------------|
| Runtime   | OpenJDK                      | 25              | Backend runtime                       |
| Framework | Spring Boot                  | 4.0.4           | REST API and dependency injection     |
| Security  | Spring Security + JJWT       | 6.x + 0.12.6    | JWT authentication and RBAC           |
| ORM       | Hibernate (JPA)              | 6               | Data persistence                      |
| Database  | MySQL                        | 8+              | Relational data storage               |
| Build     | Maven                        | -               | Backend build and packaging           |
| Frontend  | Next.js + React + TypeScript | 16.2.0 + 19.2.4 | UI framework and type safety          |
| Styling   | Tailwind CSS + Radix UI      | 4.2 + -         | UI styling and primitives             |
| Container | Docker                       | -               | Local and production containerization |

---

## Database Schema

The schema is designed to support multi-step appraisal cycles. Core entities include Users, Departments, Appraisals, and
Goals, with strict constraints to prevent duplicate cycles per employee.

---

## API Architecture

The API is JWT-secured and role-aware (HR, MANAGER, EMPLOYEE). Requests are authenticated via the `Authorization` header
and validated by a Spring Security filter chain. DTOs isolate persistence entities from API responses.

---

## Security Implementation

### Authentication & Authorization

**Authentication:**

- Spring Security 6.x with JWT (JJWT 0.12.6)
- BCrypt password hashing
- Role-based access control for HR, MANAGER, and EMPLOYEE

### Input Validation

- `jakarta.validation` for request validation
- Global exception handling via `@RestControllerAdvice`

---

## Deployment Architecture

The backend includes a multi-stage Docker build and a production compose file.
See [appraisal-backend/Dockerfile](appraisal-backend/Dockerfile)
and [appraisal-backend/docker-compose.prod.yml](appraisal-backend/docker-compose.prod.yml).

---

## Getting Started

### Prerequisites

```bash
Java 25
Maven (or Maven Wrapper)
Node.js 18+
MySQL 8+
```

### Backend (Spring Boot)

```bash
cd backend

# Windows
mvnw.cmd spring-boot:run

# macOS/Linux
./mvnw spring-boot:run
```

### Frontend (Next.js)

```bash
cd frontend
npm install
npm run dev
```

### Configuration

Development properties live in `backend/src/main/resources/application.properties`. Production defaults are in
`backend/src/main/resources/application-prod.properties`.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/appraisal_database?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=

jwt.secret=change-me
jwt.expiration=86400000

spring.mail.host=
spring.mail.port=
spring.mail.username=
spring.mail.password=
```

---

## Testing

```bash
cd backend
./mvnw test
```

```bash
cd frontend
npm run lint
```

---

## Project Structure

```
AppraiseHub/
├── backend/
│   ├── src/main/java/           # Spring Boot source
│   ├── src/main/resources/      # application.properties
│   ├── Dockerfile               # Backend container image
│   ├── docker-compose.prod.yml  # Production compose file
│   └── pom.xml                  # Maven config
└── frontend/
  ├── app/                     # Next.js app router
  ├── components/              # UI components
  ├── lib/                     # API client and utilities
  └── package.json
```

---

## Roadmap

- Reporting dashboards for HR and managers
- More granular appraisal workflows and approvals
- Audit logging for critical HR actions
- Metrics and alerting via Spring Boot Actuator

---

## Technical Decisions

- Spring Boot with layered architecture for maintainability and testability
- JWT-based stateless auth to support web and API clients
- MySQL for relational integrity across users and appraisals
- Next.js app router for consistent UI routing and server components

---

## Monitoring & Observability

- Application logs via Spring Boot logging
- Ready for Spring Boot Actuator integration

---

## Contributing

Contributions welcome. Follow standard Git workflow:

```bash
git checkout -b feature/feature-name
# Make changes
git commit -m "Add: feature description"
git push origin feature/feature-name
# Open pull request
```

**Code Standards:**

- Backend: standard Spring Boot conventions
- Frontend: ESLint + TypeScript strict mode

---

## Spring Boot Flow

This section outlines the typical Spring Boot request & runtime flow used by the backend:

- **Client → Controller:** HTTP requests from the UI or API clients are received by `@RestController` endpoints.
- **Controller → Service:** Controllers delegate business logic to `@Service` components.
- **Service → Repository:** Services use Spring Data JPA repositories (`@Repository`) to read/write domain entities.
- **Persistence → DB:** Hibernate (JPA) translates entity operations into SQL executed against MySQL.
- **Security & Filters:** Requests pass through Spring Security filter chain (authentication/authorization) and any
  custom `OncePerRequestFilter` implementations (e.g., JWT parsing).
- **DTOs & Mapping:** Controllers expose DTOs; entity ↔ DTO mapping isolates persistence concerns from API contracts.
- **Exception Handling:** Global exceptions are handled by `@ControllerAdvice` / `@RestControllerAdvice` to standardize
  error responses.
- **Profiles & Config:** Configuration lives under `src/main/resources` and uses profiles (`application.properties`,
  `application-prod.properties`, `application-test.properties`). Activate profiles with `--spring.profiles.active=` or
  via environment variables.
- **Build & Run:** Use Maven (or the included Maven Wrapper) to build and run. Packaging produces an executable JAR
  which can be run with `java -jar` or executed in Docker.

Common commands:

```bash
# Windows (dev)
cd backend
mvnw.cmd spring-boot:run

# Package
cd backend
./mvnw package    # or mvnw.cmd package on Windows

# Run packaged jar
java -jar backend/target/appraisal-0.0.1-SNAPSHOT.jar
```

---

## Current Project Flow

This project (`AppraiseHub`) follows the flow below when developing, building, and deploying:

- **Local dev:**
    - Start the backend in dev mode using the Maven wrapper: `backend/mvnw.cmd spring-boot:run` (Windows) or
      `./backend/mvnw spring-boot:run` (macOS/Linux).
    - Start the frontend with Next.js: `cd frontend && npm install && npm run dev`.
- **Configuration:**
    - Backend
      properties: [backend/src/main/resources/application.properties](backend/src/main/resources/application.properties)
      and profile-specific files (e.g. `application-prod.properties`).
    - Secrets and environment-specific values are provided via environment variables when running in containers or CI.
- **Build & Package:**
    - Backend: `./mvnw package` produces the application artifact at `backend/target/` (e.g.
      `appraisal-0.0.1-SNAPSHOT.jar`).
    - Frontend: `npm run build` in `frontend` to produce the optimized Next.js output.
- **Containerization & Deployment:**
    - Backend Dockerfile and compose files live in the `backend/` folder. Use
      `docker build -t appraisehub-backend:latest -f backend/Dockerfile backend` to build, and
      `docker-compose -f backend/docker-compose.yml up -d` to bring services up locally.
    - The system composes the frontend and backend (and DB) in local or production compose files as needed.
- **Runtime interactions:**
    - The frontend communicates with the backend via the REST API exposed at `http://localhost:8080` during
      development (CORS configured on the backend).
    - Authentication is handled via JWT tokens issued by the backend and sent in the `Authorization` header by the
      frontend.

If you'd like, I can also add a minimal diagram or sequence example showing a sample API call through the stack.

---

## License

ISC [License](./LICENSE)

---

## Author

**Nishant Sharma**  
GitHub: [@Nishant-444](https://github.com/Nishant-444)

---
