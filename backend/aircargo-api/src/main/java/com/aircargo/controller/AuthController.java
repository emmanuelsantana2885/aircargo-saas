package com.aircargo.controller;

import com.aircargo.auth.JwtUtil;
import com.aircargo.auth.UserPrincipal;
import com.aircargo.dto.LoginRequest;
import com.aircargo.dto.LoginResponse;
import com.aircargo.dto.SetPasswordRequest;
import com.aircargo.dto.SiteDTO;
import com.aircargo.entity.AppUser;
import com.aircargo.entity.UserRole;
import com.aircargo.repository.AppUserRepository;
import com.aircargo.repository.SiteRepository;
import com.aircargo.service.ActiveSessionTracker;
import com.aircargo.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final ActiveSessionTracker sessionTracker;
    private final SiteRepository siteRepository;

    public AuthController(AppUserRepository userRepository, JwtUtil jwtUtil,
                          AuditService auditService, ActiveSessionTracker sessionTracker,
                          SiteRepository siteRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.auditService = auditService;
        this.sessionTracker = sessionTracker;
        this.siteRepository = siteRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest servletRequest) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String passwordHash = user.getPasswordHash();
        boolean hasPasswordSet = passwordHash != null && !passwordHash.isBlank();

        if (hasPasswordSet) {
            if (request.password() == null || request.password().isBlank()) {
                return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).build();
            }
            if (!passwordEncoder.matches(request.password(), passwordHash)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        user.setLastLogin(OffsetDateTime.now());
        userRepository.save(user);

        String airlineIdStr = user.getAirline() != null && user.getAirline().getId() != null
                ? user.getAirline().getId().toString() : "";

        String token = jwtUtil.generateToken(
                user.getId().toString(),
                user.getRole().name(),
                airlineIdStr,
                user.getEmail(),
                user.getFullName()
        );

        auditService.logLogin(user.getId(), user.getEmail(), user.getFullName(), servletRequest.getRemoteAddr());

        sessionTracker.recordHeartbeat(user.getId(), user.getEmail(), user.getFullName(),
                user.getRole().name(), user.getLastLogin());

        List<SiteDTO> userSites;
        if (user.getRole() == UserRole.SUPER_USER && user.getSites().isEmpty()) {
            userSites = siteRepository.findByIsActiveTrue().stream()
                    .map(SiteDTO::fromEntity)
                    .collect(Collectors.toList());
        } else {
            userSites = user.getSites().stream()
                    .map(SiteDTO::fromEntity)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getAirline() != null ? user.getAirline().getId() : null,
                hasPasswordSet,
                userSites
        ));
    }

    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(@Valid @RequestBody SetPasswordRequest request,
                                          HttpServletRequest servletRequest) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Usuario inactivo"));
        }

        String currentHash = user.getPasswordHash();
        if (currentHash != null && !currentHash.isBlank()) {
            if (request.currentPassword() == null || !passwordEncoder.matches(request.currentPassword(), currentHash)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Contraseña actual incorrecta"));
            }
        } else {
            if (request.currentPassword() != null && !request.currentPassword().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Este usuario no tiene contraseña previa. No envíe contraseña actual."));
            }
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        auditService.log(user.getId(), user.getEmail(), user.getFullName(), "PASSWORD_SET",
                "USER", user.getId().toString(), null, servletRequest.getRemoteAddr());

        String airlineIdStr = user.getAirline() != null && user.getAirline().getId() != null
                ? user.getAirline().getId().toString() : "";

        String token = jwtUtil.generateToken(
                user.getId().toString(),
                user.getRole().name(),
                airlineIdStr,
                user.getEmail(),
                user.getFullName()
        );
        return ResponseEntity.ok(Map.of(
                "message", "Contraseña establecida correctamente",
                "token", token
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AppUser user = userRepository.findById(principal.getUserIdAsUuid()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean hasPasswordSet = user.getPasswordHash() != null && !user.getPasswordHash().isBlank();
        List<SiteDTO> userSites;
        if (user.getRole() == UserRole.SUPER_USER && user.getSites().isEmpty()) {
            userSites = siteRepository.findByIsActiveTrue().stream()
                    .map(SiteDTO::fromEntity)
                    .collect(Collectors.toList());
        } else {
            userSites = user.getSites().stream()
                    .map(SiteDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(new LoginResponse(
                null, user.getId(), user.getEmail(), user.getFullName(),
                user.getRole(), user.getAirline() != null ? user.getAirline().getId() : null,
                hasPasswordSet, userSites
        ));
    }

    @GetMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null) {
            sessionTracker.recordHeartbeat(
                    principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    principal.role(), OffsetDateTime.now());
        }
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
