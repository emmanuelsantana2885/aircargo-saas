package com.aircargo.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank @Size(min = 6, max = 100) String newPassword,
    String currentPassword,
    @NotBlank String totpCode
) {}
