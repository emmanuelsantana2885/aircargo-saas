package com.aircargo.repository;

import com.aircargo.entity.ReceiptPiece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReceiptPieceRepository extends JpaRepository<ReceiptPiece, UUID> {
    
    /**
     * Recupera el desglose de todas las piezas asociadas a un recibo de bodega específico.
     */
    List<ReceiptPiece> findByReceiptId(UUID id);
}