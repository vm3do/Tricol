package com.tricol.Tricol.controller;

import com.tricol.Tricol.dto.request.AssignRoleRequest;
import com.tricol.Tricol.dto.request.PermissionOverrideRequest;
//import com.tricol.Tricol.dto.response.PermissionResponse;
import com.tricol.Tricol.dto.response.AuditResponse;
import com.tricol.Tricol.dto.response.PermissionResponse;
import com.tricol.Tricol.dto.response.RoleResponse;
import com.tricol.Tricol.dto.response.UserResponse;
import com.tricol.Tricol.enums.AuditAction;
import com.tricol.Tricol.enums.AuditResourceType;
import com.tricol.Tricol.model.*;
import com.tricol.Tricol.repository.*;
import com.tricol.Tricol.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_USERS')")
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;

    @PostMapping("/users/assign-role")
    public ResponseEntity<String> assignRole(@RequestBody AssignRoleRequest request) {
        UserApp user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RoleApp role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRole(role);
        userRepository.save(user);

        auditService.logSuccess(AuditAction.ROLE_ASSIGNED, AuditResourceType.USER, user.getId());

        return ResponseEntity.ok("Role assigned successfully");
    }

    @PostMapping("/users/permission-override")
    public ResponseEntity<String> overridePermission(@RequestBody PermissionOverrideRequest request) {
        UserApp user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Permission permission = permissionRepository.findByName(request.getPermissionName())
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserApp admin = userRepository.findByEmail(authentication.getName()).orElse(null);

        UserPermission userPermission = UserPermission.builder()
                .user(user)
                .permission(permission)
                .granted(request.getGranted())
                .grantedBy(admin)
                .build();

        userPermissionRepository.save(userPermission);

        AuditAction action = request.getGranted() ? AuditAction.PERMISSION_GRANTED : AuditAction.PERMISSION_REVOKED;
        auditService.logSuccess(action, AuditResourceType.USER_PERMISSION, user.getId());

        return ResponseEntity.ok("Permission override applied successfully");
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .enabled(user.getEnabled())
                        .locked(user.getLocked())
                        .roleName(user.getRole() != null ? user.getRole().getName() : null)
                        .createdAt(user.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleRepository.findAll().stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .permissions(role.getPermissions().stream()
                                .map(Permission::getName)
                                .collect(Collectors.toList()))
                        .createdAt(role.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }

//    @GetMapping("/permissions")
////    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
////        List<PermissionResponse> permissions = permissionRepository.findAll().stream()
////                .map(permission -> PermissionResponse.builder()
////                        .id(permission.getId())
////                        .name(permission.getName())
////                        .resource(permission.getResource())
////                        .action(permission.getAction())
////                        .createdAt(permission.getCreatedAt())
////                        .build())
////                .collect(Collectors.toList());
////        return ResponseEntity.ok(permissions);
////    }

    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions(){
        List<PermissionResponse> permissions = permissionRepository.findAll().stream().map(permission -> PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .resource(permission.getResource())
                .action(permission.getAction())
                .createdAt(permission.getCreatedAt())
                .build()).toList();

        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditResponse>> getAllAudits(){
        List<AuditResponse> audits = auditLogRepository.findAll().stream()
                .map(audit -> AuditResponse.builder()
                        .id(audit.getId())
                        .action(audit.getAction())
                        .resourceType(audit.getResourceType())
                        .resourceId(audit.getResourceId())
                        .result(audit.getResult())
                        .createdAt(audit.getCreated_at())
                        .build()).toList();

        return ResponseEntity.ok(audits);
    }
}
