# everystore
### A Full-Stack Online Shopping Platform
*Online shopping with secure payments and real-time sales monitoring*

**Course:** Full-Stack Application Development (FSAD) — Academic Project Overview

**Team:** Himank Sharma (2025sl70020) · Saheb Singh (2025sl70010) · Karan Naik (2025sl70007)

---

An e-commerce platform with **Customer** and **Admin** roles, built in two phases.
See [requirement.md](requirement.md) for the source-of-truth requirements and
[docs/superpowers/specs/](docs/superpowers/specs/) for the design spec.

## Features

**Public:** catalogue browsing · product details · read reviews.

**Customer:** wishlist / favourites · shopping cart · delivery address management ·
secure Stripe checkout · order history & tracking · profile · feedback & product reviews.

**Admin:** product management (CRUD) · sales analytics & reports · customer list with KPI tiles ·
real-time dashboard (live orders, low-stock, feedback) · email confirmations.

Order fulfilment is automatic and repeat-safe: on payment, stock is decremented, the order is
marked paid (with an address snapshot), the cart is cleared, a confirmation email is sent, and the
live admin dashboard is notified — all in one transaction.

## Repository layout (monorepo)

```
FSAD/
├── requirement.md             # Requirements — single source of truth
├── docs/superpowers/specs/     # Design specs (cover both phases)
├── online-shopping/            # Phase 1: Spring Boot REST API + MySQL
└── frontend/                   # Phase 2: ReactJS UI (added later)
```

## Phases

- **Phase 1 — Backend + DB** *(complete)*: Spring Boot REST API, MySQL, JWT auth,
  Stripe checkout + webhook, email confirmation, WebSocket admin dashboard, Swagger UI.
- **Phase 2 — UI** *(complete)*: ReactJS single-page app consuming the API.

## Tech stack

- **Frontend:** React 19 · Vite 8 · React Router 7
- **Backend:** Java 21 · Spring Boot 4.1.1 · Spring Web / Security / Data JPA (Hibernate) · WebSocket (STOMP) · JWT
- **Database:** MySQL 8
- **External & tools:** Stripe · Mailtrap · Maven · springdoc-openapi (Swagger) · BCrypt · Lombok

## Local development (Phase 1)

### Prerequisites

- **Java 21+** (`java -version`)
- **MySQL 8** — the app needs a running MySQL on `localhost:3306`.
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

### Email (Mailtrap) — optional

Order-confirmation emails are sent via [Mailtrap](https://mailtrap.io/). The app has dev-only
fallbacks, so it starts without any mail config. To actually deliver emails, set `MAILTRAP_API_TOKEN`
and `MAILTRAP_INBOX_ID` (see `online-shopping/src/main/resources/application.properties`).

## Running the frontend (Phase 2 — ReactJS)

The UI is a [Vite](https://vite.dev/) + React single-page app in `frontend/`. In development it
runs its own dev server on **port 5173** and **proxies** `/api` and `/ws` calls through to the
backend on **port 8081** — so the browser only ever talks to `:5173` and there are no CORS issues.

### Prerequisites

- **Node.js 20.19+ or 22.12+** and npm (`node -v`, `npm -v`). On macOS: `brew install node`.
  (Vite 8 requires Node 20.19+ / 22.12+; Node 18 is not supported.)
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

### Stripe logins

```bash
stripe login          # once, opens browser to authorize
stripe listen --forward-to localhost:8081/api/stripe/webhook
```
