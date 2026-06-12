package com.aircargo.service;

import com.aircargo.dto.AppUserDTO;
import com.aircargo.entity.AppUser;
import com.aircargo.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppUserServiceImpl implements AppUserService{

    private final AppUserRepository repository;

    public AppUserServiceImpl(AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<AppUserDTO> getAll(UUID airlineId) {
        List<AppUser> results = airlineId != null ? repository.findByAirlineId(airlineId) : repository.findAll();
        return results.stream().map(AppUserDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public Optional<AppUserDTO> getById(UUID id) {
        return repository.findById(id).map(AppUserDTO::fromEntity);
    }

    @Override
    public AppUserDTO create(AppUserDTO dto) {
        AppUser e = AppUserDTO.toEntity(dto);
        AppUser saved = repository.save(e);
        return AppUserDTO.fromEntity(saved);
    }

    @Override
    public Optional<AppUserDTO> update(UUID id, AppUserDTO dto) {
        return repository.findById(id)
                .map(existing -> {
                    AppUser updated = AppUserDTO.toEntity(dto);
                    updated.setId(existing.getId());
                    return repository.save(updated);
                })
                .map(AppUserDTO::fromEntity);
    }

    @Override
    public boolean delete(UUID id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
