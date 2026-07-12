package com.aircargo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectedUserDTO {
    private UUID userId;
    private String email;
    private String fullName;
    private String role;
    private OffsetDateTime lastHeartbeat;
    private OffsetDateTime lastLogin;
}
