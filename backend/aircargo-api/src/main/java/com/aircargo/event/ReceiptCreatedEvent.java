package com.aircargo.event;

import com.aircargo.entity.WarehouseReceipt;

import java.util.UUID;

public record ReceiptCreatedEvent(UUID receiptId, WarehouseReceipt receipt) {}