package com.aircargo.controller;


import com.aircargo.entity.Booking;
import com.aircargo.repository.BookingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;

    public BookingController(BookingRepository bookingRepository){
        this.bookingRepository = bookingRepository;
    }


    //GET /api/bookings?flightId=xxx
    @GetMapping
    public List<Booking> getAll(
            @RequestParam(required = false) UUID flightId,
            @RequestParam(required = false) UUID airlineId){

        if(flightId != null){
            return bookingRepository.findByFlightId(flightId);
        }
        if(airlineId != null){
            return bookingRepository.findByAirlineId(airlineId);
        }
        return bookingRepository.findAll();
    }

    // GET /api/bookings/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getById(@PathVariable UUID id){
        return bookingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
