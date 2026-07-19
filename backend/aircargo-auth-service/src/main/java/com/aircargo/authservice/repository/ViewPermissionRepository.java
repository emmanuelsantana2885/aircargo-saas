package com.aircargo.authservice.repository;

import com.aircargo.authservice.entity.ViewPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViewPermissionRepository extends JpaRepository<ViewPermission, UUID> {
    Optional<ViewPermission> findByCode(String code);
    List<ViewPermission> findByIsActiveTrueOrderByCategoryAscCodeAsc();
    List<ViewPermission> findAllByOrderByCategoryAscCodeAsc();
    boolean existsByCode(String code);
}
