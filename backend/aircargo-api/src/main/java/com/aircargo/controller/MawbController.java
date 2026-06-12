package com.aircargo.controller;

import com.aircargo.dto.MawbDTO;
import com.aircargo.entity.MawbStatus;
import com.aircargo.service.MawbService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/mawbs")
public class MawbController {

    private final MawbService mawbService;

    public MawbController(MawbService mawbService){
        this.mawbService = mawbService;
    }

    // GET /api/mawbs?airlineId=xxx
    @GetMapping
    public List<MawbDTO> getAll(
            @RequestParam(required = false) UUID airlineId,
            @RequestParam(required = false) UUID flightId,
            @RequestParam(required = false) MawbStatus status){
        return mawbService.getAll(airlineId, flightId, status);
    }

    // GET /api/mawbs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MawbDTO> getById(@PathVariable UUID id){
        return mawbService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/mawbs/awb/{awbNumber}?airlineId=xxx
    @GetMapping("/awb/{awbNumber}")
    public ResponseEntity<MawbDTO> getByAwbNumber(
         @PathVariable String awbNumber,
         @RequestParam UUID airlineId) {
        return mawbService.getByAirlineIdAndAwbNumber(airlineId, awbNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/mawbs/{id}/status
    @PutMapping("/{id}/status")
    public ResponseEntity<MawbDTO> updateStatus(
            @PathVariable UUID id,
            @RequestParam MawbStatus status){
        return mawbService.updateStatus(id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
