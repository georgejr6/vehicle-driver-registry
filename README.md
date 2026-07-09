# Vehicle & Driver Registry 🚗🪪

A learning-focused, **government-style DMV (Department of Motor Vehicles) portal**,
built in **Java + Maven**. It starts as a small, clean, plain-Java application and
is designed to grow — step by step — into the kind of large web system a
government would build when modernizing a legacy registry.

This project is written to **teach**: nearly every line is commented to explain
what it does and *why*.

---

## 🎯 The big picture

Real government modernization projects replace old "legacy" systems with modern,
layered, web-based ones. We're recreating that journey on purpose:

1. **Now (v0.1):** a plain-Java, layered console app — no frameworks, no "magic".
   You learn the Java fundamentals and the architecture first.
2. **Later:** add a real database, then a web layer, then modernize the whole
   thing onto **Spring Boot** (the framework used in real enterprise/government
   Java systems).

Because the app is built in **layers with interfaces between them**, each of
those upgrades swaps out *one* layer without rewriting the others.

---

## 🧱 Architecture (how the code is organized)

```
src/main/java/gov/dmv/registry/
├── model/          The "nouns" — the data the system is about
│   ├── Person.java            a citizen / owner / license holder
│   ├── Vehicle.java           a registered vehicle
│   ├── DriverLicense.java     a license issued to a person
│   ├── VehicleType.java       enum: CAR, TRUCK, MOTORCYCLE, ...
│   └── LicenseStatus.java     enum: ACTIVE, EXPIRED, SUSPENDED, REVOKED
│
├── repository/     Storage — behind interfaces so it can be swapped later
│   ├── PersonRepository.java             the CONTRACT (what storage must do)
│   ├── InMemoryPersonRepository.java     one IMPLEMENTATION (in memory for now)
│   └── ... (same pattern for Vehicle and DriverLicense)
│
├── service/        The "brain" — the DMV business RULES
│   ├── RegistryService.java   age limits, VIN uniqueness, ownership transfer...
│   └── RegistryException.java a clear error type for broken rules
│
└── App.java        The entry point + console menu (your future web UI)
```

**The golden rule:** dependencies point *inward*. `App` talks to `service`,
`service` talks to `repository` **interfaces**. The business rules never know or
care whether data lives in memory or in a database — that's what lets us
modernize safely.

```
App  ──▶  RegistryService  ──▶  <<Repository interfaces>>
(UI)        (rules)                     ▲
                                        └── InMemory... (today)
                                        └── Database... (a future step)
```

---

## ▶️ How to run it (in IntelliJ IDEA)

1. **Open the project:** `File ▸ Open…` and select this project's folder
   (the one containing `pom.xml`). IntelliJ detects Maven and sets everything up.
2. Wait for the bottom status bar to finish "indexing / resolving dependencies".
3. Open `src/main/java/gov/dmv/registry/App.java`.
4. Click the green ▶ arrow next to `public static void main(...)` and choose
   **Run 'App.main()'**.
5. Use the menu in the **Run** panel at the bottom (type a number, press Enter).

> The app starts with a little demo data already loaded so the lists aren't empty.

### Run the tests
- Right-click `src/test/java/gov/dmv/registry/service/RegistryServiceTest.java`
  → **Run 'RegistryServiceTest'**. All tests should pass (green).
- Or from a terminal with Maven installed: `mvn test`

---

## 🧪 What v0.1 can already do

- Register people (with validation)
- Register vehicles to an owner (unique VIN, sensible model year)
- List people and vehicles
- Transfer a vehicle to a new owner
- Issue a driver license (enforces a minimum driving age, one per person)
- List licenses; suspend a license (via the service)

All of the above is covered by automated tests.

---

## 🗺️ Roadmap (where we grow next)

- [ ] **Persistence:** save data to a file, then to a real database (JDBC → JPA).
- [ ] **More domain rules:** license renewal/expiry, violations & points, fees.
- [ ] **Web layer:** expose the registry over HTTP (REST endpoints), then a UI.
- [ ] **Modernize to Spring Boot:** dependency injection, web, and DB the
      enterprise way — the "legacy → modern" leap.
- [ ] **Security & audit:** logins, roles (clerk vs. citizen), audit trails.

We tackle these one increment at a time, reviewing and revising as we go.

---

## 📚 Glossary (quick beginner reference)

| Term | Meaning |
|------|---------|
| **Maven** | Tool that builds the project and downloads libraries (reads `pom.xml`). |
| **Class** | A blueprint for objects (e.g. `Person`). |
| **Interface** | A contract listing methods, with no implementation. |
| **Enum** | A type with a fixed set of named values. |
| **Repository** | The layer that stores/fetches data. |
| **Service** | The layer that holds business rules. |
| **JUnit** | The library we use to write automated tests. |

---

*Built as a hands-on way to learn Java by growing a realistic system from scratch.*
