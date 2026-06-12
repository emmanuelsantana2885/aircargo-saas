package com.aircargo.service;

import com.aircargo.dto.WarehouseReceiptDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseReceiptService {
    List<WarehouseReceiptDTO> getAll(UUID airlineId);
    Optional<WarehouseReceiptDTO> getById(UUID id);
    WarehouseReceiptDTO create(WarehouseReceiptDTO dto);
    Optional<WarehouseReceiptDTO> update(UUID id, WarehouseReceiptDTO dto);
    boolean delete(UUID id);
}
