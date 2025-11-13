# Testing Guide - Stock Entry Implementation

## Prerequisites
- MySQL database running
- Application started successfully
- Database tables created via Liquibase migrations
- At least one Supplier and one Product in the database

## Complete Workflow Test

### Step 1: Create a Supplier (if not exists)
```http
POST http://localhost:8080/api/suppliers
Content-Type: application/json

{
  "companyName": "Textile Maroc SARL",
  "ice": "123456789012345",
  "address": "123 Rue Mohammed V",
  "city": "Casablanca",
  "contactPerson": "Ahmed Bennani",
  "email": "contact@textilemaroc.ma",
  "phone": "+212 5 22 12 34 56"
}
```

### Step 2: Create a Product (if not exists)
```http
POST http://localhost:8080/api/products
Content-Type: application/json

{
  "reference": "TIS-001",
  "name": "Tissu Coton Premium",
  "description": "Tissu 100% coton pour chemises professionnelles",
  "unitPrice": 45.50,
  "category": "Tissus",
  "reorderPoint": 100,
  "unit": "mètre"
}
```

### Step 3: Create a Supplier Order
```http
POST http://localhost:8080/api/supplier-orders
Content-Type: application/json

{
  "supplierId": 1,
  "orderDate": "2025-11-13",
  "items": [
    {
      "productId": 1,
      "quantity": 500,
      "unitPrice": 45.50
    },
    {
      "productId": 2,
      "quantity": 300,
      "unitPrice": 35.00
    }
  ]
}
```

**Expected Response:**
```json
{
  "id": 1,
  "supplier": {...},
  "orderDate": "2025-11-13",
  "totalAmount": 33250.00,
  "status": "PENDING",
  "items": [...]
}
```

### Step 4: Validate the Order
```http
PUT http://localhost:8080/api/supplier-orders/1/valider
```

**Expected Response:**
```json
{
  "id": 1,
  "status": "VALIDATED",
  ...
}
```

### Step 5: Receive the Order (Triggers Stock Entry)
```http
PUT http://localhost:8080/api/supplier-orders/1/reception
```

**Expected Response:**
```json
{
  "id": 1,
  "status": "DELIVERED",
  ...
}
```

**What happens internally:**
1. Order status changes to `DELIVERED`
2. `stockService.processStockEntry(order)` is called
3. For each order item:
   - Stock lot created with unique number (e.g., `LOT-TIS-001-20251113-1`)
   - Stock movement recorded with type `ENTREE`
4. Complete traceability established

### Step 6: Verify Stock Entry in Database

#### Check Stock Lots Created
```sql
SELECT 
    sl.id,
    sl.lot_number,
    p.reference AS product_ref,
    p.name AS product_name,
    sl.initial_quantity,
    sl.remaining_quantity,
    sl.unit_price,
    sl.entry_date,
    sl.created_at
FROM stock_lot sl
JOIN product p ON sl.product_id = p.id
WHERE sl.supplier_order_id = 1
ORDER BY sl.entry_date ASC;
```

**Expected Result:**
```
+----+-------------------------+--------------+------------------------+------------------+--------------------+------------+------------+---------------------+
| id | lot_number              | product_ref  | product_name           | initial_quantity | remaining_quantity | unit_price | entry_date | created_at          |
+----+-------------------------+--------------+------------------------+------------------+--------------------+------------+------------+---------------------+
|  1 | LOT-TIS-001-20251113-1  | TIS-001      | Tissu Coton Premium    |              500 |                500 |      45.50 | 2025-11-13 | 2025-11-13 04:30:15 |
|  2 | LOT-BTN-002-20251113-1  | BTN-002      | Boutons Métal          |              300 |                300 |      35.00 | 2025-11-13 | 2025-11-13 04:30:15 |
+----+-------------------------+--------------+------------------------+------------------+--------------------+------------+------------+---------------------+
```

#### Check Stock Movements Created
```sql
SELECT 
    sm.id,
    sm.movement_type,
    p.reference AS product_ref,
    sm.quantity,
    sm.unit_price,
    sl.lot_number,
    sm.reference,
    sm.movement_date
FROM stock_movement sm
JOIN product p ON sm.product_id = p.id
LEFT JOIN stock_lot sl ON sm.stock_lot_id = sl.id
WHERE sm.supplier_order_id = 1
ORDER BY sm.movement_date DESC;
```

**Expected Result:**
```
+----+---------------+--------------+----------+------------+-------------------------+--------------------------+---------------------+
| id | movement_type | product_ref  | quantity | unit_price | lot_number              | reference                | movement_date       |
+----+---------------+--------------+----------+------------+-------------------------+--------------------------+---------------------+
|  1 | ENTREE        | TIS-001      |      500 |      45.50 | LOT-TIS-001-20251113-1  | Réception commande #1    | 2025-11-13 04:30:15 |
|  2 | ENTREE        | BTN-002      |      300 |      35.00 | LOT-BTN-002-20251113-1  | Réception commande #1    | 2025-11-13 04:30:15 |
+----+---------------+--------------+----------+------------+-------------------------+--------------------------+---------------------+
```

#### Check Total Available Stock for a Product
```sql
SELECT 
    p.reference,
    p.name,
    SUM(sl.remaining_quantity) AS total_available_stock,
    COUNT(sl.id) AS number_of_lots
FROM product p
LEFT JOIN stock_lot sl ON p.id = sl.product_id AND sl.remaining_quantity > 0
WHERE p.id = 1
GROUP BY p.id, p.reference, p.name;
```

**Expected Result:**
```
+-------------+---------------------+------------------------+----------------+
| reference   | name                | total_available_stock  | number_of_lots |
+-------------+---------------------+------------------------+----------------+
| TIS-001     | Tissu Coton Premium |                    500 |              1 |
+-------------+---------------------+------------------------+----------------+
```

## Error Scenarios to Test

### Test 1: Try to receive a PENDING order (not validated)
```http
PUT http://localhost:8080/api/supplier-orders/{id}/reception
```
**Expected:** `400 Bad Request` - "Order is not validated"

### Test 2: Try to receive an already DELIVERED order
```http
PUT http://localhost:8080/api/supplier-orders/{id}/reception
```
**Expected:** `400 Bad Request` - "Order already received"

### Test 3: Try to validate an already VALIDATED order
```http
PUT http://localhost:8080/api/supplier-orders/{id}/valider
```
**Expected:** `400 Bad Request` - "Only pending orders can be validated"

## Logs to Check

When you receive an order, check the application logs for:

```
INFO  c.t.i.service.StockService - Processing stock entry for order ID: 1
INFO  c.t.i.service.StockService - Created stock lot: LOT-TIS-001-20251113-1 for product: TIS-001 with quantity: 500
INFO  c.t.i.service.StockService - Created stock movement for lot: LOT-TIS-001-20251113-1
INFO  c.t.i.service.StockService - Created stock lot: LOT-BTN-002-20251113-1 for product: BTN-002 with quantity: 300
INFO  c.t.i.service.StockService - Created stock movement for lot: LOT-BTN-002-20251113-1
INFO  c.t.i.service.StockService - Stock entry processed successfully for order ID: 1
```

## API Endpoints Summary

| Method | Endpoint | Description | Status Required |
|--------|----------|-------------|-----------------|
| POST | `/api/supplier-orders` | Create new order | N/A → PENDING |
| GET | `/api/supplier-orders` | List all orders | Any |
| GET | `/api/supplier-orders/{id}` | Get order details | Any |
| PUT | `/api/supplier-orders/{id}` | Update order | PENDING recommended |
| PUT | `/api/supplier-orders/{id}/valider` | Validate order | PENDING → VALIDATED |
| PUT | `/api/supplier-orders/{id}/reception` | Receive order (creates stock) | VALIDATED → DELIVERED |
| DELETE | `/api/supplier-orders/{id}` | Delete order | Any (use with caution) |

## FIFO Verification

To verify FIFO is working correctly, create multiple orders for the same product:

```sql
-- Check lots are ordered by entry_date (FIFO)
SELECT 
    lot_number,
    entry_date,
    remaining_quantity
FROM stock_lot
WHERE product_id = 1 AND remaining_quantity > 0
ORDER BY entry_date ASC, id ASC;
```

**Expected:** Oldest lots appear first (will be consumed first in future stock exits)

## Next Steps

After verifying stock entry works:
1. Implement stock exit (SORTIE) functionality
2. Implement Bon de Sortie feature
3. Create stock API endpoints (GET /api/v1/stock, etc.)
4. Implement stock alerts based on reorder_point
5. Implement FIFO stock valuation

