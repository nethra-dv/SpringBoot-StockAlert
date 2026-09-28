# Postman Setup Guide — StockAlert

## Start the backend

From the folder containing `pom.xml`:

```powershell
mvn spring-boot:run
```

Wait for:

```text
Tomcat started on port 8080
Started StockAlertApplication
```

Keep that terminal running.

## Import Postman files

1. Open Postman.
2. Click **Import**.
3. Import `postman/StockAlert-Postman-Collection.json`.
4. Import `postman/StockAlert-Local.postman_environment.json`.
5. Select **StockAlert - Local** in the environment selector.

## Recommended demo order

Run:

1. Create Product - Logitech Mouse
2. Get All Products
3. Purchase +100
4. Check Stock - Expect 100
5. Sale -95 (Triggers Reorder Alert)
6. Get Open Alerts
7. Get All Alerts
8. Purchase +50 - Fulfills Open Alert
9. Get All Alerts - Verify FULFILLED
10. Create Keyboard Product
11. Keyboard Purchase +200
12. Keyboard Sale -50
13. Fast Moving - September 2026

## Business-rule tests

Also run:

- Invalid - Sell More Than Available
- Invalid - Wrong Reason/Movement Type

These demonstrate validation and service-layer business rules.

## Architecture

```text
Postman
   ↓
REST Controller
   ↓
Service Layer
   ↓
Spring Data JPA
   ↓
MySQL
```

Postman is a testing/demo tool; it is not part of the backend runtime.
