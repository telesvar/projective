# Projective – Project-Management API

Projective is a lightweight, multi-tenant REST backend that lets organisations track teams, workspaces, projects, tasks and issues with fine-grained, role-based access control.

## Table of Contents
- [Features](#features)
- [Quick Start](#quick-start)
- [Documentation](#documentation)
- [Contributing](#contributing)
- [License](#license)

## Features
- CRUD Teams, Workspaces, Projects, Tasks and Issues
- Granular role-based access control (`VIEWER`, `MEMBER`, `ADMIN`, `OWNER`, `SERVICE_ADMIN`) secured by JWT
- Kanban-style workflows with configurable state transitions
- API-first: complete OpenAPI 3 specification & Swagger UI
- Built with Spring Boot 3 & Java 21; container-ready

## Quick Start

Clone the repository:
```sh
git clone https://github.com/telesvar/projective.git
cd projective
```

Build & run (dev profile uses in-memory H2):
```sh
mvn spring-boot:run
```

Browse Swagger UI at http://localhost:8080/swagger-ui/index.html when the application starts.

## Documentation
High-level design, architecture diagrams and requirements live in [`docs/design.md`](docs/design.md).

## Contributing
Pull requests are welcome. Please open an issue first to discuss your idea or improvement.

## License
Projective is released under the 0BSD license – see [`LICENSE`](LICENSE) for full text.
