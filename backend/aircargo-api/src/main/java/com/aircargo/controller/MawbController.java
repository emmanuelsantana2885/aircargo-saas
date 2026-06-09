package com.aircargo.controller;

import com.aircargo.entity.Mawb;
import com.aircargo.entity.MawbStatus;
import com.aircargo.repository.MawbRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/mawbs")
public class MawbController {

    private final MawbRepository mawbRepository;

    public MawbController(MawbRepository mawbRepository){
        this.mawbRepository = mawbRepository;
    }

    // GET /api/mawbs?airlineId=xxx
    @GetMapping
    public List<Mawb> getAll(
            @RequestParam(required = false) UUID airlineId,
            @RequestParam(required = false) UUID flightId,
            @RequestParam(required = false) MawbStatus status){

        if(flightId !=null){
            return mawbRepository.findByFlightId(flightId);
        }
        if(airlineId != null && status != null){
            return mawbRepository.findByAirlineIdAndStatus(airlineId, status);
        }
        if(airlineId != null){
            return mawbRepository.findByAirlineId(airlineId);
        }
        return mawbRepository.findAll();
    }

    // GET /api/mawbs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Mawb> getById(@PathVariable UUID id){
        return mawbRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/mawbs/awb/{awbNumber}?airlineId=xxx
    @GetMapping("/awb/{awbNumber}")
    public ResponseEntity<Mawb> getByAwbNumber(
         @PathVariable String awbNumber,
         @RequestParam UUID airlineId) {
        return mawbRepository.findByAirlineIdAndAwbNumber(airlineId, awbNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/mawbs/{id}/status
    @PutMapping("/{id}/status")
    public ResponseEntity<Mawb> updateStatus(
            @PathVariable UUID id,
            @RequestParam MawbStatus status){
        return mawbRepository.findById(id)
                .map(mawb -> {
                    mawb.setStatus(status);
                    return ResponseEntity.ok(mawbRepository.save(mawb));
                })
                .orElse(ResponseEntity.notFound().build());
    }


}
