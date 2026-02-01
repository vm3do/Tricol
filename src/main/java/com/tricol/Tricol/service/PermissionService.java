package com.tricol.Tricol.service;

import com.tricol.Tricol.model.Permission;
import com.tricol.Tricol.model.UserApp;
import com.tricol.Tricol.model.UserPermission;
import com.tricol.Tricol.repository.UserPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final UserPermissionRepository userPermissionRepository;

    public Set<Permission> getUserPermissions(UserApp user) {
        Set<Permission> permissions = new HashSet<>();

        if (user.getRole() != null) {
            permissions.addAll(user.getRole().getPermissions());
        }

        List<UserPermission> overrides = userPermissionRepository.findByUser(user);
        for (UserPermission override : overrides) {
            if (override.getGranted()) {
                permissions.add(override.getPermission());
            } else {
                permissions.remove(override.getPermission());
            }
        }

        return permissions;
    }
}
