# everystore — Presentation Script (Revised)
### Final Project Demonstration · Full-Stack Application Development (FSAD)

---

## Presentation Overview

| Item | Detail |
|---|---|
| **Slides** | 10 |
| **PPT target** | ~12 minutes |
| **Live demo** | ~10 minutes |
| **Q&A** | ~8 minutes |
| **Total session** | ~30 minutes |
| **Primary presenter** | Karan Naik |
| **Teammates** | Himank Sharma · Saheb Singh |

**How to use this script:** Read it aloud during rehearsal — not silently. The goal is to internalise the ideas so the final delivery sounds like you're explaining your own project, not reading from notes.

---

## Slide-Wise Time Allocation

| Slide | Topic | Target |
|---|---|---:|
| 1 | Title | 25 sec |
| 2 | Introduction | 1 min |
| 3 | Problem & Objectives | 1 min |
| 4 | Proposed Solution | 1 min |
| 5 | System Architecture *(diagram)* | 2 min 30 sec |
| 6 | Technologies Used | 45 sec |
| 7 | Application Modules | 1 min 30 sec |
| 8 | Users & Workflow *(diagram)* | 2 min |
| 9 | Data Model *(diagram)* | 1 min 30 sec |
| 10 | Conclusion | 30 sec |
| — | Natural pauses & transitions | ~30 sec |
| **Total** | | **~11 min 45 sec** |

---

## Slide-by-Slide Speaking Script

---

### Slide 1 — Title
**Target: 25 seconds**

> *Pause. Make eye contact before speaking.*

"Good [morning / afternoon]. I'm Karan Naik — with me are Himank Sharma and Saheb Singh. We're presenting **everystore**, our FSAD full-stack project: an online shopping platform with integrated Stripe payments and a real-time admin dashboard."

> *Advance to Slide 2.*

---

### Slide 2 — Introduction
**Target: 1 minute**

"everystore is a web-based e-commerce platform built for three types of users.

A **visitor** can browse the entire product catalogue and read per-product reviews without creating an account — we didn't want to put a login wall in front of casual browsing.

Once someone registers, they become a **Customer**: they can wishlist products, manage a cart, select a delivery address, pay through Stripe, track their orders, and manage their profile.

The **Admin** account is provisioned automatically when the system starts — there's no public admin signup. Admins manage the product catalogue, view sales analytics and reports, have a customer overview with KPI data, and watch a live feed of orders, stock alerts, and feedback in real time.

The separation of roles is enforced at the API level, not just the UI — but we'll see that in the architecture slide."

> *Advance to Slide 3.*

---

### Slide 3 — Problem & Objectives
**Target: 1 minute**

"This slide captures why the application exists.

The problem: small retailers need 24/7 online sales, but manual order and stock handling is slow and error-prone, it gives no live view of what's selling, and handling card payments in-house is a security risk.

Our objectives map directly onto those pain points. We wanted a catalogue anyone can browse freely, secure role-based access, a complete purchase workflow from wishlist through to paid order, **automatic stock and sales updates so the admin never manually reconciles anything**, and analytics and customer insights on demand.

The automation is probably the most important objective — and it's worth noting it has to be reliable even under failure conditions, like if the same payment confirmation arrives twice. We'll come back to how we solved that."

> *Advance to Slide 4.*

---

### Slide 4 — Proposed Solution & Key Features
**Target: 1 minute**

"The solution is a React single-page app talking to a Spring Boot REST API backed by MySQL, with Stripe handling payments and Mailtrap handling email.

The nine features on this slide span all three user types. For public access: catalogue browsing and product reviews. For customers: wishlist, cart, delivery address selection, secure checkout, and order history. For admins: product management, analytics, the customer list, and the live dashboard.

The standout feature is **automatic, repeat-safe order fulfilment**. When Stripe confirms a payment, our backend receives a webhook and executes a single database transaction: decrement stock, mark the order paid with the shipping address snapshot, clear the cart, send the confirmation email, and push a live notification to the admin dashboard. If the same webhook arrives a second time, the system detects the duplicate and exits cleanly — nothing is processed twice."

> *Advance to Slide 5.*

---

### Slide 5 — System Architecture
**Target: 2 minutes 30 seconds**

> *This is the most important technical slide. Walk through the diagram zone by zone. Point to each layer as you name it.*

"This diagram shows the full architecture — let me walk you through it layer by layer.

**Starting at the top: the browser.** This is where the customer or admin sits. The browser runs our React single-page application. Notice that the Stripe Hosted Checkout page is shown here too — but it's on Stripe's servers. The browser is simply redirected there to pay, and card details never touch our system.

> *Point to the FRONTEND box.*

**Moving down: the frontend layer.** This is our React app, built with Vite. It has pages and components for every screen, React Router for client-side navigation, and three shared state contexts — one for authentication, one for the cart, one for the wishlist. Every API call goes through a central `fetch` wrapper that attaches the JWT token automatically. For the admin's live feed, there's a separate WebSocket client using the STOMP protocol.

> *Point to the two arrows between frontend and backend.*

**The communication layer connects the two tiers.** There are two distinct channels. Regular requests — browse products, update the cart, place an order — travel over **REST and JSON** on standard HTTP. The admin's live dashboard uses a separate **WebSocket with STOMP**, which allows the server to *push* events to the browser the moment they happen, without the browser polling.

> *Point to the BACKEND box.*

**The backend layer is a Spring Boot application on Java 21.** It has three main responsibilities. First, the **security components**: a JWT filter runs on every request, validates the token, and sets the user's role; a configuration class defines which endpoints are public and which require `ROLE_ADMIN`; and a STOMP interceptor separately authenticates WebSocket connections. Second, the **controllers** — one per area — receive the HTTP requests and delegate. Third, the **services** contain the business logic, and they in turn call repositories to read and write data through JPA and Hibernate.

> *Point to the DATABASE box.*

**At the bottom: MySQL.** JPA generates the schema from our entity classes, so the code and the database stay in sync automatically. For the test suite we swap MySQL for H2 — an in-memory database — so tests run with no external dependencies.

> *Point to the STRIPE and MAILTRAP boxes on the side.*

**Two external services sit outside the main tiers.** Stripe runs the hosted checkout and calls back our backend with a signed webhook to confirm payments. Mailtrap sends the order confirmation emails over HTTPS. Both integrations are hidden behind interfaces — in tests, mock implementations replace them so we never make real API calls during testing.

The two key architectural decisions here are **stateless JWT auth** — the server keeps no session, any instance can serve any request — and **webhook-driven fulfilment** — payments and stock updates are always triggered by a confirmed, signature-verified event from Stripe."

> *Advance to Slide 6.*

---

### Slide 6 — Technologies Used
**Target: 45 seconds**

"The stack is grouped into four categories.

**Frontend:**
- React 19 — builds the interactive single-page user interface
- Vite 8 — fast build tool and development server
- React Router 7 — manages client-side navigation between pages
- STOMP.js — WebSocket client for the real-time admin feed

**Backend:**
- Java 21 — the language the entire backend runs on
- Spring Boot 4.1.1 — core framework assembling the backend
- Spring Web / Security / Data JPA — REST endpoints, auth enforcement, database access
- WebSocket — server-push channel for the live dashboard
- JWT — creates and validates stateless login tokens

**Database:**
- MySQL 8 — production relational database storing all data

**External and tools:**
- Stripe — hosts the secure payment checkout page
- Mailtrap — delivers order confirmation emails to customers
- Maven — compiles, tests, and packages the backend
- Swagger / OpenAPI — auto-generates browsable API documentation
- BCrypt — hashes passwords before storing in the database
- Lombok — reduces Java boilerplate like getters and setters"

> *Advance to Slide 7.*

---

### Slide 7 — Application Modules
**Target: 1 minute 30 seconds**

"The application is organised into 14 modules, grouped by who they serve.

The **public and core** group — Authentication and Registration, the Product Catalogue, and Email Notification — underpins everything.

The **customer** group has six modules: Shopping Cart, Checkout and Payment, Order Management, Wishlist, Profile and Address Management, and Feedback and Reviews.

The **admin** group has five: Product Management, Customer Management, Sales Analysis, Reports, and the Real-Time Dashboard.

What's important is the **layered flow** shown at the bottom of this slide: Controllers → Services → Repositories → Database. A Controller receives the HTTP request and routes it. A Service holds the business logic and transaction boundaries. A Repository talks to the database through JPA. This layering means every module can be tested independently — for example, the Order Service is tested with a mock Stripe gateway, so there are no real payment calls in the test suite.

The **Checkout and Payment module** is the central hub. It's the only point where the catalogue, cart, address selection, orders, email, and the live dashboard all interact simultaneously."

> *Optional handover to teammate — see Team Coordination section.*
> *Advance to Slide 8.*

---

### Slide 8 — Users & Workflow
**Target: 2 minutes**

> *Walk through the diagram from top to bottom. Point to each decision node and flow path as you describe it.*

"This diagram maps the actual user journeys through the frontend application. Let me walk you through it.

> *Point to the top of the diagram — the entry and PUBCHK decision node.*

**Entry.** When the app loads, the first decision the router makes is: is this a public route? The catalogue (`/`) and the product detail page (`/products/:id`) are public — they bypass the authentication gate entirely. That's the left branch of that first decision. Everything else goes through the ProtectedRoute gate on the right.

> *Point to the GATE decision node and the AUTH subgraph.*

**Authentication gate.** If a valid JWT token is in local storage, the user proceeds. If not, they're sent to the login page, and their intended destination is remembered so they return there after signing in. The register page always creates a customer account.

> *Point to the CUST subgraph — trace the customer flow.*

**Customer journey.** From the catalogue, a customer can browse, heart products into the wishlist, and add items to the cart. The Cart page requires selecting a delivery address before checkout is enabled — this is a required step. Clicking checkout posts to our API, which creates a pending order and redirects the browser to Stripe's hosted checkout page. After payment, Stripe calls back our webhook, the order is fulfilled, and the customer lands on the success page with a link to their order history.

Notice that the Orders page and the Wishlist and Profile pages are only accessible to authenticated customers — the admin does not see these navigation items.

> *Point to the STRIPE, SUCCESS, CANCEL boxes.*

**Payment outcomes.** A successful payment goes to the success screen. If the customer cancels at Stripe, they land on the cancel page and can return to their cart — the pending order remains unpaid and the stock is not decremented.

> *Point to the ADMIN subgraph.*

**Admin journey.** The admin dashboard is a single route with four tabs: Products for catalogue management, Reports for sales analytics, Customers for the KPI view, and Live for the WebSocket feed.

> *Point to the NAV node at the bottom.*

**The header navigation** is persistent on every page and renders differently by role — customers see Shop, Orders, Wishlist, Feedback, and a profile link. Admins see Shop and Admin. Unauthenticated visitors see only Shop and the login/signup links."

> *Advance to Slide 9.*

---

### Slide 9 — Data Model
**Target: 1 minute 30 seconds**

> *Walk the ER diagram from the central entities outward, then highlight the key design decisions.*

"The ER diagram shows all nine tables. Let me walk through it systematically.

> *Point to USERS and PRODUCTS.*

**The two central entities are USERS and PRODUCTS.** Almost every other table connects to one or both of them.

> *Trace the USERS relationships.*

**From USERS outward:** a user has at most one cart, many orders, many feedback entries, many saved addresses, and many favourited products. The `addresses` and `favourites` tables are the two we added beyond the original requirements.

> *Point to CARTS → CART_ITEMS and ORDERS → ORDER_ITEMS.*

**Cart and order items** follow the same pattern: a junction table between the parent (cart or order) and the product, holding the quantity. The critical addition in `order_items` is `price_at_purchase` — the price at the moment the order was created. If a product's price changes later, the historical order still shows what the customer actually paid.

> *Point to the ship_* columns on ORDERS.*

**On the ORDERS table itself**, there are seven `ship_*` columns — recipient name, address lines, city, state, pincode. These are a **snapshot** of the delivery address at checkout time. If the customer later edits or deletes their address, the order history is unaffected. This is a standard e-commerce design pattern.

> *Point to FAVOURITES.*

**FAVOURITES** is a pure junction table — user, product, and a timestamp. A unique constraint on `(user_id, product_id)` makes the heart action idempotent: clicking heart twice doesn't create duplicate rows.

> *Point to FEEDBACK and the nullable product_id.*

**FEEDBACK** serves dual purpose. When `product_id` is null, it's a general site feedback submission. When `product_id` has a value, it's a product review for that specific product. One table, two use cases, no duplication."

> *Advance to Slide 10.*

---

### Slide 10 — Conclusion
**Target: 30 seconds**

"To close: everystore is a complete, working, role-aware e-commerce system — public browsing, Stripe payments with automatic fulfilment, wishlist, delivery addresses, reviews, admin analytics, customer management, and a real-time WebSocket dashboard. All integrated and consistent.

The central benefit is that stock, orders, and payments are always in sync automatically — there's no manual reconciliation step, no batch job, no way for the data to drift.

We're happy to take questions, and after that Himank and Saheb will demonstrate the live application."

---

## Optional Team Coordination & Handovers

These are optional. The script above is complete and coherent if you present everything yourself.

---

### Handing over during Slide 7 — Modules (backend explanation)

> After explaining the module groupings and the layered flow:

"I've walked you through how the modules are organised. I'll hand over to **[Himank / Saheb]** now, who worked closely on the backend services and can give you a deeper look at the checkout and payment flow."

---

### Handing over for the live demo (after Slide 10)

"That concludes the presentation slides. **[Himank / Saheb]** is now going to walk you through the live application — starting with the customer checkout flow and then the admin live dashboard."

OR for a focused module demo:

"I'll hand over to **[Himank / Saheb]** to demonstrate the full Stripe payment flow — from the cart through to the order appearing on the admin dashboard in real time."

---

### Resuming after a teammate

"Thank you, [Himank / Saheb]. So as you just saw, [one sentence summarising the demo]. We're now happy to take questions from the evaluators."

---

### Suggested split (if dividing the presentation)

| Section | Natural owner |
|---|---|
| Slides 1–4 (intro, problem, solution) | Karan |
| Slides 5–6 (architecture, tech) | Himank or Karan |
| Slide 7 (modules) + backend walkthrough | Saheb or Himank |
| Slides 8–10 (workflow, DB, conclusion) | Karan to close |
| Live application demo | Himank + Saheb |

---

## Diagram Coverage Checklist

| Slide | Diagram | Status | Covered in revised script |
|---|---|---|---|
| 5 | **System Architecture** — three-tier layout with frontend, backend, DB, Stripe, Mailtrap, two communication channels, security components | Required improvement — original script named layers without walking through the diagram visually | ✅ Full zone-by-zone walk-through added |
| 8 | **Application Workflow (UI Flow)** — routing decisions, public vs. protected paths, customer journey, Stripe payment flow, admin dashboard tabs, header nav | Required improvement — original described journeys in prose but did not guide through the diagram | ✅ Node-by-node walk-through added with pointer directions |
| 9 | **Database ER Diagram** — 9 entities and all relationships | Required improvement — original described tables in paragraphs without tracing the diagram visually | ✅ Entity-by-entity walk-through with snapshot pattern highlights added |

*No other meaningful diagrams are present in the presentation. Slides 1–4, 6, 7, and 10 are text/bullet layouts.*

---

## Likely Evaluation Questions & Suggested Answers

### Architecture & Design

**Q: Why did you use stateless JWT authentication instead of sessions?**
A: Sessions require the server to store session state — which works for a single instance but becomes a problem if you scale horizontally. JWT is stateless: the token carries the user's identity and role, the server just validates the signature. No database lookup on every request, and any server instance can handle any request.

**Q: What happens if the Stripe webhook fires twice for the same payment?**
A: The `markOrderPaid` method is idempotent. It looks up the order by the Stripe session ID. If the order is already PAID, it returns immediately without doing anything. A duplicate webhook is a complete no-op. This is a standard webhook safety pattern.

**Q: Why is the Stripe webhook endpoint public — no JWT required?**
A: Because the caller is Stripe's servers, not a browser with a JWT. We authenticate it differently — by verifying the cryptographic signature Stripe attaches to every webhook payload. If the signature doesn't match our secret, we reject it before touching any data.

**Q: Why is the Catalog public but Cart/Checkout requires login?**
A: We wanted zero friction for browsers. Product listings have no sensitive data. Cart and checkout operate on personal data — addresses, orders, payment — so authentication is appropriate there.

---

### Database

**Q: Why store the shipping address as a snapshot rather than a foreign key?**
A: Because addresses can change. If we stored only a foreign key and the customer later edited or deleted their address, the historical order would show wrong information. Snapshotting the full address at checkout time makes the order record immutable and permanently accurate.

**Q: Why does the feedback table serve dual purposes?**
A: Both general feedback and product reviews are the same data structure — user, rating, message, timestamp. The only difference is whether a product is associated. Using one table with a nullable `product_id` avoids duplication and keeps the query pattern consistent.

**Q: Why store the price in order_items rather than referencing the product?**
A: Product prices change. If we referenced the live price, old orders would show the current price, not what the customer actually paid. `price_at_purchase` locks the value at order creation time.

**Q: How is the favourites table structured?**
A: A pure junction table — `user_id`, `product_id`, `created_at`. A unique constraint on `(user_id, product_id)` means hearting the same product twice is idempotent rather than creating duplicate rows.

---

### Authentication & Security

**Q: How does WebSocket authentication work — why not use the HTTP-level JWT from the upgrade request?**
A: Spring's WebSocket implementation doesn't reliably propagate the HTTP-level authentication principal to subsequent STOMP frames. So we authenticate at the STOMP layer instead. The STOMP CONNECT frame carries an `Authorization: Bearer` header; our interceptor validates it and stashes the principal in the STOMP session attributes. When the client then subscribes to an admin topic, we read the principal back and check for `ROLE_ADMIN`.

**Q: Why BCrypt over something like SHA-256?**
A: SHA-256 is a fast hash — fast is bad for passwords because an attacker can compute billions of hashes per second. BCrypt is intentionally slow with a configurable cost factor. It's specifically designed for password storage.

---

### Technical Decisions & Trade-offs

**Q: Why Spring Boot rather than something lighter like Express or FastAPI?**
A: Spring Boot gives us REST endpoints, security filters, database ORM, WebSocket, transaction management, and test infrastructure in one coherent ecosystem. It also demonstrates enterprise Java patterns appropriate for the FSAD course.

**Q: Why MySQL rather than a NoSQL database?**
A: The data is inherently relational — users have carts, carts have items, items reference products. The relationships and referential integrity that SQL foreign keys provide are the right fit. A document database would have forced us to either duplicate data or handle consistency manually.

**Q: How do you prevent overselling when two customers buy the last unit simultaneously?**
A: We use optimistic locking on the Product entity — the `version` column. When the checkout decrements stock, JPA checks that the version hasn't changed since the read. If two requests race, one succeeds and the other gets an optimistic lock exception and fails gracefully.

**Q: Why H2 for tests instead of a test MySQL database?**
A: H2 starts in-memory in milliseconds with no external process. The test profile runs H2 in MySQL compatibility mode, so the same JPA entities and queries work without change. The full test suite runs anywhere with no infrastructure setup.

---

### Limitations & Future Scope

> *The Future Scope block was removed from Slide 10 in the final PPT. These questions are retained because evaluators may still raise them — be ready to answer confidently even though the slide no longer covers this topic.*

**Q: What are the current limitations?**
A: Three. No keyword search — products are browsed by category only. No discount codes. And the WebSocket broker is in-memory, so you can only run one application instance; for horizontal scaling you'd need an external broker like RabbitMQ.

**Q: Why wasn't keyword search implemented?**
A: It's a realistic future enhancement. It would require a `FULLTEXT` index on the product name and description columns in MySQL, a new repository query using `MATCH ... AGAINST`, and a search input on the frontend. It was deprioritised in favour of delivering the full purchase workflow completely.

**Q: Is the application production-ready?**
A: For a single-instance deployment — yes. It has proper security, environment-variable-based secrets with no hardcoded credentials, a full test suite, and auto-generated API documentation. For genuinely high-traffic deployment you'd add an external message broker, horizontal scaling, and CDN-hosted assets — which are the noted future scope items.

---

## Final Rehearsal Checklist

- [ ] Run through the full script aloud at least twice — time yourself against the per-slide targets.
- [ ] Practice the three diagram walk-throughs (slides 5, 8, 9) separately — these need smooth pointer movements.
- [ ] Confirm all three team member names and roll numbers on Slide 1 are correct.
- [ ] Confirm the diagram slides (5, 8, 9) are legible at the display resolution you'll use on the day.
- [ ] Agree with Himank and Saheb in advance: who does the live demo and what exactly they'll demonstrate.
- [ ] Have the app running and tested before the session starts — confirm Stripe test mode is active.
- [ ] Review the Q&A section — especially the webhook idempotency, WebSocket auth, and snapshot pattern questions.
- [ ] Confirm the course label "FSAD" matches what the evaluators expect.
- [ ] Time check: target ≤12 minutes for slides, ≤10 for demo, ≤8 for Q&A.
