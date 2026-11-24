# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Teaching rule (READ FIRST — the repo owner is new to Spring Boot / Java)

Whenever you make or propose a code change in this repo, you MUST:

1. **Explain it in plain language.** Assume no prior Spring Boot / Java framework knowledge.
   Define any term the first time it appears (e.g. "a `@Service` is just a class holding business
   logic"). Prefer short sentences over precise-but-dense ones.
2. **Include an ASCII flow diagram** that shows how the request/data moves through the layers
   involved in the change, with a one-line "what this layer does" note on each box. Show the actual
   classes touched by *this* change, not a generic template. Example shape:

   ```
   HTTP request ──▶ Controller ──▶ Service ──▶ Repository ──▶ MySQL
                    (front door)   (brain /    (talks to DB:   (stored
                                    rules)      save/find)      data)
   ```
3. **Say what you changed and why**, tying each edited file back to its box in the diagram.

This applies to every change, however small. It is about teaching, not just doing.

## Project

Online shopping platform (e-commerce) with **Customer** and **Admin** roles, built in two phases.
`requirement.md` is the authoritative requirements doc; `docs/superpowers/specs/` holds the design spec
and `docs/superpowers/plans/` the per-module implementation plans. The Stripe payment flow is a
deliberate scope extension beyond `requirement.md` (which does not mention payment).

- **Phase 1 (complete)** — Spring Boot REST API + MySQL, in `online-shopping/`.
- **Phase 2 (in progress)** — ReactJS SPA, in sibling `frontend/`.

## Commands

All backend commands run from `online-shopping/` using the Maven wrapper:

```bash
cd online-shopping
./mvnw spring-boot:run          # run the app (needs MySQL on localhost:3306)
./mvnw test                     # run the full test suite
./mvnw test -Dtest=OrderServiceTest                 # single test class
./mvnw test -Dtest=OrderServiceTest#markOrderPaid*  # single test method (glob)
./mvnw verify                   # compile + test (CI-equivalent)
./mvnw clean package            # build the jar
```

- **Running the app** requires a live MySQL (`online_shopping` DB is auto-created). All secrets are
  read from env vars with dev-only fallbacks (see `application.properties`): `DB_USERNAME/PASSWORD`,
  `JWT_SECRET`, `STRIPE_SECRET_KEY/WEBHOOK_SECRET`, `MAILTRAP_API_TOKEN/MAILTRAP_INBOX_ID`, `ADMIN_EMAIL/PASSWORD`.
- **Tests require no external services**: the `test` profile (`src/test/resources/application-test.properties`)
  swaps MySQL for in-memory H2 (MySQL compatibility mode) and uses dummy Stripe/mail/JWT values.
- Swagger UI: `http://localhost:8081/swagger-ui.html`.

## Architecture

Classic layered Spring Boot REST API. Base package `com.shop.onlineshopping`, one package per layer:
`entity` → `repository` → `service` → `controller`, plus `dto`, `security`, `config`, `exception`.
Each layer talks only to the one below; **entities are never returned from controllers** — DTOs
expose data, and most DTOs have a static `from(entity)` factory.

### Authentication (stateless JWT, no Spring `UserDetailsService`)
- `JwtAuthFilter` reads the `Authorization: Bearer` header on every request, validates via
  `JwtService`, and sets a `UsernamePasswordAuthenticationToken` with authority `ROLE_<role>`.
  A bad/missing token leaves the request **unauthenticated** (no exception) — the authorization
  layer decides. There is no DB lookup in the filter; the principal is the email string from the token.
- `SecurityConfig`: CSRF off, sessions stateless. Public paths: `/api/auth/**`, `/api/stripe/webhook`,
  `/ws/**`, Swagger. `/api/admin/**` requires `ROLE_ADMIN`; everything else requires authentication.
- Controllers resolve the current `User` entity by looking up `authentication.getName()` (the email)
  via `UserRepository` — see `CheckoutController.currentUser(...)` for the pattern.
- `AdminSeeder` (a `CommandLineRunner`) creates the admin user on startup from `app.admin.*` if absent.

### Payment flow (Stripe hosted checkout + webhook) — the key cross-cutting path
1. `POST /api/checkout` → `PaymentService` creates a Stripe Checkout Session and a PENDING `Order`
   keyed by the Stripe session id.
2. Stripe calls `POST /api/stripe/webhook` (public, signature-verified in `StripeGateway` **before**
   any state change). `OrderService.markOrderPaid(sessionId)` is **idempotent** (returns early if
   already PAID or session unknown) and, in one `@Transactional` unit: decrements product QOH
   (clamped at 0), flips the order to PAID, clears the cart, sends the confirmation email, and pushes
   admin notifications.
- `StripeGateway` is an interface (`StripeGatewayImpl`) so tests can stub the Stripe SDK.

### Real-time admin dashboard (WebSocket / STOMP) — note the auth quirk
- `WebSocketConfig`: STOMP endpoint `/ws`, simple broker on `/topic`, app prefix `/app`.
- `StompAuthChannelInterceptor` authenticates the **STOMP CONNECT** frame from its `Authorization`
  header (not the HTTP handshake). **Important quirk:** Spring does not propagate the CONNECT
  principal to later frames, so the interceptor stashes the principal in the STOMP session
  attributes under `STOMP_PRINCIPAL` and reads it back when authorizing SUBSCRIBE. Subscribing to
  `/topic/admin/**` requires `ROLE_ADMIN`; clients are forbidden from `SEND`ing to any `/topic/**`.
- `NotificationService` publishes to `/topic/admin/orders`, `/topic/admin/low-stock`,
  `/topic/admin/feedback`. **All sends are wrapped in try/catch and swallow failures** — a push
  must never break the caller's transaction (e.g. a paid order). Low-stock fires only when a paid
  decrement *crosses* the `app.low-stock-threshold` boundary.

### Error handling
`@RestControllerAdvice` classes in `exception/` map custom exceptions (e.g. `ResourceNotFoundException`,
`UnauthorizedException`, `EmptyCartException`) to `ApiError` responses. Throw these from services
rather than returning error codes.

## Conventions

- **Constructor injection only** (no `@Autowired` fields); dependencies are `final`.
- SLF4J logger per class: `private static final Logger log = LoggerFactory.getLogger(X.class);`.
- Lombok is available (annotation processor configured in `pom.xml`).
- Tests mirror the main package layout under `src/test/java/...`; controller tests use
  `@WebMvcTest`/MockMvc, repository tests use `@DataJpaTest`, services use plain Mockito.
