package com.aircargo.authservice.service;

import com.aircargo.authservice.dto.AppUserDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserService {
    List<AppUserDTO> getAll(UUID airlineId);
    Optional<AppUserDTO> getById(UUID id);
    AppUserDTO create(AppUserDTO dto);
    Optional<AppUserDTO> update(UUID id, AppUserDTO dto);
    boolean delete(UUID id);
    void resetPassword(UUID id);
}
