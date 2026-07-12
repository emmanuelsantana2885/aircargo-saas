package com.aircargo.repository;

import com.aircargo.entity.WarehouseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseReceiptRepository extends JpaRepository<WarehouseReceipt, UUID> {
    
    /**
     * Recupera los recibos de bodega asociados a una aerolínea (Aislamiento Multi-tenant).
     */
    List<WarehouseReceipt> findByAirlineId(UUID airlineId);

    /**
     * Recupera todos los recibos de bodega asociados a una MAWB.
     */
    List<WarehouseReceipt> findByMawbId(UUID mawbId);

    /**
     * Busca un recibo de bodega para un MAWB y HAWB específicos.
     * Usado para detectar si ya existe un recibo para este HAWB y
     * actualizarlo en lugar de insertar uno nuevo.
     */
    Optional<WarehouseReceipt> findByMawbIdAndHawbId(UUID mawbId, UUID hawbId);

    List<WarehouseReceipt> findByMawbIdAndHawbIdIsNotNull(UUID mawbId);
}
