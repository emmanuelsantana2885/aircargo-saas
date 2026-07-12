package com.aircargo.service;

import com.aircargo.dto.ConnectedUserDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActiveSessionTracker {

    private static final long SESSION_TIMEOUT_SECONDS = 300;
    private final Map<UUID, HeartbeatEntry> sessions = new ConcurrentHashMap<>();

    public void recordHeartbeat(UUID userId, String email, String fullName, String role, OffsetDateTime lastLogin) {
        sessions.put(userId, new HeartbeatEntry(userId, email, fullName, role, OffsetDateTime.now(), lastLogin));
    }

    public void removeSession(UUID userId) {
        sessions.remove(userId);
    }

    public List<ConnectedUserDTO> getConnectedUsers() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusSeconds(SESSION_TIMEOUT_SECONDS);
        return sessions.values().stream()
                .filter(e -> e.lastHeartbeat.isAfter(cutoff))
                .map(e -> ConnectedUserDTO.builder()
                        .userId(e.userId)
                        .email(e.email)
                        .fullName(e.fullName)
                        .role(e.role)
                        .lastHeartbeat(e.lastHeartbeat)
                        .lastLogin(e.lastLogin)
                        .build())
                .toList();
    }

    @Scheduled(fixedRate = 60_000)
    public void purgeStaleSessions() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusSeconds(SESSION_TIMEOUT_SECONDS);
        sessions.values().removeIf(e -> e.lastHeartbeat.isBefore(cutoff));
    }

    private record HeartbeatEntry(UUID userId, String email, String fullName, String role,
                                  OffsetDateTime lastHeartbeat, OffsetDateTime lastLogin) {}
}
