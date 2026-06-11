package com.aircargo.repository;

import com.aircargo.entity.WarehouseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseReceiptRepository extends JpaRepository<WarehouseReceipt, UUID> {

    List<WarehouseReceipt> findByAirlineId(UUID airlineId);

    Optional<WarehouseReceipt> findByMawbId(UUID mawbId);
}
