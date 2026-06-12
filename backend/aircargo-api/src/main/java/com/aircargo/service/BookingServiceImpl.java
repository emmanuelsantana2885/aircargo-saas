package com.aircargo.service;

import com.aircargo.dto.BookingDTO;
import com.aircargo.entity.Booking;
import com.aircargo.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;

    public BookingServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<BookingDTO> getAll(UUID airlineId, UUID flightId) {
        List<Booking> results;
        if (flightId != null) results = bookingRepository.findByFlightId(flightId);
        else if (airlineId != null) results = bookingRepository.findByAirlineId(airlineId);
        else results = bookingRepository.findAll();
        return results.stream().map(BookingDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public Optional<BookingDTO> getById(UUID id) {
        return bookingRepository.findById(id).map(BookingDTO::fromEntity);
    }

    @Override
    public BookingDTO create(BookingDTO dto) {
        Booking entity = BookingDTO.toEntity(dto);
        Booking saved = bookingRepository.save(entity);
        return BookingDTO.fromEntity(saved);
    }

    @Override
    public Optional<BookingDTO> update(UUID id, BookingDTO dto) {
        return bookingRepository.findById(id)
                .map(existing -> {
                    Booking updated = BookingDTO.toEntity(dto);
                    updated.setId(existing.getId());
                    return bookingRepository.save(updated);
                })
                .map(BookingDTO::fromEntity);
    }

    @Override
    public boolean delete(UUID id) {
        if (!bookingRepository.existsById(id)) return false;
        bookingRepository.deleteById(id);
        return true;
    }

    @Override
    public Optional<BookingDTO> updateAwb(UUID id, String awbNumber) {
        return bookingRepository.findById(id)
                .map(existing -> {
                    existing.setAwbNumber(awbNumber);
                    return bookingRepository.save(existing);
                })
                .map(BookingDTO::fromEntity);
    }
}
