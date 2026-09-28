# StockAlert — Retail Store Inventory Reorder Alert System

A Spring Boot + Java 17 + MySQL backend based on the supplied project specification.

## Technology

- Java 17
- Spring Boot 3.5.5
- Spring Web
- Spring Data JPA / Hibernate
- Bean Validation
- MySQL
- Maven
- Postman/Swagger-compatible REST APIs

## 1. Create the database

Open MySQL and run:

```sql
CREATE DATABASE stockalert;
```

## 2. Configure the database

Open:

`src/main/resources/application.properties`

Change:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

to your actual MySQL credentials.

The application uses:

```text
jdbc:mysql://localhost:3306/stockalert
```

JPA is configured with `ddl-auto=update`, so the tables will be created automatically.

## 3. Run the project in VS Code

Make sure Java 17 and Maven are installed.

From the project root:

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
./mvnw spring-boot:run
```

Or:

```bash
mvn spring-boot:run
```

The API starts at:

`http://localhost:8080`

## 4. Main API endpoints

### Products

```text
POST   /api/products
GET    /api/products
GET    /api/products/{id}
PUT    /api/products/{id}
DELETE /api/products/{id}
GET    /api/products/{id}/stock
```

### Stock movements

```text
POST /api/products/{productId}/movements
GET  /api/products/{productId}/movements
GET  /api/products/{productId}/movements/current-stock
```

### Reorder alerts

```text
GET /api/reorder-alerts
GET /api/reorder-alerts/open
PUT /api/reorder-alerts/{id}/fulfill
```

### Fast-moving products

```text
GET /api/products/fast-moving?from=2026-09-01&to=2026-09-28
```

## 5. Test data

### Create a product

```http
POST http://localhost:8080/api/products
Content-Type: application/json
```

```json
{
  "name": "Logitech Mouse",
  "sku": "MOUSE-001",
  "reorderThreshold": 10,
  "reorderQuantity": 50
}
```

### Add 100 units

```http
POST http://localhost:8080/api/products/1/movements
Content-Type: application/json
```

```json
{
  "movementType": "IN",
  "reason": "PURCHASE",
  "quantity": 100
}
```

### Sell 95 units

```http
POST http://localhost:8080/api/products/1/movements
Content-Type: application/json
```

```json
{
  "movementType": "OUT",
  "reason": "SALE",
  "quantity": 95
}
```

Current stock becomes 5.

Since:

```text
5 <= reorder threshold 10
```

an OPEN reorder alert is automatically created.

Check:

```http
GET http://localhost:8080/api/reorder-alerts/open
```

## 6. Business rules implemented

1. Current stock is derived from all stock movements.
2. IN movements increase stock.
3. OUT movements decrease stock.
4. SALE and DAMAGE must be OUT movements.
5. PURCHASE and RETURN must be IN movements.
6. Stock cannot be reduced below zero.
7. A reorder alert is created when stock is at or below the threshold.
8. A second OPEN alert is not created for the same product.
9. A new IN movement fulfils the currently open alert.
10. Fast-moving products are calculated from SALE quantities over a requested date range.
11. Invalid requests return clear HTTP 400 errors.
12. Missing products/alerts return HTTP 404 errors.
13. Duplicate SKUs are rejected.

## 7. Project structure

```text
src/main/java/com/example/stockalert
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

The service layer contains the core business rules, as required by the specification.

## 8. Suggested demo sequence

For an external review/demo:

1. Create product.
2. Add 100 units.
3. Check current stock.
4. Sell 95 units.
5. Check current stock again.
6. Show automatically generated reorder alert.
7. Try selling more than available stock and show the validation/business error.
8. Add new stock.
9. Show the alert becoming FULFILLED.
10. Add several SALE movements.
11. Demonstrate the fast-moving products endpoint.

## Important

Do not commit your real MySQL password to GitHub. Keep the password in `application.properties` only for local testing, or move it to environment variables for a production deployment.
