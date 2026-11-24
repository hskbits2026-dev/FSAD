# Project Abstract

## 1. Project Title

**everystore — A Full-Stack Online Shopping Platform with Integrated Payments and Real-Time Sales Monitoring**

(Working application name: *everystore*. Repository: *Online Shopping Platform*.)

---

## 2. Problem Statement

Small and mid-sized retailers increasingly need an online presence, but they face two persistent difficulties. First, **customers** expect to browse a catalogue, place orders, and pay securely from any device, at any time — expectations that a physical store or a telephone/manual ordering process cannot meet. Manual or spreadsheet-based order handling is slow, error-prone, and gives no reliable record of what was sold, to whom, and when.

Second, **store administrators** need to keep the product catalogue accurate and to understand sales as they happen. In many existing setups, stock levels and sales figures are reconciled only periodically (for example, at the end of a day or week), so administrators cannot see current demand, cannot quickly tell which products are selling fast or slow, and learn about low stock only after it has become a problem. Payment handling adds further risk: storing or processing card details in-house exposes a small retailer to significant security and compliance burdens.

A dedicated, role-aware web application is therefore necessary — one that lets customers shop and pay safely online, keeps stock and order records consistent automatically, delegates card handling to a specialised payment provider, and gives administrators an accurate, up-to-the-moment view of sales and feedback. This project implements exactly such a platform.

---

## 3. Project Objectives

The primary objectives of the application are to:

1. **Provide separate, secure access** for customers and administrators, so that each role sees only the functions appropriate to it.
2. **Enable customers to browse products** by category and individually, with all key details (name, description, price, quantity on hand, and product image) — without requiring a login for browsing or reading reviews.
3. **Support a complete purchase workflow** — wishlisting products, adding and removing items in a shopping cart, selecting a delivery address, checking out, paying securely, and reviewing or deleting one's own past orders.
4. **Allow customers to manage their account** — edit their name and phone, and maintain one or more saved delivery addresses with a default selection.
5. **Allow customers to submit feedback** — both a general site rating and per-product reviews visible to any visitor.
6. **Let administrators manage the catalogue**, adding, updating, and removing products across the defined categories.
7. **Keep stock and order records consistent automatically**, decrementing stock only when a payment is confirmed and preventing overselling at checkout.
8. **Give administrators sales analysis and reporting** over configurable periods (week, month, quarter, year), broken down by product and by category, including identification of fast- and slow-moving products.
9. **Give administrators a customer view** — a list of all registered customers with KPIs (order count, lifetime spend, active/inactive status).
10. **Surface important business events in real time** — new paid orders, low-stock warnings, and new feedback — on an administrative dashboard.

**Intended outcome:** a working, end-to-end e-commerce system in which customers can shop and pay online with confidence, while administrators maintain an accurate catalogue and monitor sales and customer sentiment without manual reconciliation.

---

## 4. Proposed Solution

The application is a **full-stack web platform** consisting of a browser-based storefront and administrative dashboard (the frontend) communicating with a server-side application and database (the backend). It addresses the stated problem as follows.

**For customers**, the platform offers a registration and login facility, and a product catalogue that can be browsed and filtered by category **without requiring an account** — casual visitors can explore the shop and read per-product reviews freely. Customers add items to a personal shopping cart (quantities for a repeated item are combined), save favourite products to a **wishlist**, and review the cart. At checkout the customer selects a saved delivery address; the system validates that enough stock exists, records the order with the address snapshot, and hands the shopper to a **secure hosted payment page** operated by an external payment provider, so that card details never pass through this application. Customers can view their order history (most recent first) with the shipping address on each order, delete their own orders, and submit feedback. Customers can also manage their account — edit their name and phone number, and maintain multiple saved delivery addresses.

**For administrators**, the platform provides a dashboard to manage the catalogue (create, update, and delete products), to view sales analytics and reports over a chosen period, **to browse a customer list with KPIs** (total/active/inactive counts, lifetime revenue, per-customer order stats), and to watch a **live activity feed**. Crucially, sales records are updated **automatically and reliably**: when the payment provider confirms a payment, the system — in a single, repeat-safe operation — reduces the relevant stock, marks the order as paid, empties the customer's cart, sends an order-confirmation email, and notifies the dashboard. Low-stock alerts are raised precisely when a sale causes a product to fall to or below a configured threshold.

**Key features:**
- Role-based access for Customer and Admin, backed by token-based authentication.
- **Public catalog browsing**: products and per-product reviews are accessible without an account.
- Category-wise and product-wise catalogue browsing with pagination.
- **Per-product reviews**: any visitor can read reviews; authenticated customers can submit them.
- **Wishlist (favourites)**: customers can heart/un-heart products and view their saved list.
- Shopping cart with add, remove, and quantity handling.
- **Delivery address management**: customers save and manage multiple addresses; default is auto-selected at checkout.
- Secure, externally hosted payment and automatic, confirmation-driven order fulfilment with **shipping address snapshot** on each order.
- Automatic stock decrement, overselling prevention, and order-confirmation email.
- **Customer profile**: edit name and phone.
- Administrative product management (add / modify / delete).
- Sales analysis by product and category over week/month/quarter/year, including fast-/slow-moving product ranking.
- **Admin customer list**: KPI tiles and per-customer order/spend summary with active/inactive status.
- Sales and feedback reports.
- A real-time administrative dashboard for new orders, low-stock alerts, and feedback.

---

## 5. Technologies Used

All technologies below are those actually present in the repository.

### Frontend
| Technology | Purpose |
|---|---|
| **React 19** | Builds the single-page user interface as reusable components. |
| **Vite 8** | Development server and build tool that bundles the React application. |
| **React Router 7** | Manages navigation between screens within the single page without full reloads. |
| **STOMP.js (`@stomp/stompjs`)** | Client library for the real-time WebSocket connection used by the admin live feed. |
| **Browser `fetch` + Context API** | Native HTTP calls to the backend and lightweight shared state (logged-in user, cart). |

### Backend
| Technology | Purpose |
|---|---|
| **Java 21** | The backend programming language. |
| **Spring Boot 4.1.1** | Core framework that assembles and runs the server-side application. |
| **Spring Web (MVC)** | Exposes the application's functionality as REST endpoints over HTTP. |
| **Spring Security** | Enforces authentication and role-based authorization. |
| **Spring Data JPA / Hibernate** | Object-relational mapping: stores and retrieves data as Java objects and generates the database schema. |
| **Spring WebSocket (STOMP)** | Pushes real-time events to the administrative dashboard. |
| **JJWT** | Creates and validates JSON Web Tokens used for stateless login. |
| **springdoc-openapi (Swagger UI)** | Generates interactive API documentation. |

### Database
| Technology | Purpose |
|---|---|
| **MySQL 8** | The production relational database that stores users, products, carts, orders, and feedback. |
| **H2 (in-memory)** | A lightweight database used only for automated tests, so tests need no external services. |

### External Services, Libraries & Tools
| Technology | Purpose |
|---|---|
| **Stripe (Java SDK)** | External payment provider handling the secure hosted checkout and confirming payments via a signed callback (webhook). |
| **Mailtrap Send API** | External service used to send order-confirmation emails over a secure HTTPS connection. |
| **Maven (wrapper)** | Builds, tests, and runs the backend. |
| **Lombok** | Reduces repetitive Java code (e.g., getters/setters) in the data classes. |
| **BCrypt** | Securely hashes customer and admin passwords before storage. |

---

## 6. Modules

The application is organised into the following major modules, each mapping to implemented functionality.

1. **Authentication & Registration Module** — Lets customers register and lets both customers and administrators log in. Issues a secure token on success, hashes passwords, and ensures self-registered users are always ordinary customers. An administrator account is created automatically when the system first starts.

2. **Product Catalogue Module (Customer)** — Presents products to customers and visitors, filtered by category and shown page by page, **publicly accessible without login**. Provides a detailed view for each product (name, description, price, quantity on hand, and product image) along with a per-product reviews section.

3. **Product Management Module (Admin)** — Allows administrators to add, update, and delete products across the five categories (Electronics, Electrical, Furniture, Cosmetics, Toys & Books), with validation of the entered details.

4. **Shopping Cart Module** — Maintains one cart per customer; supports adding products (combining quantities for a repeated item) and removing items, with ownership checks so a user can only modify their own cart.

5. **Checkout & Payment Module** — Validates stock, requires the customer to select a saved delivery address, creates a pending order with a price snapshot and the address snapshot, and redirects the customer to the external hosted payment page. On confirmed payment it reliably and repeat-safely finalises the order (reduces stock, marks it paid, clears the cart, emails confirmation, and notifies the dashboard).

6. **Order Management Module** — Lets customers view their own orders (newest first) — including the shipping address snapshot on each order — and delete their own orders, with server-side checks preventing access to other users' orders.

7. **Feedback Module** — Allows customers to submit a **general site rating** (1–5) and a message, which is stored and surfaced to administrators. The same infrastructure serves **per-product reviews** (tied to a specific product) which are readable by all visitors.

8. **Wishlist / Favourites Module** — Lets customers heart/un-heart products from product cards or the detail page; a dedicated Wishlist page shows all favourited products. The unique constraint on (user, product) makes the action idempotent.

9. **Profile & Address Management Module** — Lets customers edit their name and phone number. Customers can save multiple delivery addresses (each with label, recipient, phone, full address, and an optional line 2), set a default, and edit or delete them. The default address is pre-selected at checkout.

10. **Sales Analysis Module (Admin)** — Produces sales figures over a chosen period (week, month, quarter, year), grouped by product or by category, and ranks products by movement to highlight fast- and slow-moving items.

11. **Reports Module (Admin)** — Provides consolidated sales and customer-feedback reports for analysis.

12. **Admin Customer Management Module** — Lists all registered customers with KPI summary tiles (total, active, inactive, lifetime revenue) and a per-customer table (name, email, phone, address count, order count, lifetime spend, join date, status). Supports a soft-delete model: an INACTIVE customer's history is preserved.

13. **Real-Time Dashboard & Notifications Module (Admin)** — Streams new-order, low-stock, and feedback events to the administrator's dashboard as they occur, over a secure WebSocket connection.

14. **Email Notification Module** — Sends an order-confirmation email to the customer when a payment succeeds.

*(These modules implement and extend the seven modules named in the original requirements: Login/Registration, Products Search, Ordering, Feedback, Products Updation, Sales Analysis, and Reports.)*

---

## 7. Users and Roles

The application defines **two human roles** and interacts with **two external systems**.

### Customer
A registered shopper. Customers can register and log in; browse and filter the catalogue; view product details; add and remove cart items; check out and pay; view and delete their own orders; and submit feedback. Their access is limited to their own data — the system enforces that a customer can act only on their own cart and orders. All self-registered accounts are customers; a customer cannot grant themselves administrative rights.

### Administrator
A privileged operator. The administrator account is provisioned automatically by the system at startup (there is no public sign-up for administrators). Administrators can manage the product catalogue (add, update, delete), view sales analytics and reports, and monitor the real-time dashboard of orders, low-stock alerts, and feedback. An administrator may also use the ordinary customer-facing functions.

### External systems (non-human actors)
- **Payment provider (Stripe):** hosts the secure checkout page and confirms successful payments back to the application through a verified callback. This keeps sensitive card data entirely outside the platform.
- **Email service (Mailtrap):** receives and delivers the order-confirmation emails sent by the platform.

---

## 8. Conclusion

This project delivers a complete, role-aware **online shopping platform** that lets customers browse a categorised catalogue, manage a cart, pay securely through an external provider, and track their orders and feedback, while giving administrators the tools to manage the catalogue and to understand sales and customer sentiment in real time. Its central benefit is **reliable, automatic consistency between payments, stock, and sales records**: because order fulfilment is driven by confirmed payment and executed as a single repeat-safe operation, administrators always work from accurate data, and customers are protected from overselling.

By separating customer and administrator capabilities, delegating card handling to a specialised payment service, and surfacing business events as they happen, the application demonstrates the practical patterns of a modern e-commerce system — secure authentication, a clean separation between the user interface and the server, dependable transaction handling, and real-time monitoring. As an academic full-stack project it integrates a React frontend, a Spring Boot backend, a MySQL database, and two external service integrations into one coherent, working system, and it provides a solid foundation on which further commercial features could later be built.
