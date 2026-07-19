package com.aircargo.flightservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    public void log(UUID userId, String email, String fullName, String action,
                    String entityType, String entityId, String details, String ipAddress) {
        log.info("AUDIT [{}] {}:{} {} {} {}", action, email, userId, entityType, entityId, ipAddress);
    }

    public void logLogin(UUID userId, String email, String fullName, String ipAddress) {
        log(userId, email, fullName, "LOGIN", null, null, null, ipAddress);
    }

    public void logUserCreate(UUID userId, String email, String fullName,
                              UUID targetUserId, String targetEmail, String ipAddress) {
        log(userId, email, fullName, "CREATE", "USER", targetUserId.toString(),
                "{\"email\":\"" + targetEmail + "\"}", ipAddress);
    }

    public void logUserUpdate(UUID userId, String email, String fullName,
                              UUID targetUserId, String changes, String ipAddress) {
        log(userId, email, fullName, "UPDATE", "USER", targetUserId.toString(),
                changes, ipAddress);
    }

    public void logUserDelete(UUID userId, String email, String fullName,
                              UUID targetUserId, String targetEmail, String ipAddress) {
        log(userId, email, fullName, "DELETE", "USER", targetUserId.toString(),
                "{\"email\":\"" + targetEmail + "\"}", ipAddress);
    }

    public void logPasswordReset(UUID userId, String email, String fullName,
                                 UUID targetUserId, String targetEmail, String ipAddress) {
        log(userId, email, fullName, "PASSWORD_RESET", "USER", targetUserId.toString(),
                "{\"email\":\"" + targetEmail + "\"}", ipAddress);
    }
}
