# Dual JWT Authentication Implementation Summary

## Overview
Successfully implemented dual JWT authentication supporting both **local JWT** (internal users) and **Keycloak OAuth2** (external users) in a Spring Boot application.

## Architecture

### Filter Chain Order
1. **KeycloakJwtFilter** (runs first)
   - Attempts to decode token with Keycloak's public key
   - Checks if issuer matches Keycloak
   - If successful: extracts authorities, sets authentication, continues
   - If fails: clears context, passes to next filter

2. **JwtAuthenticationFilter** (runs second)
   - Checks if authentication already set (by Keycloak filter)
   - If yes: skips processing
   - If no: validates local JWT with symmetric key, sets authentication

### Components Created/Modified

#### New Files Created:
1. **KeycloakJwtFilter.java**
   - Location: `src/main/java/com/tricol/Tricol/security/filter/`
   - Purpose: Validates Keycloak JWT tokens

2. **JwtAuthoritiesConverter.java**
   - Location: `src/main/java/com/tricol/Tricol/security/converter/`
   - Purpose: Converts JWT claims to Spring Security authorities
   - Routes based on issuer claim

3. **JwtDecoderConfig.java**
   - Location: `src/main/java/com/tricol/Tricol/config/`
   - Purpose: Defines two JwtDecoder beans
     - `localJwtDecoder`: symmetric key (HS256)
     - `keycloakJwtDecoder`: public key from JWK endpoint (RS256)

4. **RoleRepository.java**
   - Location: `src/main/java/com/tricol/Tricol/repository/`
   - Purpose: Fetches roles with permissions eagerly to avoid LazyInitializationException

#### Modified Files:
1. **SecurityConfig.java**
   - Added KeycloakJwtFilter to filter chain
   - Kept existing JwtAuthenticationFilter

2. **JwtAuthenticationFilter.java**
   - Added check to skip if already authenticated by Keycloak filter

3. **JwtUtil.java**
   - Added `.issuer(localIssuer)` to token generation

4. **application.properties**
   - Added Keycloak configuration properties

## How It Works

### Local JWT Authentication Flow
```
Request with local JWT
  ↓
KeycloakJwtFilter: Decode fails (wrong algorithm) → Pass to next filter
  ↓
JwtAuthenticationFilter: Validate with local secret → Set authentication
  ↓
Controller: User authenticated with local permissions
```

### Keycloak JWT Authentication Flow
```
Request with Keycloak JWT
  ↓
KeycloakJwtFilter: Decode success → Extract roles → Lookup permissions in DB → Set authentication
  ↓
JwtAuthenticationFilter: Skip (already authenticated)
  ↓
Controller: User authenticated with Keycloak-derived permissions
```

## Key Implementation Details

### 1. Token Routing by Issuer
- **Local tokens:** `iss: "tricol-local"`
- **Keycloak tokens:** `iss: "http://localhost:8081/realms/tricol"`

### 2. Authority Extraction

**Local JWT:**
- Permissions embedded directly in token claims
- Read from `authorities` claim
- Already includes permission overrides

**Keycloak JWT:**
- Roles extracted from `resource_access.tricol-client.roles`
- Roles mapped to database RoleApp entities
- Permissions fetched from role associations
- No user-specific overrides (Keycloak users don't exist in DB)

### 3. Lazy Loading Fix
Added `@Query` with `JOIN FETCH` in RoleRepository to eagerly load permissions:
```java
@Query("SELECT r FROM RoleApp r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
Optional<RoleApp> findByName(@Param("name") String name);
```

## Configuration

### application.properties
```properties
# Local JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=1800000
jwt.refresh-expiration=86400000
jwt.issuer=tricol-local

# Keycloak OAuth2
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081/realms/tricol
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8081/realms/tricol/protocol/openid-connect/certs
```

### Docker Compose
```yaml
services:
  mysql:
    # MySQL for Keycloak (port 3307)
  
  keycloak:
    # Keycloak server (port 8081)
```

## Testing

### Local JWT
1. Login: `POST /api/auth/login` with credentials
2. Get token from response
3. Use token: `GET /api/v1/products` with `Authorization: Bearer <token>`
4. Result: ✅ 200 OK with data

### Keycloak JWT
1. Get token: `POST http://localhost:8081/realms/tricol/protocol/openid-connect/token` with client credentials
2. Use token: `GET /api/v1/products` with `Authorization: Bearer <token>`
3. Result: ✅ 200 OK with data

## Security Considerations

1. **No user provisioning:** Keycloak users are NOT stored in application database
2. **Permission mapping:** Keycloak roles must match database role names
3. **Stateless:** Both authentication methods remain stateless
4. **Filter ordering:** Critical that Keycloak filter runs before local filter
5. **Exception handling:** Failed Keycloak authentication falls through to local authentication

## Known Limitations

1. **Keycloak users:** Cannot have individual permission overrides (no DB entry)
2. **Role naming:** Keycloak role names must exactly match database RoleApp.name
3. **Token expiration:** Handled by respective token issuers
4. **Refresh tokens:** Only supported for local authentication

## Troubleshooting

### Issue: LazyInitializationException
**Solution:** Use `JOIN FETCH` in repository query to eagerly load permissions

### Issue: Local JWT validated by Keycloak decoder
**Solution:** Check filter order and ensure local filter checks for existing authentication

### Issue: Keycloak authentication cleared
**Solution:** Ensure local filter skips processing when authentication already set

## Future Enhancements

1. Add role mapping table for Keycloak role name translation
2. Implement refresh token support for Keycloak
3. Add user provisioning option for Keycloak users (optional)
4. Implement audit logging for Keycloak authentication events
5. Add support for multiple Keycloak realms

---

**Implementation Date:** January 26, 2026  
**Status:** ✅ Fully functional - both local and Keycloak JWT authentication working

