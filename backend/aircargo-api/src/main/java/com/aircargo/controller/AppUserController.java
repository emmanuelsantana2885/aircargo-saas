package com.aircargo.controller;

import com.aircargo.dto.AppUserDTO;
import com.aircargo.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
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
    public ResponseEntity<AppUserDTO> create(@Valid @RequestBody AppUserDTO dto) {
        AppUserDTO created = appUserService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppUserDTO> update(@PathVariable UUID id, @Valid @RequestBody AppUserDTO dto) {
        return appUserService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean removed = appUserService.delete(id);
        if (!removed) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
