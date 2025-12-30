package com.tricol.Tricol.repository;

import com.tricol.Tricol.model.RoleApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleApp, Long> {

    @Query("SELECT r FROM RoleApp r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<RoleApp> findByName(@Param("name") String name);
}
