package com.tricol.Tricol.service;

import com.tricol.Tricol.enums.AuditAction;
import com.tricol.Tricol.enums.AuditResourceType;
import com.tricol.Tricol.enums.AuditResult;
import com.tricol.Tricol.model.AuditLog;
import com.tricol.Tricol.model.UserApp;
import com.tricol.Tricol.repository.AuditLogRepository;
import com.tricol.Tricol.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void log(AuditAction action, AuditResourceType resourceType, Long resourceId, AuditResult result) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return;
        }

        String username = authentication.getName();
        UserApp user = userRepository.findByEmail(username).orElse(null);

        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .result(result)
                .build();

        auditLogRepository.save(auditLog);
    }

    @Transactional
    public void logSuccess(AuditAction action, AuditResourceType resourceType, Long resourceId) {
        log(action, resourceType, resourceId, AuditResult.SUCCESS);
    }

    @Transactional
    public void logFailure(AuditAction action, AuditResourceType resourceType, Long resourceId) {
        log(action, resourceType, resourceId, AuditResult.FAILURE);
    }

    @Transactional
    public void logWithUser(UserApp user, AuditAction action, AuditResourceType resourceType, Long resourceId, AuditResult result){
        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .result(result)
                .build();

        auditLogRepository.save(auditLog);
    }
}
