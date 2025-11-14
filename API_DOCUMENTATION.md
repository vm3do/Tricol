# Inventory Management API Documentation

## Base URL
```
http://localhost:8080
```

## Products API

### GET /api/v1/products
Get all products
- **Response**: Array of ProductResponseDTO

### GET /api/v1/products/{id}
Get product by ID
- **Response**: ProductResponseDTO

### POST /api/v1/products
Create new product
- **Request Body**:
```json
{
  "reference": "PROD-001",
  "name": "Product Name",
  "description": "Product description",
  "unitPrice": 25.50,
  "category": "Electronics",
  "reorderPoint": 10,
  "unit": "pieces"
}
```

### PUT /api/v1/products/{id}
Update product
- **Request Body**:
```json
{
  "reference": "PROD-001-UPDATED",
  "name": "Updated Product Name",
  "description": "Updated description",
  "unitPrice": 30.00,
  "category": "Electronics",
  "reorderPoint": 15,
  "unit": "pieces"
}
```

### DELETE /api/v1/products/{id}
Delete product

### GET /api/v1/products/{id}/stock
Get product stock information
- **Response**: StockResponseDTO

---

## Suppliers API

### GET /api/v1/suppliers
Get all suppliers
- **Response**: Array of SupplierResponseDTO

### GET /api/v1/suppliers/{id}
Get supplier by ID
- **Response**: SupplierResponseDTO

### POST /api/v1/suppliers
Create new supplier
- **Request Body**:
```json
{
  "name": "Supplier Name",
  "ice": "123456789",
  "email": "supplier@example.com",
  "phone": "+1234567890",
  "address": "123 Supplier Street, City, Country"
}
```

### PUT /api/v1/suppliers/{id}
Update supplier
- **Request Body**:
```json
{
  "name": "Updated Supplier Name",
  "ice": "123456789",
  "email": "updated@example.com",
  "phone": "+1234567890",
  "address": "456 Updated Street, City, Country"
}
```

### DELETE /api/v1/suppliers/{id}
Delete supplier

---

## Supplier Orders API

### GET /api/v1/orders
Get all orders
- **Response**: Array of SupplierOrderResponseDTO

### GET /api/v1/orders/{id}
Get order by ID
- **Response**: SupplierOrderResponseDTO

### POST /api/v1/orders
Create new order
- **Request Body**:
```json
{
  "supplierId": 1,
  "orderDate": "2024-01-15",
  "items": [
    {
      "productId": 1,
      "quantity": 100,
      "unitPrice": 25.50
    },
    {
      "productId": 2,
      "quantity": 50,
      "unitPrice": 15.75
    }
  ]
}
```

### PUT /api/v1/orders/{id}
Update order
- **Request Body**:
```json
{
  "orderDate": "2024-01-16",
  "status": "PENDING",
  "items": [
    {
      "productId": 1,
      "quantity": 120,
      "unitPrice": 25.50
    }
  ]
}
```

### PUT /api/v1/orders/{id}/validate
Validate order (no request body)

### PUT /api/v1/orders/{id}/receive
Receive order and process stock entry (no request body)

### DELETE /api/v1/orders/{id}
Delete order

### GET /api/v1/orders/supplier/{supplierId}
Get orders by supplier
- **Response**: Array of SupplierOrderResponseDTO

---

## Stock Management API

### GET /api/v1/stock
Get stock summary dashboard
- **Response**: StockSummaryResponseDTO

### GET /api/v1/stock/product/{productId}
Get stock for specific product
- **Response**: StockResponseDTO

### GET /api/v1/stock/movements
Get all stock movements
- **Response**: Array of StockMovementResponseDTO

### GET /api/v1/stock/movements/product/{productId}
Get stock movements for specific product
- **Response**: Array of StockMovementResponseDTO

### GET /api/v1/stock/alerts
Get low stock alerts
- **Response**: Array of ProductResponseDTO

### GET /api/v1/stock/valuation
Get total inventory valuation
- **Response**: 
```json
{
  "totalValue": 125000.50
}
```

---

## Stock Outbound API

### GET /api/v1/stock-outbound
Get all stock outbounds
- **Response**: Array of StockOutboundResponseDTO

### GET /api/v1/stock-outbound/{id}
Get stock outbound by ID
- **Response**: StockOutboundResponseDTO

### POST /api/v1/stock-outbound
Create new stock outbound
- **Request Body**:
```json
{
  "reason": "PRODUCTION",
  "workshop": "Workshop A",
  "notes": "Materials for production order #123",
  "items": [
    {
      "productId": 1,
      "quantity": 50,
      "notes": "For assembly line 1"
    },
    {
      "productId": 2,
      "quantity": 25,
      "notes": "For assembly line 2"
    }
  ]
}
```

### PUT /api/v1/stock-outbound/{id}
Update stock outbound (only DRAFT status)
- **Request Body**:
```json
{
  "reason": "MAINTENANCE",
  "workshop": "Workshop B",
  "notes": "Updated notes for maintenance"
}
```

### PUT /api/v1/stock-outbound/{id}/validate
Validate and process stock outbound (no request body)

### PUT /api/v1/stock-outbound/{id}/cancel
Cancel stock outbound (no request body)

### GET /api/v1/stock-outbound/workshop/{workshop}
Get stock outbounds by workshop
- **Response**: Array of StockOutboundResponseDTO

---

## Response DTOs

### ProductResponseDTO
```json
{
  "id": 1,
  "reference": "PROD-001",
  "name": "Product Name",
  "description": "Product description",
  "unitPrice": 25.50,
  "category": "Electronics",
  "reorderPoint": 10,
  "unit": "pieces",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### StockResponseDTO
```json
{
  "productId": 1,
  "productReference": "PROD-001",
  "productName": "Product Name",
  "currentStock": 150,
  "stockValue": 3825.00,
  "reorderPoint": 10,
  "isLowStock": false
}
```

### StockSummaryResponseDTO
```json
{
  "stocks": [
    {
      "productId": 1,
      "productReference": "PROD-001",
      "productName": "Product Name",
      "currentStock": 150,
      "stockValue": 3825.00,
      "reorderPoint": 10,
      "isLowStock": false
    }
  ],
  "totalValue": 125000.50,
  "alerts": [],
  "totalProducts": 25,
  "lowStockCount": 0
}
```

### StockOutboundResponseDTO
```json
{
  "id": 1,
  "reference": "OUT-20240115-001",
  "reason": "PRODUCTION",
  "status": "DRAFT",
  "workshop": "Workshop A",
  "notes": "Materials for production",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productReference": "PROD-001",
      "productName": "Product Name",
      "quantity": 50,
      "notes": "For assembly line 1"
    }
  ],
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

---

## Enums

### OutboundReason
- `PRODUCTION`
- `MAINTENANCE` 
- `OTHER`

### OutboundStatus
- `DRAFT`
- `VALIDATED`
- `CANCELLED`

### OrderStatus
- `PENDING`
- `VALIDATED`
- `DELIVERED`

### MovementType
- `ENTREE` (Stock In)
- `SORTIE` (Stock Out)

---

## Error Responses

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 999",
  "path": "/api/v1/products/999"
}
```

### 400 Bad Request
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient stock for product: PROD-001",
  "path": "/api/v1/stock-outbound/1/validate"
}
```