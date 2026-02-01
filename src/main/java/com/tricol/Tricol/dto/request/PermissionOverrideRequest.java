package com.tricol.Tricol.dto.request;

import lombok.Data;

@Data
public class PermissionOverrideRequest {
    private Long userId;
    private String permissionName;
    private Boolean granted;
    private String reason;
}
