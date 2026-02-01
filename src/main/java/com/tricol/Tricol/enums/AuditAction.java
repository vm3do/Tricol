package com.tricol.Tricol.enums;

public enum AuditAction {
    // Authentication
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    TOKEN_REFRESH,
    
    // User Management
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    USER_ENABLED,
    USER_DISABLED,
    PASSWORD_CHANGED,
    
    // Role & Permission Management
    ROLE_ASSIGNED,
    ROLE_REMOVED,
    PERMISSION_GRANTED,
    PERMISSION_REVOKED,
    
    // Supplier Operations
    SUPPLIER_CREATED,
    SUPPLIER_UPDATED,
    SUPPLIER_DELETED,
    
    // Product Operations
    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    PRODUCT_DELETED,
    PRODUCT_THRESHOLD_CONFIGURED,
    
    // Order Operations
    ORDER_CREATED,
    ORDER_UPDATED,
    ORDER_VALIDATED,
    ORDER_CANCELLED,
    ORDER_RECEIVED,
    
    // Stock Operations
    STOCK_ADJUSTED,
    
    // Bon de Sortie Operations
    BON_SORTIE_CREATED,
    BON_SORTIE_VALIDATED,
    BON_SORTIE_CANCELLED
}
