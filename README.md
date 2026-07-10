# Vehicle & Driver Registry 🚗🪪

A learning-focused, **government-style DMV (Department of Motor Vehicles) web
portal**, built with **Java 21 + Spring Boot 3.5 + Maven**. It is a
self-contained web server backed by a real (in-process) SQL database — the kind
of system a government builds when modernizing a legacy registry.

This project is written to **teach**: nearly every line is commented to explain
what it does and *why*.

---

## 🎯 The big picture

Real government modernization projects replace old "legacy" systems with modern,
layered, web-based ones. This repo is built to *show that journey*:

- It started life (see the first commit) as a plain-Java, console app with a
  hand-written in-memory store — the "fundamentals, no framework magic" version.
- It was then **modernized to Spring Boot**: the same business rules, now served
  as a **web portal** over a **real database**. Because the code was built in
  layers, that upgrade barely touched the rules — only the storage and the
  "front door" changed. That is the entire point of good architecture.

---

## 🧱 Architecture (how the code is organized)

```
src/main/java/gov/dmv/registry/
├── VehicleDriverRegistryApplication.java   the startup class (press ▶ here)
│
├── model/          The "nouns" — now JPA @Entity classes = database tables
│   ├── Person.java            a citizen / owner / license holder
│   ├── Vehicle.java           a vehicle (@ManyToOne owner  → foreign key)
│   ├── DriverLicense.java     a license (@OneToOne holder)
│   ├── VehicleType.java       enum: CAR, TRUCK, MOTORCYCLE, ...
│   └── LicenseStatus.java     enum: ACTIVE, EXPIRED, SUSPENDED, REVOKED
│
├── repository/     Storage — Spring Data JPA interfaces (no code to write!)
│   ├── PersonRepository.java          extends JpaRepository → free CRUD
│   ├── VehicleRepository.java         + findByVin / findByOwnerId
│   └── DriverLicenseRepository.java   + findByHolderId
│
├── service/        The "brain" — the DMV business RULES
│   ├── RegistryService.java   age limits, unique VIN, ownership transfer...
│   └── RegistryException.java a clear error type for broken rules
│
├── web/            The web layer (the "front door")
│   └── PortalController.java  maps browser requests → service calls → HTML
│
└── config/
    └── DataInitializer.java   seeds demo data at startup

src/main/resources/
├── application.properties     database + app settings (well commented)
├── templates/                 Thymeleaf HTML pages = the portal UI
│   ├── fragments.html         shared header / nav / message banners
│   ├── index.html  people.html  vehicles.html  licenses.html
└── static/css/style.css       styling
```

**The golden rule:** dependencies point *inward*. `web` → `service` →
`repository` interfaces → `model`. The business rules don't know or care that
the data lives in a database or that a browser is calling them — which is what
made the modernization cheap.

---

## ▶️ How to run it (IntelliJ IDEA Ultimate)

1. **Open the project:** `File ▸ Open…` → select this folder (the one with
   `pom.xml`). IntelliJ detects Maven/Spring Boot and downloads dependencies
   (first time takes a minute — watch the bottom status bar).
2. Open `VehicleDriverRegistryApplication.java`.
3. Click the green ▶ next to `main(...)` → **Run**.
4. When the console shows *"Started VehicleDriverRegistryApplication"*, open a
   browser to **http://localhost:8080**.
5. Use the portal: add people, register vehicles, transfer ownership, issue
   licenses. It ships with a little demo data already loaded.

> Prefer the command line? Run `mvn spring-boot:run` in the project folder.

### See the actual database
Open **http://localhost:8080/h2-console** while the app runs. Set **JDBC URL** to
`jdbc:h2:mem:dmv`, **User** `sa`, empty password, click **Connect**, and browse
the `PERSON`, `VEHICLE`, and `DRIVER_LICENSE` tables Hibernate built from our
Java classes.

### Run the tests
- Right-click `RegistryServiceTest` → **Run**. All should pass (green).
- Or: `mvn test`

---

## 🧪 What the portal can do today

- Register people (with validation)
- Register vehicles to an owner (unique VIN, sensible model year)
- Transfer a vehicle to a new owner
- Issue a driver license (min driving age 16, one per person)
- Suspend a license
- Live dashboard counts, all stored in a real SQL database

All core rules are covered by automated tests.

---

## 🗺️ Roadmap (where we grow next)

- [ ] **Persistence that survives restarts:** switch H2 to file-based, then to
      PostgreSQL; introduce schema migrations (Flyway/Liquibase).
- [ ] **More domain depth:** license renewal & expiry, violations/points, fees.
- [ ] **Validation & UX:** richer form validation, edit/delete, search & paging.
- [ ] **A REST API** alongside the HTML pages (for mobile apps / integrations).
- [ ] **Security & audit:** logins, roles (clerk vs. citizen), audit trails.
- [ ] **Packaging & deploy:** build a runnable jar / container image.

We tackle these one increment at a time, reviewing and revising as we go.

---

## 📚 Glossary (quick beginner reference)

| Term | Meaning |
|------|---------|
| **Spring Boot** | Framework that runs the web server and wires our components together. |
| **Maven** | Build tool; reads `pom.xml`, downloads libraries, compiles, tests. |
| **JPA / Hibernate** | Maps Java `@Entity` classes to database tables. |
| **Repository** | Interface that stores/fetches data (Spring writes the code). |
| **Service** | Class that holds the business rules. |
| **Controller** | Class that handles web requests and returns pages. |
| **Thymeleaf** | Turns HTML templates + data into the finished web page. |
| **H2** | A small SQL database that runs inside the app (nothing to install). |

---

*Built as a hands-on way to learn Java + Spring Boot by growing a realistic
government system from scratch.*
