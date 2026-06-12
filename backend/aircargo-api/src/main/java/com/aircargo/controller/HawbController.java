package com.aircargo.controller;

import com.aircargo.entity.Hawb;
import com.aircargo.repository.HawbRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hawbs")
public class HawbController {

    private final HawbRepository hawbRepository;

    public HawbController(HawbRepository hawbRepository) {
        this.hawbRepository = hawbRepository;
    }

    @GetMapping
    public List<Hawb> getAll(@RequestParam(required = false) UUID airlineId,
                             @RequestParam(required = false) UUID mawbId) {
        if (mawbId != null) return hawbRepository.findByMawbId(mawbId);
        if (airlineId != null) return hawbRepository.findByAirlineId(airlineId);
        return hawbRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hawb> getById(@PathVariable UUID id) {
        return hawbRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Hawb> create(@RequestBody Hawb hawb) {
        Hawb saved = hawbRepository.save(hawb);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Hawb> update(@PathVariable UUID id, @RequestBody Hawb hawb) {
        return hawbRepository.findById(id)
                .map(existing -> {
                    hawb.setId(existing.getId());
                    Hawb updated = hawbRepository.save(hawb);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!hawbRepository.existsById(id)) return ResponseEntity.notFound().build();
        hawbRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
