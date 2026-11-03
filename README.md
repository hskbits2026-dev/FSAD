# Online Shopping Platform

An e-commerce platform with **Customer** and **Admin** roles, built in two phases.
See [fsad.md](fsad.md) for the source-of-truth requirements and
[docs/superpowers/specs/](docs/superpowers/specs/) for the design spec.

## Repository layout (monorepo)

```
FSAD/
├── fsad.md                     # Requirements — single source of truth
├── docs/superpowers/specs/     # Design specs (cover both phases)
├── online-shopping/            # Phase 1: Spring Boot REST API + MySQL
└── frontend/                   # Phase 2: ReactJS UI (added later)
```

## Phases

- **Phase 1 — Backend + DB** *(in progress)*: Spring Boot REST API, MySQL, JWT auth,
  Stripe checkout + webhook, email confirmation, WebSocket admin dashboard, Swagger UI.
- **Phase 2 — UI**: ReactJS single-page app consuming the API.

## Tech stack

Java 21 · Spring Boot 4.1.1 · Spring Data JPA / Hibernate · MySQL · Spring Security (JWT) ·
springdoc-openapi (Swagger) · Stripe · WebSocket (STOMP) · ReactJS (Phase 2).

## Local development (Phase 1)

### Prerequisites

- **Java 21+** (`java -version`)
- **MySQL** — the app needs a running MySQL on `localhost:3306`.
  On macOS, install via Homebrew: `brew install mysql`.

### MySQL (the database)

```bash
brew services start mysql      # start MySQL (runs in background, restarts at login)
brew services stop mysql       # stop MySQL
brew services restart mysql    # restart MySQL
brew services list             # check status (look for: mysql  started)

mysql -u root -proot           # open a SQL prompt (default local password: root)
```

The app connects as `root` / `root` by default and **auto-creates** the `online_shopping`
database and its tables on first start. To use different credentials, set `DB_USERNAME` /
`DB_PASSWORD` (see `online-shopping/src/main/resources/application.properties`).

> If MySQL was installed with an empty root password, set it once:
> `mysql -u root --skip-password -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';"`

### The project (the Spring Boot app)

```bash
cd online-shopping
./mvnw spring-boot:run         # run the app (needs MySQL running); Ctrl+C to stop
./mvnw test                    # run tests (no MySQL needed — uses in-memory H2)
./mvnw clean package           # build a runnable jar
```

Swagger UI (API explorer): `http://localhost:8080/swagger-ui.html`

Default seeded admin login: `admin@shop.com` / `Admin@123`.
