package com.aircargo.authservice.service;

import com.aircargo.authservice.dto.RolePermissionBulkUpdateRequest;
import com.aircargo.authservice.dto.RolePermissionDTO;
import com.aircargo.authservice.dto.ViewPermissionDTO;
import com.aircargo.authservice.entity.RolePermission;
import com.aircargo.authservice.entity.ViewPermission;
import com.aircargo.authservice.repository.RolePermissionRepository;
import com.aircargo.authservice.repository.ViewPermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final ViewPermissionRepository viewPermissionRepository;

    public RolePermissionService(RolePermissionRepository rolePermissionRepository,
                                  ViewPermissionRepository viewPermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
        this.viewPermissionRepository = viewPermissionRepository;
    }

    public List<ViewPermissionDTO> getAllViews() {
        return viewPermissionRepository.findAllByOrderByCategoryAscCodeAsc().stream()
                .map(ViewPermissionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<RolePermissionDTO> getAllRolesWithPermissions() {
        List<ViewPermission> allViews = viewPermissionRepository.findAllByOrderByCategoryAscCodeAsc();
        List<RolePermission> allRolePerms = rolePermissionRepository.findAll();

        Map<String, Map<String, Boolean>> roleViewMap = new HashMap<>();
        for (RolePermission rp : allRolePerms) {
            roleViewMap
                .computeIfAbsent(rp.getRole(), k -> new HashMap<>())
                .put(rp.getViewPermission().getCode(), rp.getCanAccess());
        }

        List<String> roles = Arrays.asList(
            "READ_ONLY", "WAREHOUSE_ASSISTANT", "OPERATIONS", "TRAFFIC",
            "LOAD_PLANNER", "ADMIN", "SUPER_USER"
        );

        List<RolePermissionDTO> result = new ArrayList<>();
        for (String role : roles) {
            Map<String, Boolean> permMap = roleViewMap.getOrDefault(role, new HashMap<>());
            List<RolePermissionDTO.ViewAccessDTO> views = allViews.stream()
                .map(v -> RolePermissionDTO.ViewAccessDTO.builder()
                    .viewCode(v.getCode())
                    .viewName(v.getName())
                    .viewDescription(v.getDescription())
                    .category(v.getCategory())
                    .canAccess(permMap.getOrDefault(v.getCode(), false))
                    .build())
                .collect(Collectors.toList());
            result.add(RolePermissionDTO.builder()
                .role(role)
                .views(views)
                .build());
        }
        return result;
    }

    public RolePermissionDTO getRolePermissions(String role) {
        List<ViewPermission> allViews = viewPermissionRepository.findAllByOrderByCategoryAscCodeAsc();
        List<RolePermission> rolePerms = rolePermissionRepository.findByRole(role);
        Set<String> accessibleViews = rolePerms.stream()
            .filter(RolePermission::getCanAccess)
            .map(rp -> rp.getViewPermission().getCode())
            .collect(Collectors.toSet());

        List<RolePermissionDTO.ViewAccessDTO> views = allViews.stream()
            .map(v -> RolePermissionDTO.ViewAccessDTO.builder()
                .viewCode(v.getCode())
                .viewName(v.getName())
                .viewDescription(v.getDescription())
                .category(v.getCategory())
                .canAccess(accessibleViews.contains(v.getCode()))
                .build())
            .collect(Collectors.toList());

        return RolePermissionDTO.builder()
            .role(role)
            .views(views)
            .build();
    }

    @Transactional
    public void updateRolePermissions(String role, RolePermissionBulkUpdateRequest request) {
        rolePermissionRepository.deleteByRole(role);
        rolePermissionRepository.flush();

        if (request.getPermissions() == null) return;

        for (Map.Entry<String, Boolean> entry : request.getPermissions().entrySet()) {
            String viewCode = entry.getKey();
            Boolean canAccess = entry.getValue();
            if (canAccess == null || !canAccess) continue;

            ViewPermission view = viewPermissionRepository.findByCode(viewCode)
                    .orElse(null);
            if (view == null) continue;

            RolePermission rp = RolePermission.builder()
                    .role(role)
                    .viewPermission(view)
                    .canAccess(true)
                    .build();
            rolePermissionRepository.save(rp);
        }
    }

    @Transactional
    public ViewPermissionDTO createView(ViewPermissionDTO dto) {
        if (viewPermissionRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Ya existe una vista con el codigo: " + dto.getCode());
        }
        ViewPermission entity = new ViewPermission();
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        ViewPermission saved = viewPermissionRepository.save(entity);
        return ViewPermissionDTO.fromEntity(saved);
    }

    @Transactional
    public ViewPermissionDTO updateView(UUID id, ViewPermissionDTO dto) {
        return viewPermissionRepository.findById(id)
                .map(existing -> {
                    existing.setCode(dto.getCode());
                    existing.setName(dto.getName());
                    existing.setDescription(dto.getDescription());
                    existing.setCategory(dto.getCategory());
                    existing.setIsActive(dto.getIsActive());
                    return viewPermissionRepository.save(existing);
                })
                .map(ViewPermissionDTO::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Vista no encontrada: " + id));
    }

    @Transactional
    public void deleteView(UUID id) {
        if (!viewPermissionRepository.existsById(id)) {
            throw new IllegalArgumentException("Vista no encontrada: " + id);
        }
        rolePermissionRepository.findByRoleAndViewPermissionCode("", "")
                .stream().filter(rp -> rp.getViewPermission().getId().equals(id))
                .forEach(rp -> rolePermissionRepository.delete(rp));
        viewPermissionRepository.deleteById(id);
    }

    public boolean canRoleAccess(String role, String viewCode) {
        return rolePermissionRepository
                .existsByRoleAndViewPermissionCodeAndCanAccessTrue(role, viewCode);
    }

    public Set<String> getAccessibleViews(String role) {
        return rolePermissionRepository.findByRoleAndCanAccessTrue(role)
                .stream()
                .map(rp -> rp.getViewPermission().getCode())
                .collect(Collectors.toSet());
    }
}
