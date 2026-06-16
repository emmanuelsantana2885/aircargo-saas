package com.aircargo.repository;

import com.aircargo.entity.WarehouseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WarehouseReceiptRepository extends JpaRepository<WarehouseReceipt, UUID> {
    
    /**
     * Recupera los recibos de bodega asociados a una aerolínea (Aislamiento Multi-tenant).
     */
    List<WarehouseReceipt> findByAirlineId(UUID airlineId);
}
