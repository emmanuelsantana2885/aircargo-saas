package com.aircargo.dto;

import com.aircargo.entity.UserRole;
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
public class AppUserDTO {

    private UUID id;
    private UUID airlineId;
    private UUID supabaseUid;
    private String email;
    private String fullName;
    private UserRole role;
    private Boolean isActive;
    private OffsetDateTime lastLogin;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
