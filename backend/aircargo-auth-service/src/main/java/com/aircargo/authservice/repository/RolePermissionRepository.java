package com.aircargo.authservice.repository;

import com.aircargo.authservice.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {
    List<RolePermission> findByRole(String role);
    List<RolePermission> findByRoleAndCanAccessTrue(String role);
    Optional<RolePermission> findByRoleAndViewPermissionCode(String role, String viewCode);
    void deleteByRole(String role);
    boolean existsByRoleAndViewPermissionCodeAndCanAccessTrue(String role, String viewCode);
}
