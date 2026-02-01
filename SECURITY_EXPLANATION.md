# Security Implementation - Complete Explanation

## What Was Added

### 1. @PreAuthorize Annotations
Added to all existing controllers to enforce permission checks.

**How it works:**
- Before method executes, Spring Security checks if user has required authority
- If user has permission → method executes
- If user lacks permission → 403 Forbidden returned

**Example:**
```java
@GetMapping
@PreAuthorize("hasAuthority('VIEW_PRODUCT')")
public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
    return ResponseEntity.ok().body(productService.findAllProducts());
}
```

**Flow:**
1. Request arrives with JWT
2. JwtAuthenticationFilter extracts authorities from JWT
3. SecurityContext populated with authorities
4. @PreAuthorize checks: Does user have 'VIEW_PRODUCT'?
5. Yes → Execute method | No → Return 403

---

### 2. AuditService
Logs all security-related and sensitive business actions.

**What it does:**
- Extracts current user from SecurityContext
- Creates AuditLog entry with: who, what, when, result
- Saves to audit_logs table

**When it's called:**
- User registration
- Login success/failure
- Role assignment
- Permission override
- (Can be added to business operations)

**Example:**
```java
auditService.logSuccess(AuditAction.LOGIN_SUCCESS, AuditResourceType.AUTHENTICATION, user.getId());
```

**Database record created:**
```
user_id: 123
action: LOGIN_SUCCESS
resource_type: AUTHENTICATION
resource_id: 123
result: SUCCESS
timestamp: 2024-01-15 10:30:00
```

---

### 3. AdminController
New controller for user/role/permission management.

**Endpoints:**
- `POST /api/admin/users/assign-role` - Assign role to user
- `POST /api/admin/users/permission-override` - Grant/revoke specific permission
- `GET /api/admin/users` - List all users
- `GET /api/admin/roles` - List all roles
- `GET /api/admin/permissions` - List all permissions

**All endpoints require MANAGE_USERS permission (only ADMIN has this).**

---

## Complete Authentication Flow

### Registration
```
POST /api/auth/register
↓
AuthController.register()
↓
Check if email exists
↓
Hash password with BCrypt
↓
Create UserApp (no role)
↓
Save to database
↓
AuditService logs USER_CREATED
↓
Return success
```

### Login
```
POST /api/auth/login
↓
AuthController.login()
↓
AuthenticationManager.authenticate()
↓
DaoAuthenticationProvider
↓
CustomUserDetailsService.loadUserByUsername()
  ├─ Load user from database
  ├─ PermissionService.getUserPermissions()
  │  ├─ Get role permissions
  │  └─ Apply user overrides
  └─ Return UserDetails with authorities
↓
Validate password (BCrypt)
↓
JwtUtil.generateAccessToken() - Contains authorities
JwtUtil.generateRefreshToken()
↓
AuditService logs LOGIN_SUCCESS
↓
Return tokens
```

### Authenticated Request
```
GET /api/products
Header: Authorization: Bearer <token>
↓
JwtAuthenticationFilter.doFilterInternal()
  ├─ Extract token from header
  ├─ JwtUtil.validateToken()
  ├─ JwtUtil.extractUsername()
  ├─ JwtUtil.extractAuthorities()
  └─ Set SecurityContext
↓
ProductController.getAllProducts()
↓
@PreAuthorize("hasAuthority('VIEW_PRODUCT')")
  ├─ Check SecurityContext authorities
  └─ Has VIEW_PRODUCT? → Allow | No? → 403
↓
ProductService.findAllProducts()
↓
Return data
```

---

## Permission Resolution Logic

### User with Role (No Overrides)
```
User: Hassan (MAGASINIER)
Role permissions: [VIEW_STOCK, CREATE_BON_SORTIE, VALIDATE_BON_SORTIE]
User overrides: []

Final permissions: [VIEW_STOCK, CREATE_BON_SORTIE, VALIDATE_BON_SORTIE]
```

### User with Permission Revoked
```
User: Amine (MAGASINIER)
Role permissions: [VIEW_STOCK, CREATE_BON_SORTIE, VALIDATE_BON_SORTIE]
User overrides: [CREATE_BON_SORTIE = FALSE]

Step 1: Start with role permissions
Step 2: Apply override → Remove CREATE_BON_SORTIE
Final permissions: [VIEW_STOCK, VALIDATE_BON_SORTIE]
```

### User with Extra Permission Granted
```
User: Sara (RESPONSABLE_ACHATS)
Role permissions: [CREATE_SUPPLIER, VIEW_SUPPLIER, ...]
User overrides: [VIEW_AUDIT_LOGS = TRUE]

Step 1: Start with role permissions
Step 2: Apply override → Add VIEW_AUDIT_LOGS
Final permissions: [CREATE_SUPPLIER, VIEW_SUPPLIER, ..., VIEW_AUDIT_LOGS]
```

---

## Audit Logging

### What Gets Logged
- User registration
- Login success/failure
- Role assignments
- Permission overrides
- (Can add: CRUD operations on sensitive resources)

### Audit Table Structure
```
audit_logs
├── id
├── user_id (who did it)
├── action (what they did)
├── resource_type (what type of resource)
├── resource_id (which specific resource)
├── result (SUCCESS or FAILURE)
└── timestamp (when)
```

### Example Queries
```sql
-- All actions by user
SELECT * FROM audit_logs WHERE user_id = 123;

-- All failed login attempts
SELECT * FROM audit_logs WHERE action = 'LOGIN_FAILURE';

-- All permission changes
SELECT * FROM audit_logs WHERE action IN ('PERMISSION_GRANTED', 'PERMISSION_REVOKED');

-- Actions in last 24 hours
SELECT * FROM audit_logs WHERE timestamp > NOW() - INTERVAL 1 DAY;
```

---

## Security Features Implemented

### 1. Stateless Authentication
- JWT tokens (no server-side sessions)
- Scalable (any server can validate any token)
- Mobile/web friendly

### 2. Role-Based Access Control (RBAC)
- 4 roles: ADMIN, RESPONSABLE_ACHATS, MAGASINIER, CHEF_ATELIER
- Each role has default permissions

### 3. Dynamic Permission Overrides
- Admin can grant/revoke individual permissions
- Overrides take precedence over role defaults
- Flexible without creating custom roles

### 4. Method-Level Security
- @PreAuthorize on controller methods
- Fine-grained permission checks
- Clear permission requirements

### 5. Audit Trail
- All security events logged
- Who did what, when
- Success/failure tracking

### 6. Password Security
- BCrypt hashing (slow by design)
- Salted automatically
- Never stored in plain text

### 7. Account Management
- Enable/disable accounts
- Lock suspicious accounts
- No role = no permissions

---

## Testing Scenarios

### Scenario 1: New User Registration
```
1. Register user → No role, no permissions
2. Try to access /api/products → 403 Forbidden
3. Admin assigns MAGASINIER role
4. User logs in again → Gets new JWT with permissions
5. Try to access /api/products → 200 OK (has VIEW_PRODUCT)
```

### Scenario 2: Permission Override
```
1. Amine (MAGASINIER) can CREATE_BON_SORTIE
2. Admin revokes CREATE_BON_SORTIE from Amine
3. Amine's current JWT still works (until expiration)
4. After 30 min, JWT expires
5. Amine logs in again → New JWT without CREATE_BON_SORTIE
6. Try to create bon de sortie → 403 Forbidden
```

### Scenario 3: Audit Trail
```
1. Admin assigns role to user → Logged
2. Admin revokes permission → Logged
3. User logs in → Logged
4. User fails login → Logged
5. Query audit_logs table → See all actions
```

---

## Key Design Decisions

### Why JWT?
- Stateless (no session storage)
- Self-contained (all info in token)
- Scalable (any server validates)
- Standard for REST APIs

### Why @PreAuthorize?
- Declarative (clear at method level)
- Testable (easy to mock)
- Maintainable (change in one place)
- Spring Security standard

### Why Separate Audit Table?
- Immutable history
- Query performance
- Compliance requirements
- Separate from business data

### Why Permission Overrides?
- Flexibility without custom roles
- Temporary permissions
- Individual exceptions
- Audit trail of changes

---

## What Happens on Each Request

```
1. Client sends request with JWT in Authorization header
2. JwtAuthenticationFilter intercepts
3. Extract and validate JWT
4. Extract username and authorities from JWT
5. Create Authentication object
6. Set in SecurityContext (thread-local)
7. Request continues to controller
8. @PreAuthorize checks authorities in SecurityContext
9. If authorized → Execute method
10. If not authorized → Return 403
11. After response → SecurityContext cleared
```

**Key Point:** Server doesn't remember anything between requests. Each request is independent.

---

## Summary

**What you built:**
- Complete JWT authentication system
- Role-based access control with dynamic overrides
- Method-level security on all endpoints
- Comprehensive audit logging
- Admin panel for user management

**What it provides:**
- Secure API access
- Flexible permission management
- Complete audit trail
- Scalable architecture
- Production-ready security
