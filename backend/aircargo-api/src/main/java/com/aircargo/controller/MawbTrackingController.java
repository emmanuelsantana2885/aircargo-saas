package com.aircargo.controller;

import com.aircargo.service.MawbTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tracking")
public class MawbTrackingController {

    private final MawbTrackingService trackingService;

    public MawbTrackingController(MawbTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping("/mawb/{mawbId}")
    public ResponseEntity<List<Map<String, Object>>> getMawbTimeline(@PathVariable UUID mawbId) {
        return ResponseEntity.ok(trackingService.getMawbTimeline(mawbId));
    }

    @GetMapping("/mawbs")
    public ResponseEntity<List<Map<String, Object>>> getAllMawbTracking() {
        return ResponseEntity.ok(trackingService.getAllMawbTrackingSummary());
    }
}
