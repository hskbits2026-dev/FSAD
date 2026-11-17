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

Swagger UI (API explorer): `http://localhost:8081/swagger-ui.html`

Default seeded admin login: `admin@shop.com` / `Admin@123`.

## Running the frontend (Phase 2 — ReactJS)

The UI is a [Vite](https://vite.dev/) + React single-page app in `frontend/`. In development it
runs its own dev server on **port 5173** and **proxies** `/api` and `/ws` calls through to the
backend on **port 8081** — so the browser only ever talks to `:5173` and there are no CORS issues.

### Prerequisites

- **Node.js 18+** and npm (`node -v`, `npm -v`). On macOS: `brew install node`.
- **The backend must be running first** (see above) on `http://localhost:8081`, or API calls
  (login, catalog, cart…) will fail.

### Start it

```bash
cd frontend
npm install        # first time only — downloads dependencies into node_modules/
npm run dev        # start the dev server; Ctrl+C to stop
```

Then open the URL it prints — **`http://localhost:5173`**.

Other scripts:

```bash
npm run build      # produce an optimized production build in dist/
npm run preview    # serve that production build locally to check it
npm run lint       # static checks (oxlint)
```

### Demo logins

| Role     | Email            | Password   |
| -------- | ---------------- | ---------- |
| Customer | `test@demo.com`  | `Test@123` |
| Admin    | `admin@shop.com` | `Admin@123` |

> The **Admin** link in the header (dashboard, product CRUD, reports, live feed) only appears when
> you are logged in as the admin account.

