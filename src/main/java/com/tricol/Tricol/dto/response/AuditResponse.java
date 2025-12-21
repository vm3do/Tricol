package com.tricol.Tricol.dto.response;

import com.tricol.Tricol.enums.AuditAction;
import com.tricol.Tricol.enums.AuditResourceType;
import com.tricol.Tricol.enums.AuditResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditResponse {
    private Long id;
    private AuditAction action;
    private Long resourceId;
    private AuditResourceType resourceType;
    private AuditResult result;
    private LocalDateTime createdAt;
}
