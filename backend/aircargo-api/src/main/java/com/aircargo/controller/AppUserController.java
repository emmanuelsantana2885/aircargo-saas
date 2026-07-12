package com.aircargo.controller;

import com.aircargo.auth.UserPrincipal;
import com.aircargo.dto.AppUserDTO;
import com.aircargo.dto.ConnectedUserDTO;
import com.aircargo.service.ActiveSessionTracker;
import com.aircargo.service.AppUserService;
import com.aircargo.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService appUserService;
    private final AuditService auditService;
    private final ActiveSessionTracker sessionTracker;

    public AppUserController(AppUserService appUserService, AuditService auditService,
                             ActiveSessionTracker sessionTracker) {
        this.appUserService = appUserService;
        this.auditService = auditService;
        this.sessionTracker = sessionTracker;
    }

    @GetMapping
    public List<AppUserDTO> getAll(@RequestParam(required = false) UUID airlineId) {
        return appUserService.getAll(airlineId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUserDTO> getById(@PathVariable UUID id) {
        return appUserService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AppUserDTO> create(@Valid @RequestBody AppUserDTO dto,
                                              @AuthenticationPrincipal UserPrincipal principal,
                                              HttpServletRequest request) {
        if (dto.getSiteIds() == null || dto.getSiteIds().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        AppUserDTO created = appUserService.create(dto);
        auditService.logUserCreate(
                principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                created.getId(), created.getEmail(), request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppUserDTO> update(@PathVariable UUID id, @Valid @RequestBody AppUserDTO dto,
                                              @AuthenticationPrincipal UserPrincipal principal,
                                              HttpServletRequest request) {
        return appUserService.update(id, dto)
                .map(updated -> {
                    auditService.logUserUpdate(
                            principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                            id, "{\"role\":\"" + dto.getRole() + "\",\"isActive\":" + dto.getIsActive() + "}",
                            request.getRemoteAddr());
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserPrincipal principal,
                                        HttpServletRequest request) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        AppUserDTO existing = appUserService.getById(id).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();
        if (existing.getId().equals(principal.getUserIdAsUuid())) {
            return ResponseEntity.badRequest().build();
        }
        boolean removed = appUserService.delete(id);
        if (!removed) return ResponseEntity.notFound().build();
        auditService.logUserDelete(
                principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                id, existing.getEmail(), request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable UUID id,
                                            @AuthenticationPrincipal UserPrincipal principal,
                                            HttpServletRequest request) {
        AppUserDTO user = appUserService.getById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        appUserService.resetPassword(id);
        auditService.logPasswordReset(
                principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                id, user.getEmail(), request.getRemoteAddr());
        return ResponseEntity.ok(Map.of("message", "Contraseña restablecida"));
    }

    @GetMapping("/connected")
    public List<ConnectedUserDTO> getConnected() {
        return sessionTracker.getConnectedUsers();
    }
}
