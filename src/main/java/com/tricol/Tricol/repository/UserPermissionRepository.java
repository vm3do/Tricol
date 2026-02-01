package com.tricol.Tricol.repository;

import com.tricol.Tricol.model.UserApp;
import com.tricol.Tricol.model.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
    List<UserPermission> findByUser(UserApp user);
}
