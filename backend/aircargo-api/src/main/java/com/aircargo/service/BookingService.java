package com.aircargo.service;

import com.aircargo.dto.BookingDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingService {
    List<BookingDTO> getAll(UUID airlineId, UUID flightId);
    Optional<BookingDTO> getById(UUID id);
    BookingDTO create(BookingDTO dto);
    Optional<BookingDTO> update(UUID id, BookingDTO dto);
    Optional<BookingDTO> updateAwb(UUID id, String awbNumber);
    boolean delete(UUID id);
}
