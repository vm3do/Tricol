# Tricol API Documentation - Security Endpoints

## Base URL
```
http://localhost:8080
```

---

## Authentication Endpoints

### 1. Register User
**POST** `/api/auth/register`

**Request Body:**
```json
{
  "email": "amine@tricol.com",
  "password": "password123",
  "fullName": "Amine Benali"
}
```

**Response:** `200 OK`
```json
"User registered successfully"
```

---

### 2. Login
**POST** `/api/auth/login`

**Request Body:**
```json
{
  "email": "amine@tricol.com",
  "password": "password123"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

---

## Admin Endpoints (Requires MANAGE_USERS permission)

### 3. Assign Role to User
**POST** `/api/admin/users/assign-role`

**Headers:**
```
Authorization: Bearer <admin-access-token>
```

**Request Body:**
```json
{
  "userId": 1,
  "roleName": "MAGASINIER"
}
```

**Response:** `200 OK`
```json
"Role assigned successfully"
```

---

### 4. Override User Permission
**POST** `/api/admin/users/permission-override`

**Headers:**
```
Authorization: Bearer <admin-access-token>
```

**Request Body (Revoke):**
```json
{
  "userId": 1,
  "permissionName": "CREATE_BON_SORTIE",
  "granted": false,
  "reason": "Training period restriction"
}
```

**Request Body (Grant):**
```json
{
  "userId": 2,
  "permissionName": "VIEW_AUDIT_LOGS",
  "granted": true,
  "reason": "Compliance review access"
}
```

**Response:** `200 OK`
```json
"Permission override applied successfully"
```

---

### 5. Get All Users
**GET** `/api/admin/users`

**Headers:**
```
Authorization: Bearer <admin-access-token>
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "email": "amine@tricol.com",
    "fullName": "Amine Benali",
    "enabled": true,
    "locked": false,
    "role": {
      "id": 3,
      "name": "MAGASINIER"
    }
  }
]
```

---

### 6. Get All Roles
**GET** `/api/admin/roles`

**Headers:**
```
Authorization: Bearer <admin-access-token>
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "ADMIN"
  },
  {
    "id": 2,
    "name": "RESPONSABLE_ACHATS"
  },
  {
    "id": 3,
    "name": "MAGASINIER"
  },
  {
    "id": 4,
    "name": "CHEF_ATELIER"
  }
]
```

---

### 7. Get All Permissions
**GET** `/api/admin/permissions`

**Headers:**
```
Authorization: Bearer <admin-access-token>
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "CREATE_SUPPLIER",
    "resource": "SUPPLIER",
    "action": "CREATE"
  },
  {
    "id": 2,
    "name": "VIEW_SUPPLIER",
    "resource": "SUPPLIER",
    "action": "VIEW"
  }
]
```

---

## Protected Business Endpoints (Examples)

### 8. Get All Products
**GET** `/api/products`

**Required Permission:** `VIEW_PRODUCT`

**Headers:**
```
Authorization: Bearer <access-token>
```

---

### 9. Create Product
**POST** `/api/products`

**Required Permission:** `CREATE_PRODUCT`

**Headers:**
```
Authorization: Bearer <access-token>
```

---

### 10. Get All Suppliers
**GET** `/api/suppliers`

**Required Permission:** `VIEW_SUPPLIER`

**Headers:**
```
Authorization: Bearer <access-token>
```

---

### 11. Create Supplier
**POST** `/api/suppliers`

**Required Permission:** `CREATE_SUPPLIER`

**Headers:**
```
Authorization: Bearer <access-token>
```

---

### 12. Validate Order
**PUT** `/api/orders/{id}/validate`

**Required Permission:** `VALIDATE_ORDER`

**Headers:**
```
Authorization: Bearer <access-token>
```

---

### 13. Receive Order
**PUT** `/api/orders/{id}/receive`

**Required Permission:** `RECEIVE_ORDER`

**Headers:**
```
Authorization: Bearer <access-token>
```

---

### 14. Create Bon de Sortie
**POST** `/api/stock-outbound`

**Required Permission:** `CREATE_BON_SORTIE`

**Headers:**
```
Authorization: Bearer <access-token>
```

---

### 15. Validate Bon de Sortie
**PUT** `/api/stock-outbound/{id}/validate`

**Required Permission:** `VALIDATE_BON_SORTIE`

**Headers:**
```
Authorization: Bearer <access-token>
```

---

## Permission Matrix

| Role | Permissions |
|------|-------------|
| **ADMIN** | All permissions |
| **RESPONSABLE_ACHATS** | CREATE_SUPPLIER, UPDATE_SUPPLIER, DELETE_SUPPLIER, VIEW_SUPPLIER, CREATE_PRODUCT, UPDATE_PRODUCT, DELETE_PRODUCT, VIEW_PRODUCT, CONFIGURE_PRODUCT_THRESHOLD, CREATE_ORDER, UPDATE_ORDER, VALIDATE_ORDER, CANCEL_ORDER, VIEW_ORDER, VIEW_STOCK, VIEW_STOCK_VALUATION, VIEW_STOCK_HISTORY, VIEW_BON_SORTIE |
| **MAGASINIER** | VIEW_SUPPLIER, VIEW_PRODUCT, RECEIVE_ORDER, VIEW_ORDER, VIEW_STOCK, VIEW_STOCK_VALUATION, VIEW_STOCK_HISTORY, CREATE_BON_SORTIE, VALIDATE_BON_SORTIE, CANCEL_BON_SORTIE, VIEW_BON_SORTIE |
| **CHEF_ATELIER** | VIEW_PRODUCT, VIEW_STOCK, VIEW_STOCK_HISTORY, CREATE_BON_SORTIE, VIEW_BON_SORTIE |

---

## Error Responses

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "Access Denied"
}
```

### 400 Bad Request
```json
{
  "error": "Bad Request",
  "message": "Email already exists"
}
```

---

## Testing Flow

### Step 1: Register Admin User
```bash
POST /api/auth/register
{
  "email": "admin@tricol.com",
  "password": "admin123",
  "fullName": "Admin User"
}
```

### Step 2: Manually Assign ADMIN Role (Database)
```sql
UPDATE users SET role_id = 1 WHERE email = 'admin@tricol.com';
```

### Step 3: Login as Admin
```bash
POST /api/auth/login
{
  "email": "admin@tricol.com",
  "password": "admin123"
}
```

### Step 4: Register Regular User
```bash
POST /api/auth/register
{
  "email": "amine@tricol.com",
  "password": "password123",
  "fullName": "Amine Benali"
}
```

### Step 5: Assign Role (As Admin)
```bash
POST /api/admin/users/assign-role
Authorization: Bearer <admin-token>
{
  "userId": 2,
  "roleName": "MAGASINIER"
}
```

### Step 6: Login as Regular User
```bash
POST /api/auth/login
{
  "email": "amine@tricol.com",
  "password": "password123"
}
```

### Step 7: Test Protected Endpoint
```bash
GET /api/products
Authorization: Bearer <amine-token>
```

### Step 8: Override Permission (As Admin)
```bash
POST /api/admin/users/permission-override
Authorization: Bearer <admin-token>
{
  "userId": 2,
  "permissionName": "CREATE_BON_SORTIE",
  "granted": false,
  "reason": "Training period"
}
```

### Step 9: Test After Override
```bash
POST /api/stock-outbound
Authorization: Bearer <amine-token>
```
**Expected:** 403 Forbidden (permission revoked)
