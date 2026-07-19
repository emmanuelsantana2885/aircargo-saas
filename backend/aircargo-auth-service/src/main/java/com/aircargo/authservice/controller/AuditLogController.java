package com.aircargo.authservice.controller;

import com.aircargo.authservice.entity.AuditLog;
import com.aircargo.authservice.repository.AuditLogRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogRepository repository;

    public AuditLogController(AuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<AuditLog> getAll(@RequestParam(required = false) UUID userId) {
        if (userId != null) {
            return repository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return repository.findAllByOrderByCreatedAtDesc();
    }
}
