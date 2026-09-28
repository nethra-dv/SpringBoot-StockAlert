# Quick Postman Test Flow

Base URL:

`http://localhost:8080`

## 1. Create Product

POST `/api/products`

```json
{
  "name": "Logitech Mouse",
  "sku": "MOUSE-001",
  "reorderThreshold": 10,
  "reorderQuantity": 50
}
```

## 2. Purchase 100

POST `/api/products/1/movements`

```json
{
  "movementType": "IN",
  "reason": "PURCHASE",
  "quantity": 100
}
```

## 3. Sell 95

POST `/api/products/1/movements`

```json
{
  "movementType": "OUT",
  "reason": "SALE",
  "quantity": 95
}
```

## 4. Check Stock

GET `/api/products/1/stock`

Expected current stock:

```json
{
  "productId": 1,
  "currentStock": 5
}
```

## 5. Check Open Alerts

GET `/api/reorder-alerts/open`

An OPEN alert should exist because `5 <= 10`.

## 6. Add New Stock

POST `/api/products/1/movements`

```json
{
  "movementType": "IN",
  "reason": "PURCHASE",
  "quantity": 50
}
```

The previous OPEN alert becomes FULFILLED.

## 7. Test insufficient stock

Try:

```json
{
  "movementType": "OUT",
  "reason": "SALE",
  "quantity": 1000
}
```

The API should return HTTP 400 with a clear error.
