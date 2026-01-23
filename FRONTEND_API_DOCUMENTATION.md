# TRICOL Backend API - Complete Frontend Integration Guide

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Authentication & Security](#authentication--security)
3. [API Endpoints](#api-endpoints)
4. [Data Models & DTOs](#data-models--dtos)
5. [Enumerations](#enumerations)
6. [Permissions Matrix](#permissions-matrix)
7. [Error Handling](#error-handling)
8. [Best Practices](#best-practices)

---

## 🎯 Project Overview

**TRICOL** is a Spring Boot backend API for managing supply chain and inventory operations for a professional clothing manufacturer. The system implements:

- **FIFO (First-In-First-Out)** stock management with automatic lot tracking
- **Dual JWT authentication** (Custom JWT + OAuth2/Keycloak)
- **Role-based access control (RBAC)** with 4 roles and granular permissions
- **Full audit logging** of critical operations
- **RESTful API** with comprehensive CRUD operations

### Tech Stack (Backend)
- **Framework**: Spring Boot 3.x
- **Security**: Spring Security with JWT + OAuth2
- **Database**: MySQL with Liquibase migrations
- **ORM**: Hibernate/JPA
- **Architecture**: Layered (Controller → Service → Repository)

### Base URL
```
http://localhost:8080/api
```

---

## 🔐 Authentication & Security

### Authentication Methods

The backend supports **dual authentication**:

1. **Custom JWT** (for standalone frontend)
2. **OAuth2/Keycloak** (for SSO integration - optional)

### Login Flow

**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

### Token Management

- **Access Token Expiration**: 30 minutes (1800000 ms)
- **Refresh Token Expiration**: 24 hours (86400000 ms)
- **Token Type**: Bearer
- **Header Format**: `Authorization: Bearer {accessToken}`

### Registration Flow

**Endpoint**: `POST /api/auth/register`

**Request Body**:
```json
{
  "email": "newuser@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe"
}
```

**Response** (200 OK):
```json
"User registered successfully"
```

**Note**: Newly registered users have **no role** by default. An admin must assign a role via `/api/admin/users/assign-role`.

---

## 📡 API Endpoints

### 1. Authentication Endpoints

| Method | Endpoint | Permission | Description |
|--------|----------|------------|-------------|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login and get JWT tokens |

---

### 2. Product Endpoints

**Base Path**: `/api/v1/products`

| Method | Endpoint | Permission | Description |
|--------|----------|------------|-------------|
| GET | `/api/v1/products` | `VIEW_PRODUCT` | Get all products |
| GET | `/api/v1/products/{id}` | `VIEW_PRODUCT` | Get product by ID |
| POST | `/api/v1/products` | `CREATE_PRODUCT` | Create new product |
| PUT | `/api/v1/products/{id}` | `UPDATE_PRODUCT` | Update product |
| DELETE | `/api/v1/products/{id}` | `DELETE_PRODUCT` | Delete product |
| GET | `/api/v1/products/{id}/stock` | `VIEW_STOCK` | Get product stock details |

#### Example: Create Product

**Request**: `POST /api/v1/products`
```json
{
  "reference": "REF-001",
  "name": "Chemise Professionnelle",
  "description": "Chemise blanche pour personnel",
  "unitPrice": 150.00,
  "category": "VETEMENTS",
  "reorderPoint": 50,
  "unit": "PIECE"
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "reference": "REF-001",
  "name": "Chemise Professionnelle",
  "description": "Chemise blanche pour personnel",
  "unitPrice": 150.00,
  "category": "VETEMENTS",
  "reorderPoint": 50,
  "unit": "PIECE",
  "createdAt": "2026-02-02T10:30:00",
  "updatedAt": "2026-02-02T10:30:00"
}
```

---

### 3. Supplier Endpoints

**Base Path**: `/api/v1/suppliers`

| Method | Endpoint | Permission | Description |
|--------|----------|------------|-------------|
| GET | `/api/v1/suppliers` | `VIEW_SUPPLIER` | Get all suppliers |
| GET | `/api/v1/suppliers/{id}` | `VIEW_SUPPLIER` | Get supplier by ID |
| POST | `/api/v1/suppliers` | `CREATE_SUPPLIER` | Create new supplier |
| PUT | `/api/v1/suppliers/{id}` | `UPDATE_SUPPLIER` | Update supplier |
| DELETE | `/api/v1/suppliers/{id}` | `DELETE_SUPPLIER` | Delete supplier |

#### Example: Create Supplier

**Request**: `POST /api/v1/suppliers`
```json
{
  "companyName": "Textile Maroc SARL",
  "address": "Zone Industrielle Ain Sebaa",
  "city": "Casablanca",
  "contactPerson": "Ahmed Benali",
  "email": "contact@textile-maroc.ma",
  "phone": "+212 522 345 678",
  "ice": "002345678000012"
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "companyName": "Textile Maroc SARL",
  "address": "Zone Industrielle Ain Sebaa",
  "city": "Casablanca",
  "contactPerson": "Ahmed Benali",
  "email": "contact@textile-maroc.ma",
  "phone": "+212 522 345 678",
  "ice": "002345678000012",
  "createdAt": "2026-02-02T10:30:00",
  "updatedAt": "2026-02-02T10:30:00"
}
```

---

### 4. Supplier Order Endpoints (Commandes Fournisseurs)

**Base Path**: `/api/v1/orders`

| Method | Endpoint | Permission | Description |
|--------|----------|------------|-------------|
| GET | `/api/v1/orders` | `VIEW_ORDER` | Get all orders |
| GET | `/api/v1/orders/{id}` | `VIEW_ORDER` | Get order by ID |
| POST | `/api/v1/orders` | `CREATE_ORDER` | Create new order |
| PUT | `/api/v1/orders/{id}` | `UPDATE_ORDER` | Update order (only if PENDING) |
| PUT | `/api/v1/orders/{id}/validate` | `VALIDATE_ORDER` | Validate order (PENDING → VALIDATED) |
| PUT | `/api/v1/orders/{id}/receive` | `RECEIVE_ORDER` | Receive order (VALIDATED → DELIVERED + create stock lots) |
| DELETE | `/api/v1/orders/{id}` | `CANCEL_ORDER` | Cancel order |
| GET | `/api/v1/orders/supplier/{supplierId}` | `VIEW_ORDER` | Get orders by supplier |

#### Example: Create Order

**Request**: `POST /api/v1/orders`
```json
{
  "supplierId": 1,
  "orderDate": "2026-02-02",
  "items": [
    {
      "productId": 1,
      "quantity": 100,
      "unitPrice": 145.00
    },
    {
      "productId": 2,
      "quantity": 50,
      "unitPrice": 220.00
    }
  ]
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "supplier": {
    "id": 1,
    "companyName": "Textile Maroc SARL",
    "address": "Zone Industrielle Ain Sebaa",
    "city": "Casablanca",
    "contactPerson": "Ahmed Benali",
    "email": "contact@textile-maroc.ma",
    "phone": "+212 522 345 678",
    "ice": "002345678000012",
    "createdAt": "2026-02-02T10:30:00",
    "updatedAt": "2026-02-02T10:30:00"
  },
  "orderDate": "2026-02-02",
  "totalAmount": 25500.00,
  "status": "PENDING",
  "items": [
    {
      "id": 1,
      "product": { /* ProductResponseDTO */ },
      "quantity": 100,
      "unitPrice": 145.00,
      "totalAmount": 14500.00
    },
    {
      "id": 2,
      "product": { /* ProductResponseDTO */ },
      "quantity": 50,
      "unitPrice": 220.00,
      "totalAmount": 11000.00
    }
  ]
}
```

#### Order Status Flow

```
PENDING → (validate) → VALIDATED → (receive) → DELIVERED
   ↓
CANCELLED (can cancel at any status)
```

---

### 5. Stock Endpoints

**Base Path**: `/api/v1/stock`

| Method | Endpoint | Permission | Description |
|--------|----------|------------|-------------|
| GET | `/api/v1/stock` | `VIEW_STOCK` | Get stock summary (all products) |
| GET | `/api/v1/stock/product/{productId}` | `VIEW_STOCK` | Get stock for specific product |
| GET | `/api/v1/stock/movements` | `VIEW_STOCK_HISTORY` | Get all stock movements |
| GET | `/api/v1/stock/movements/product/{productId}` | `VIEW_STOCK_HISTORY` | Get movements for product |
| GET | `/api/v1/stock/movements/search` | `VIEW_STOCK_HISTORY` | Search movements (advanced filters) |
| GET | `/api/v1/stock/alerts` | `VIEW_STOCK` | Get products below reorder point |
| GET | `/api/v1/stock/valuation` | `VIEW_STOCK_VALUATION` | Get total stock value (FIFO) |

#### Example: Get Stock Summary

**Request**: `GET /api/v1/stock`

**Response** (200 OK):
```json
{
  "stocks": [
    {
      "productId": 1,
      "productReference": "REF-001",
      "productName": "Chemise Professionnelle",
      "currentStock": 150,
      "stockValue": 21750.00,
      "isLowStock": false
    },
    {
      "productId": 2,
      "productReference": "REF-002",
      "productName": "Pantalon de Travail",
      "currentStock": 30,
      "stockValue": 6600.00,
      "isLowStock": true
    }
  ],
  "totalValue": 28350.00,
  "alerts": [
    {
      "id": 2,
      "reference": "REF-002",
      "name": "Pantalon de Travail",
      "reorderPoint": 50
    }
  ],
  "totalProducts": 2,
  "alertCount": 1
}
```

#### Example: Search Movements (Advanced Filters)

**Request**: `GET /api/v1/stock/movements/search`

**Query Parameters**:
- `productId` (optional): Filter by product ID
- `reference` (optional): Filter by product reference
- `type` (optional): Filter by movement type (`ENTREE` or `SORTIE`)
- `lotNumber` (optional): Filter by lot number
- `startDate` (optional): Filter movements after this date (ISO 8601 format)
- `endDate` (optional): Filter movements before this date (ISO 8601 format)

**Example**:
```
GET /api/v1/stock/movements/search?productId=1&type=ENTREE&startDate=2026-01-01T00:00:00&endDate=2026-02-02T23:59:59
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "product": {
      "id": 1,
      "reference": "REF-001",
      "name": "Chemise Professionnelle"
    },
    "type": "ENTREE",
    "quantity": 100,
    "lotNumber": "LOT-2026-02-001",
    "movementDate": "2026-02-02T14:30:00",
    "reference": "SUPP_ORDER_1",
    "notes": "Réception commande fournisseur #1"
  }
]
```

---

### 6. Stock Outbound Endpoints (Bons de Sortie)

**Base Path**: `/api/stock-outbound`

| Method | Endpoint | Permission | Description |
|--------|----------|------------|-------------|
| GET | `/api/stock-outbound` | `VIEW_BON_SORTIE` | Get all outbound orders |
| GET | `/api/stock-outbound/{id}` | `VIEW_BON_SORTIE` | Get outbound by ID |
| POST | `/api/stock-outbound` | `CREATE_BON_SORTIE` | Create new outbound (status: DRAFT) |
| PUT | `/api/stock-outbound/{id}` | `CREATE_BON_SORTIE` | Update outbound (only if DRAFT) |
| PUT | `/api/stock-outbound/{id}/validate` | `VALIDATE_BON_SORTIE` | Validate outbound (DRAFT → VALIDATED + FIFO stock exit) |
| PUT | `/api/stock-outbound/{id}/cancel` | `CANCEL_BON_SORTIE` | Cancel outbound (only if DRAFT) |
| GET | `/api/stock-outbound/workshop/{workshop}` | `VIEW_BON_SORTIE` | Get outbounds by workshop |

#### Example: Create Outbound Order

**Request**: `POST /api/stock-outbound`
```json
{
  "reason": "PRODUCTION",
  "workshop": "Atelier Couture A",
  "notes": "Production commande client #2345",
  "items": [
    {
      "productId": 1,
      "quantity": 50,
      "notes": "Chemises pour lot urgent"
    },
    {
      "productId": 3,
      "quantity": 30,
      "notes": null
    }
  ]
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "reference": "OUT-2026-02-001",
  "reason": "PRODUCTION",
  "status": "DRAFT",
  "workshop": "Atelier Couture A",
  "notes": "Production commande client #2345",
  "items": [
    {
      "id": 1,
      "product": {
        "id": 1,
        "reference": "REF-001",
        "name": "Chemise Professionnelle"
      },
      "quantity": 50,
      "notes": "Chemises pour lot urgent"
    },
    {
      "id": 2,
      "product": {
        "id": 3,
        "reference": "REF-003",
        "name": "Boutons"
      },
      "quantity": 30,
      "notes": null
    }
  ],
  "createdAt": "2026-02-02T15:20:00",
  "updatedAt": "2026-02-02T15:20:00"
}
```

#### Outbound Status Flow

```
DRAFT → (validate) → VALIDATED (triggers FIFO stock exit)
  ↓
CANCELLED (only from DRAFT)
```

**Important**: When an outbound is validated, the system automatically:
1. Creates `SORTIE` movements for each item using FIFO logic
2. Deducts quantities from the oldest lots first
3. Links movements to the outbound reference
4. Updates stock levels

---

### 7. Admin Endpoints

**Base Path**: `/api/admin`

**Note**: All admin endpoints require `MANAGE_USERS` permission (only ADMIN role by default).

| Method | Endpoint | Permission | Description |
|--------|----------|------------|-------------|
| GET | `/api/admin/users` | `MANAGE_USERS` | Get all users |
| POST | `/api/admin/users/assign-role` | `MANAGE_USERS` | Assign role to user |
| POST | `/api/admin/users/permission-override` | `MANAGE_USERS` | Grant/revoke individual permission |
| GET | `/api/admin/roles` | `MANAGE_USERS` | Get all roles with permissions |
| GET | `/api/admin/permissions` | `MANAGE_USERS` | Get all permissions |
| GET | `/api/admin/audit-logs` | `VIEW_AUDIT_LOGS` | Get audit logs |

#### Example: Assign Role to User

**Request**: `POST /api/admin/users/assign-role`
```json
{
  "userId": 5,
  "roleName": "MAGASINIER"
}
```

**Response** (200 OK):
```json
"Role assigned successfully"
```

#### Example: Permission Override (Grant)

**Request**: `POST /api/admin/users/permission-override`
```json
{
  "userId": 5,
  "permissionName": "DELETE_PRODUCT",
  "granted": true
}
```

**Response** (200 OK):
```json
"Permission override applied successfully"
```

**Note**: This allows granting or revoking specific permissions to individual users, overriding their role's default permissions.

#### Example: Get All Users

**Request**: `GET /api/admin/users`

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "email": "admin@tricol.ma",
    "fullName": "Administrator",
    "enabled": true,
    "locked": false,
    "roleName": "ADMIN",
    "createdAt": "2026-01-15T08:00:00"
  },
  {
    "id": 2,
    "email": "achats@tricol.ma",
    "fullName": "Mohammed Alami",
    "enabled": true,
    "locked": false,
    "roleName": "RESPONSABLE_ACHATS",
    "createdAt": "2026-01-20T09:30:00"
  },
  {
    "id": 3,
    "email": "newuser@tricol.ma",
    "fullName": "New User",
    "enabled": true,
    "locked": false,
    "roleName": null,
    "createdAt": "2026-02-02T10:00:00"
  }
]
```

---

## 📦 Data Models & DTOs

### Product DTOs

#### ProductCreateRequestDTO
```typescript
{
  reference: string;        // Max 100 chars, required
  name: string;            // Max 255 chars, required
  description: string;     // Max 1000 chars, required
  unitPrice: number;       // Decimal, min 0.01, required
  category: string;        // Max 100 chars, required
  reorderPoint: number;    // Integer, min 0, required
  unit: string;            // Max 50 chars (e.g., "PIECE", "KG"), required
}
```

#### ProductUpdateRequestDTO
```typescript
{
  reference?: string;       // Optional fields for partial update
  name?: string;
  description?: string;
  unitPrice?: number;
  category?: string;
  reorderPoint?: number;
  unit?: string;
}
```

#### ProductResponseDTO
```typescript
{
  id: number;
  reference: string;
  name: string;
  description: string;
  unitPrice: number;        // BigDecimal
  category: string;
  reorderPoint: number;
  unit: string;
  createdAt: string;        // ISO 8601 datetime
  updatedAt: string;        // ISO 8601 datetime
}
```

---

### Supplier DTOs

#### SupplierCreateRequestDTO
```typescript
{
  companyName: string;      // Max 255 chars, required
  address?: string;         // Max 2000 chars, optional
  city?: string;            // Max 255 chars, optional
  contactPerson?: string;   // Max 255 chars, optional
  email?: string;           // Valid email, max 255 chars, optional
  phone?: string;           // Max 50 chars, optional
  ice: string;              // Max 50 chars, required (Tax ID)
}
```

#### SupplierUpdateRequestDTO
```typescript
{
  companyName?: string;     // All fields optional for partial update
  address?: string;
  city?: string;
  contactPerson?: string;
  email?: string;
  phone?: string;
  ice?: string;
}
```

#### SupplierResponseDTO
```typescript
{
  id: number;
  companyName: string;
  address: string;
  city: string;
  contactPerson: string;
  email: string;
  phone: string;
  ice: string;
  createdAt: string;
  updatedAt: string;
}
```

---

### Supplier Order DTOs

#### SupplierOrderRequestDTO
```typescript
{
  supplierId: number;                          // Required
  orderDate: string;                           // ISO date (YYYY-MM-DD), required
  items: SupplierOrderItemRequestDTO[];        // At least 1 item required
}
```

#### SupplierOrderItemRequestDTO
```typescript
{
  productId: number;        // Required
  quantity: number;         // Integer, min 1, required
  unitPrice: number;        // Decimal, min 0.01, required
}
```

#### SupplierOrderUpdateDTO
```typescript
{
  orderDate?: string;       // Optional
  items?: SupplierOrderItemRequestDTO[];  // Optional
}
```

#### SupplierOrderResponseDTO
```typescript
{
  id: number;
  supplier: SupplierResponseDTO;
  orderDate: string;                    // ISO date
  totalAmount: number;                  // Calculated automatically (sum of items)
  status: OrderStatus;                  // PENDING | VALIDATED | DELIVERED | CANCELLED
  items: SupplierOrderItemResponseDTO[];
}
```

#### SupplierOrderItemResponseDTO
```typescript
{
  id: number;
  product: ProductResponseDTO;
  quantity: number;
  unitPrice: number;
  totalAmount: number;                  // quantity * unitPrice
}
```

---

### Stock DTOs

#### StockResponseDTO
```typescript
{
  productId: number;
  productReference: string;
  productName: string;
  currentStock: number;                 // Total quantity in stock
  stockValue: number;                   // FIFO valuation (BigDecimal)
  isLowStock: boolean;                  // true if currentStock <= reorderPoint
}
```

#### StockSummaryResponseDTO
```typescript
{
  stocks: StockResponseDTO[];
  totalValue: number;                   // Total FIFO value of all stock
  alerts: ProductResponseDTO[];         // Products below reorder point
  totalProducts: number;
  alertCount: number;
}
```

#### StockMovementResponseDTO
```typescript
{
  id: number;
  product: ProductResponseDTO;
  type: MovementType;                   // ENTREE | SORTIE
  quantity: number;
  lotNumber: string;                    // Generated automatically (e.g., "LOT-2026-02-001")
  movementDate: string;                 // ISO 8601 datetime
  reference: string;                    // Source reference (e.g., "SUPP_ORDER_5", "OUTBOUND_3")
  notes?: string;
}
```

---

### Stock Outbound DTOs

#### StockOutboundRequestDTO
```typescript
{
  reason: OutboundReason;               // PRODUCTION | MAINTENANCE | OTHER
  workshop: string;                     // Workshop/atelier name
  notes?: string;                       // Optional notes
  items: StockOutboundItemRequestDTO[];
}
```

#### StockOutboundItemRequestDTO
```typescript
{
  productId: number;
  quantity: number;
  notes?: string;
}
```

#### StockOutboundUpdateDTO
```typescript
{
  reason?: OutboundReason;
  workshop?: string;
  notes?: string;
  items?: StockOutboundItemRequestDTO[];
}
```

#### StockOutboundResponseDTO
```typescript
{
  id: number;
  reference: string;                    // Generated automatically (e.g., "OUT-2026-02-001")
  reason: OutboundReason;
  status: OutboundStatus;               // DRAFT | VALIDATED | CANCELLED
  workshop: string;
  notes?: string;
  items: StockOutboundItemResponseDTO[];
  createdAt: string;
  updatedAt: string;
}
```

#### StockOutboundItemResponseDTO
```typescript
{
  id: number;
  product: ProductResponseDTO;
  quantity: number;
  notes?: string;
}
```

---

### Authentication DTOs

#### LoginRequest
```typescript
{
  email: string;
  password: string;
}
```

#### RegisterRequest
```typescript
{
  email: string;
  password: string;
  fullName: string;
}
```

#### AuthResponse
```typescript
{
  accessToken: string;      // JWT token (30 min expiry)
  refreshToken: string;     // JWT token (24 hour expiry)
  tokenType: string;        // Always "Bearer"
}
```

---

### Admin DTOs

#### AssignRoleRequest
```typescript
{
  userId: number;
  roleName: string;         // ADMIN | RESPONSABLE_ACHATS | MAGASINIER | CHEF_ATELIER
}
```

#### PermissionOverrideRequest
```typescript
{
  userId: number;
  permissionName: string;   // Any permission name (see Permissions Matrix)
  granted: boolean;         // true = grant, false = revoke
}
```

#### UserResponse
```typescript
{
  id: number;
  email: string;
  fullName: string;
  enabled: boolean;
  locked: boolean;
  roleName: string | null;  // null if no role assigned
  createdAt: string;
}
```

#### RoleResponse
```typescript
{
  id: number;
  name: string;
  permissions: string[];    // Array of permission names
  createdAt: string;
}
```

#### PermissionResponse
```typescript
{
  id: number;
  name: string;             // e.g., "VIEW_PRODUCT"
  resource: string;         // e.g., "PRODUCT"
  action: string;           // e.g., "VIEW"
  createdAt: string;
}
```

#### AuditResponse
```typescript
{
  id: number;
  action: AuditAction;      // See AuditAction enum
  resourceType: AuditResourceType;  // See AuditResourceType enum
  resourceId: number;
  result: AuditResult;      // SUCCESS | FAILURE
  createdAt: string;
}
```

---

## 🔢 Enumerations

### OrderStatus
```typescript
enum OrderStatus {
  PENDING = "PENDING",           // Order created, awaiting validation
  VALIDATED = "VALIDATED",       // Order validated, awaiting delivery
  DELIVERED = "DELIVERED",       // Order received, stock lots created
  CANCELLED = "CANCELLED"        // Order cancelled
}
```

### OutboundStatus
```typescript
enum OutboundStatus {
  DRAFT = "DRAFT",               // Created but not validated
  VALIDATED = "VALIDATED",       // Validated, stock deducted (FIFO)
  CANCELLED = "CANCELLED"        // Cancelled (only from DRAFT)
}
```

### OutboundReason
```typescript
enum OutboundReason {
  PRODUCTION = "PRODUCTION",     // Stock exit for production
  MAINTENANCE = "MAINTENANCE",   // Stock exit for maintenance
  OTHER = "OTHER"                // Other reasons
}
```

### MovementType
```typescript
enum MovementType {
  ENTREE = "ENTREE",             // Stock entry (from supplier order reception)
  SORTIE = "SORTIE"              // Stock exit (from outbound validation)
}
```

### AuditAction
```typescript
enum AuditAction {
  // Authentication
  LOGIN_SUCCESS,
  LOGIN_FAILURE,
  LOGOUT,
  
  // User Management
  USER_CREATED,
  USER_UPDATED,
  USER_DELETED,
  ROLE_ASSIGNED,
  PERMISSION_GRANTED,
  PERMISSION_REVOKED,
  
  // Business Operations
  PRODUCT_CREATED,
  PRODUCT_UPDATED,
  PRODUCT_DELETED,
  SUPPLIER_CREATED,
  SUPPLIER_UPDATED,
  SUPPLIER_DELETED,
  ORDER_CREATED,
  ORDER_VALIDATED,
  ORDER_RECEIVED,
  ORDER_CANCELLED,
  OUTBOUND_CREATED,
  OUTBOUND_VALIDATED,
  OUTBOUND_CANCELLED
}
```

### AuditResourceType
```typescript
enum AuditResourceType {
  AUTHENTICATION,
  USER,
  USER_PERMISSION,
  PRODUCT,
  SUPPLIER,
  ORDER,
  STOCK,
  BON_SORTIE,
  ADMIN
}
```

### AuditResult
```typescript
enum AuditResult {
  SUCCESS = "SUCCESS",
  FAILURE = "FAILURE"
}
```

---

## 🔐 Permissions Matrix

### Available Permissions

| Permission Name | Resource | Action | Description |
|----------------|----------|--------|-------------|
| `CREATE_SUPPLIER` | SUPPLIER | CREATE | Create new supplier |
| `UPDATE_SUPPLIER` | SUPPLIER | UPDATE | Update supplier |
| `DELETE_SUPPLIER` | SUPPLIER | DELETE | Delete supplier |
| `VIEW_SUPPLIER` | SUPPLIER | VIEW | View suppliers |
| `CREATE_PRODUCT` | PRODUCT | CREATE | Create new product |
| `UPDATE_PRODUCT` | PRODUCT | UPDATE | Update product |
| `DELETE_PRODUCT` | PRODUCT | DELETE | Delete product |
| `VIEW_PRODUCT` | PRODUCT | VIEW | View products |
| `CONFIGURE_PRODUCT_THRESHOLD` | PRODUCT | CONFIGURE | Configure reorder point |
| `CREATE_ORDER` | ORDER | CREATE | Create supplier order |
| `UPDATE_ORDER` | ORDER | UPDATE | Update order (PENDING only) |
| `VALIDATE_ORDER` | ORDER | VALIDATE | Validate order |
| `CANCEL_ORDER` | ORDER | CANCEL | Cancel order |
| `RECEIVE_ORDER` | ORDER | RECEIVE | Receive order delivery |
| `VIEW_ORDER` | ORDER | VIEW | View orders |
| `VIEW_STOCK` | STOCK | VIEW | View stock levels |
| `VIEW_STOCK_VALUATION` | STOCK | VIEW_VALUATION | View stock valuation (FIFO) |
| `VIEW_STOCK_HISTORY` | STOCK | VIEW_HISTORY | View stock movements |
| `CREATE_BON_SORTIE` | BON_SORTIE | CREATE | Create/update outbound |
| `VALIDATE_BON_SORTIE` | BON_SORTIE | VALIDATE | Validate outbound |
| `CANCEL_BON_SORTIE` | BON_SORTIE | CANCEL | Cancel outbound |
| `VIEW_BON_SORTIE` | BON_SORTIE | VIEW | View outbounds |
| `MANAGE_USERS` | ADMIN | MANAGE_USERS | Manage users, roles, permissions |
| `VIEW_AUDIT_LOGS` | ADMIN | VIEW_AUDIT | View audit logs |

---

### Role Permissions Mapping

#### 1. ADMIN
**Has ALL permissions** (full access to everything)

#### 2. RESPONSABLE_ACHATS (Purchase Manager)
- **Suppliers**: CREATE, UPDATE, DELETE, VIEW
- **Products**: CREATE, UPDATE, DELETE, VIEW, CONFIGURE_PRODUCT_THRESHOLD
- **Orders**: CREATE, UPDATE, VALIDATE, CANCEL, VIEW
- **Stock**: VIEW, VIEW_STOCK_VALUATION, VIEW_STOCK_HISTORY
- **Outbounds**: VIEW only

#### 3. MAGASINIER (Warehouse Manager)
- **Suppliers**: VIEW only
- **Products**: VIEW only
- **Orders**: RECEIVE, VIEW
- **Stock**: VIEW, VIEW_STOCK_VALUATION, VIEW_STOCK_HISTORY
- **Outbounds**: CREATE, VALIDATE, CANCEL, VIEW (full management)

#### 4. CHEF_ATELIER (Workshop Manager)
- **Products**: VIEW only
- **Stock**: VIEW, VIEW_STOCK_HISTORY
- **Outbounds**: CREATE, VIEW (can create draft, cannot validate)

---

### Permission Override System

The backend supports **individual permission overrides**. This means:
- A user's effective permissions = Role permissions ± Individual overrides
- Admins can grant extra permissions to specific users
- Admins can revoke specific permissions from users

**Example**:
```
User: "workshop_lead@tricol.ma"
Role: CHEF_ATELIER (normally cannot VALIDATE_BON_SORTIE)

Admin grants override:
  - Permission: VALIDATE_BON_SORTIE
  - Granted: true

Result: This user can now validate outbounds, unlike other CHEF_ATELIER users.
```

---

## ⚠️ Error Handling

### HTTP Status Codes

| Status Code | Meaning | When Used |
|------------|---------|-----------|
| 200 | OK | Successful GET, PUT, POST (non-creation) |
| 201 | Created | Successful POST (resource created) |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Validation errors, invalid input |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | User lacks required permission |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Business rule violation (e.g., duplicate reference) |
| 500 | Internal Server Error | Unexpected server error |

### Error Response Format

All errors return a consistent format:

```typescript
{
  timestamp: string;       // ISO 8601 datetime
  status: number;          // HTTP status code
  error: string;           // Error name (e.g., "Bad Request")
  message: string;         // Human-readable error message
  path: string;            // API endpoint that failed
}
```

**Example** (400 Bad Request):
```json
{
  "timestamp": "2026-02-02T15:45:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Reference is required",
  "path": "/api/v1/products"
}
```

**Example** (403 Forbidden):
```json
{
  "timestamp": "2026-02-02T15:45:30",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/v1/products/5"
}
```

**Example** (404 Not Found):
```json
{
  "timestamp": "2026-02-02T15:45:30",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 999",
  "path": "/api/v1/products/999"
}
```

### Common Validation Errors

- **Email already exists**: `"Email already exists"` (400)
- **Required field missing**: `"<Field> is required"` (400)
- **Invalid email format**: `"Invalid email"` (400)
- **Min value violation**: `"<Field> must be at least <value>"` (400)
- **Max length exceeded**: `"<Field> must not exceed <length> characters"` (400)
- **Resource not found**: `"<Resource> not found with id: <id>"` (404)
- **Insufficient stock**: `"Insufficient stock for product <id>"` (409)
- **Invalid status transition**: `"Cannot <action> order with status <status>"` (409)

---

## 🎯 Best Practices for Frontend Integration

### 1. JWT Token Management

**Store tokens securely**:
```typescript
// Option 1: localStorage (simpler but less secure)
localStorage.setItem('accessToken', response.accessToken);
localStorage.setItem('refreshToken', response.refreshToken);

// Option 2: httpOnly cookies (more secure, requires backend support)
// Set cookies via backend Set-Cookie header
```

**Add token to all requests**:
```typescript
// Angular HttpInterceptor example
export class JwtInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('accessToken');
    
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    
    return next.handle(req);
  }
}
```

**Handle token expiration**:
```typescript
// On 401 error, redirect to login
// Optionally implement refresh token logic
```

---

### 2. Permission-Based UI

**Hide/disable features based on user permissions**:

```typescript
// Example: Permission service
@Injectable()
export class PermissionService {
  private userPermissions: Set<string> = new Set();
  
  hasPermission(permission: string): boolean {
    return this.userPermissions.has(permission);
  }
  
  hasAnyPermission(...permissions: string[]): boolean {
    return permissions.some(p => this.userPermissions.has(p));
  }
}

// In component
canCreateProduct(): boolean {
  return this.permissionService.hasPermission('CREATE_PRODUCT');
}

canEditProduct(): boolean {
  return this.permissionService.hasPermission('UPDATE_PRODUCT');
}
```

**In template (Angular standalone with @if)**:
```html
@if (canCreateProduct()) {
  <button (click)="createProduct()">Créer Produit</button>
}

@if (canEditProduct()) {
  <button (click)="editProduct(product)">Modifier</button>
}
```

---

### 3. FIFO Stock Logic (Frontend Display)

The backend handles FIFO automatically, but the frontend should:

**Display lot information**:
- Show lot number, entry date, remaining quantity
- Order lots by entry date (oldest first)
- Highlight which lots will be used for an outbound

**Example**: When creating an outbound for 100 units:
```
Product: Chemise Professionnelle (150 in stock)

Lots (FIFO order):
  ✓ LOT-2026-01-15-001 - 80 units @ 145.00 MAD (Jan 15, 2026)
  ✓ LOT-2026-02-01-002 - 20 units @ 150.00 MAD (Feb 1, 2026)
  ○ LOT-2026-02-02-003 - 50 units @ 148.00 MAD (Feb 2, 2026)
  
Outbound will consume:
  - 80 units from LOT-2026-01-15-001
  - 20 units from LOT-2026-02-01-002
```

---

### 4. Order Status Workflow UI

**Show status badges with colors**:
```typescript
getStatusColor(status: OrderStatus): string {
  switch(status) {
    case 'PENDING': return 'warning';     // Yellow/Orange
    case 'VALIDATED': return 'info';       // Blue
    case 'DELIVERED': return 'success';    // Green
    case 'CANCELLED': return 'danger';     // Red
  }
}
```

**Conditionally show action buttons**:
```html
@if (order.status === 'PENDING') {
  <button (click)="validateOrder(order.id)">Valider</button>
  <button (click)="cancelOrder(order.id)">Annuler</button>
}

@if (order.status === 'VALIDATED') {
  <button (click)="receiveOrder(order.id)">Recevoir</button>
}
```

---

### 5. Stock Alerts & Dashboard

**Display alerts prominently**:
```typescript
// Fetch alerts on dashboard load
stockService.getStockAlerts().subscribe(alerts => {
  this.lowStockProducts = alerts;
  
  if (alerts.length > 0) {
    this.showNotification(`${alerts.length} produit(s) sous le seuil`);
  }
});
```

**Visual indicators**:
```html
@for (product of products; track product.id) {
  <tr [class.bg-warning]="product.currentStock <= product.reorderPoint">
    <td>{{ product.reference }}</td>
    <td>{{ product.name }}</td>
    <td>
      {{ product.currentStock }}
      @if (product.currentStock <= product.reorderPoint) {
        <span class="badge badge-danger">⚠ Stock Faible</span>
      }
    </td>
  </tr>
}
```

---

### 6. Search & Filtering

**Implement client-side filtering** for better UX:
```typescript
// For large lists, use server-side pagination (not yet implemented in backend)
// For now, fetch all and filter client-side

filteredProducts: Product[] = [];
searchTerm: string = '';

filterProducts() {
  this.filteredProducts = this.allProducts.filter(p =>
    p.reference.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
    p.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
    p.category.toLowerCase().includes(this.searchTerm.toLowerCase())
  );
}
```

**Use advanced search for movements**:
```typescript
searchMovements(filters: MovementSearchFilters) {
  let params = new HttpParams();
  
  if (filters.productId) params = params.set('productId', filters.productId.toString());
  if (filters.type) params = params.set('type', filters.type);
  if (filters.startDate) params = params.set('startDate', filters.startDate.toISOString());
  if (filters.endDate) params = params.set('endDate', filters.endDate.toISOString());
  
  return this.http.get<StockMovement[]>('/api/v1/stock/movements/search', { params });
}
```

---

### 7. Form Validation

**Match backend validation rules**:

```typescript
// Reactive Forms example
productForm = this.fb.group({
  reference: ['', [Validators.required, Validators.maxLength(100)]],
  name: ['', [Validators.required, Validators.maxLength(255)]],
  description: ['', [Validators.required, Validators.maxLength(1000)]],
  unitPrice: [null, [Validators.required, Validators.min(0.01)]],
  category: ['', [Validators.required, Validators.maxLength(100)]],
  reorderPoint: [null, [Validators.required, Validators.min(0)]],
  unit: ['', [Validators.required, Validators.maxLength(50)]]
});
```

**Display validation errors**:
```html
<input formControlName="reference" />
@if (productForm.get('reference')?.errors?.['required'] && productForm.get('reference')?.touched) {
  <span class="error">La référence est obligatoire</span>
}
@if (productForm.get('reference')?.errors?.['maxlength']) {
  <span class="error">La référence ne doit pas dépasser 100 caractères</span>
}
```

---

### 8. Loading States & Error Feedback

**Show loading spinners**:
```typescript
isLoading = false;

loadProducts() {
  this.isLoading = true;
  this.productService.getAll().subscribe({
    next: (products) => {
      this.products = products;
      this.isLoading = false;
    },
    error: (err) => {
      this.showError(err.error.message);
      this.isLoading = false;
    }
  });
}
```

**Toast notifications** for success/error:
```typescript
// Using a toast library (e.g., ngx-toastr)
createProduct(product: Product) {
  this.productService.create(product).subscribe({
    next: () => {
      this.toastr.success('Produit créé avec succès');
      this.router.navigate(['/products']);
    },
    error: (err) => {
      this.toastr.error(err.error.message || 'Erreur lors de la création');
    }
  });
}
```

---

### 9. Confirmation Dialogs

**Confirm destructive actions**:
```typescript
deleteProduct(product: Product) {
  const confirmMsg = `Êtes-vous sûr de vouloir supprimer "${product.name}" ?`;
  
  if (confirm(confirmMsg)) {  // Or use a proper modal library
    this.productService.delete(product.id).subscribe({
      next: () => {
        this.toastr.success('Produit supprimé');
        this.loadProducts();
      },
      error: (err) => {
        this.toastr.error(err.error.message);
      }
    });
  }
}
```

---

### 10. Date Formatting

**Backend returns ISO 8601 dates**, format them for display:

```typescript
// Using Angular DatePipe
{{ product.createdAt | date:'dd/MM/yyyy HH:mm' }}

// Or using a library like date-fns
import { format } from 'date-fns';
import { fr } from 'date-fns/locale';

formatDate(dateString: string): string {
  return format(new Date(dateString), 'dd MMM yyyy HH:mm', { locale: fr });
}
```

---

## 🚀 Development Setup

### Backend Requirements

1. **Java 17+**
2. **MySQL 8.0+**
3. **Maven 3.8+**

### Environment Variables

Create a `.env` file or set system environment variables:

```bash
DB_URL=jdbc:mysql://localhost:3306/tricol_db?createDatabaseIfNotExist=true
DB_USERNAME=root
DB_PASSWORD=yourpassword
JWT_SECRET=your-256-bit-secret-key-here-minimum-32-characters
```

### Run Backend Locally

```bash
# Navigate to project root
cd C:\Users\medoa\IdeaProjects\Tricol

# Run with Maven
mvnw spring-boot:run

# Or build and run JAR
mvnw clean package
java -jar target/inventory-management-0.0.1-SNAPSHOT.jar
```

Backend will start on: `http://localhost:8080`

### Optional: Run Keycloak + MySQL with Docker

```bash
# Start containers
docker compose up -d

# Stop containers
docker compose down

# Stop and remove volumes
docker compose down -v
```

- **Keycloak**: `http://localhost:8081` (admin/admin)
- **MySQL**: `localhost:3307` (root/NewPass123!)

---

## 📚 Additional Resources

### Key Files in Backend

- **Controllers**: `src/main/java/com/tricol/Tricol/controller/`
- **Services**: `src/main/java/com/tricol/Tricol/service/`
- **Models**: `src/main/java/com/tricol/Tricol/model/`
- **DTOs**: `src/main/java/com/tricol/Tricol/dto/`
- **Security Config**: `src/main/java/com/tricol/Tricol/security/`
- **Database Migrations**: `src/main/resources/db/changelog/`

### Testing the API

Use the provided HTTP file for manual testing:
- **File**: `ALL_ENDPOINTS_TEST_DATA.http`
- **Tool**: IntelliJ HTTP Client, Postman, or curl

### Security Documentation

For detailed security implementation:
- **File**: `SECURITY_EXPLANATION.md`
- **File**: `DUAL_JWT_AUTHENTICATION.md`

---

## 🎓 Your Role as Frontend AI Agent

### Your Mission

Build a complete Angular 20 frontend application that:

1. **Authenticates users** via JWT (login/register)
2. **Implements role-based UI** showing/hiding features based on permissions
3. **Manages all CRUD operations** for Suppliers, Products, Orders, Outbounds
4. **Displays stock information** with FIFO visualization
5. **Shows real-time alerts** for low stock
6. **Provides admin interface** for user/role/permission management
7. **Handles errors gracefully** with user-friendly messages
8. **Uses Angular 20 features**: Standalone components, `@if`/`@for` control flow, signals

### Key Guidelines

- **DO NOT use NgRx** - Use services with RxJS and signals for state management
- **Use standalone components** - No NgModules
- **Use new control flow**: `@if`, `@for`, `@switch` instead of `*ngIf`, `*ngFor`, `*ngSwitch`
- **Reactive Forms** for all forms
- **HTTP Interceptor** for JWT token injection
- **Route Guards** for authentication and permission checks
- **Responsive design** (mobile-first)
- **Material Design or TailwindCSS** for UI (your choice)

### Architecture Suggestions

```
src/
├── app/
│   ├── core/                      # Singleton services
│   │   ├── services/
│   │   │   ├── auth.service.ts
│   │   │   ├── token.service.ts
│   │   │   └── permission.service.ts
│   │   ├── interceptors/
│   │   │   ├── jwt.interceptor.ts
│   │   │   └── error.interceptor.ts
│   │   └── guards/
│   │       ├── auth.guard.ts
│   │       └── permission.guard.ts
│   │
│   ├── shared/                    # Reusable components
│   │   ├── components/
│   │   │   ├── navbar/
│   │   │   ├── sidebar/
│   │   │   ├── loading-spinner/
│   │   │   └── confirm-dialog/
│   │   └── pipes/
│   │       └── date-format.pipe.ts
│   │
│   ├── features/                  # Feature modules
│   │   ├── auth/
│   │   │   ├── login/
│   │   │   └── register/
│   │   ├── dashboard/
│   │   ├── products/
│   │   │   ├── product-list/
│   │   │   ├── product-form/
│   │   │   └── product-detail/
│   │   ├── suppliers/
│   │   ├── orders/
│   │   ├── stock/
│   │   ├── outbounds/
│   │   └── admin/
│   │
│   └── models/                    # TypeScript interfaces
│       ├── product.model.ts
│       ├── supplier.model.ts
│       ├── order.model.ts
│       ├── stock.model.ts
│       ├── outbound.model.ts
│       └── user.model.ts
```

### Your First Steps

1. **Create Angular project**: `ng new tricol-frontend --standalone`
2. **Install dependencies**: Angular Material or TailwindCSS, RxJS, etc.
3. **Set up environment files** with API base URL
4. **Create core services** (AuthService, HTTP interceptors)
5. **Build auth pages** (login, register)
6. **Implement JWT storage and guards**
7. **Create main layout** (navbar, sidebar with role-based menu)
8. **Build dashboard** with stock alerts and summary cards
9. **Implement feature modules** one by one (Products → Suppliers → Orders → Stock → Outbounds → Admin)
10. **Test with real backend** using the endpoints in this document

---

## 📞 Need Help?

This backend is fully functional and tested. All endpoints are documented above with request/response examples.

**Common Questions**:

1. **Q**: How do I test the API?
   **A**: Use the `ALL_ENDPOINTS_TEST_DATA.http` file with IntelliJ or Postman.

2. **Q**: How do I get initial users/roles?
   **A**: The backend seeds 4 roles automatically on first run. Register a user, then use an admin endpoint to assign a role.

3. **Q**: What if I get 403 Forbidden?
   **A**: Check that your JWT token is valid and the user has the required permission for that endpoint.

4. **Q**: How does FIFO work?
   **A**: When you validate an outbound, the backend automatically deducts stock from the oldest lots first. You don't need to specify lots manually.

5. **Q**: Can I modify permissions per user?
   **A**: Yes! Use `/api/admin/users/permission-override` to grant/revoke individual permissions.

---

## ✅ Summary Checklist for Frontend Developer

- [ ] Understand JWT authentication flow
- [ ] Know all API endpoints and their required permissions
- [ ] Understand DTOs (request/response structures)
- [ ] Know the 4 roles and their permissions
- [ ] Understand FIFO stock logic (automatic, backend-handled)
- [ ] Know order status flow (PENDING → VALIDATED → DELIVERED)
- [ ] Know outbound status flow (DRAFT → VALIDATED)
- [ ] Understand permission override system
- [ ] Know how to handle errors (401, 403, 404, etc.)
- [ ] Understand movement search with filters
- [ ] Know how audit logging works

---

**Good luck building the frontend!** 🚀

If you have questions about specific endpoints, DTOs, or business logic, refer back to this document or test the API directly.

---

**Document Version**: 1.0  
**Last Updated**: February 2, 2026  
**Backend Version**: inventory-management-0.0.1-SNAPSHOT  
**API Base URL**: `http://localhost:8080/api`

