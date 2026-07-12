# Plan: Receipt Export Improvements

## Goal
Align backend Excel export with Python `Receipts.py` script + add async PDF export.

## Constraints
- No airline logo files (skip logo embedding)
- No LibreOffice — PDF via existing openhtmltopdf
- Async PDF export

---

## Changes

### 1. Add ZXing Dependency — `pom.xml`
Add `com.google.zxing:core:3.5.3` + `com.google.zxing:javase:3.5.3` for QR code and Code128 barcode generation.

### 2. New Service — `ReceiptGraphicsService.java`
**Path:** `backend/aircargo-api/src/main/java/com/aircargo/service/ReceiptGraphicsService.java`

| Method | Returns | Description |
|--------|---------|-------------|
| `generateQrCode(String mawb)` | `byte[]` (PNG) | QR code encoding MAWB number |
| `generateBarcode(String mawb)` | `byte[]` (PNG) | Code128 barcode of MAWB |

Uses ZXing's `QRCodeWriter` + `BarcodeFormat.CODE_128`.

### 3. Dynamic Airline Title — `ReceiptExportService.java`
Add static map matching Python script:
```java
Map<String, String> AIRLINE_TITLES = Map.of(
    "UPS", "UPS Air Cargo Dock Receipt",
    "AMERIJET", "Amerijet Air Cargo Dock Receipt",
    "SKYHIGH", "Sky High Aviation Dock Receipt",
    "DHL", "DHL Aviation Dock Receipt",
    "ATLAS", "Atlas Air Dock Receipt"
);
```
Fallback: `airlineName + " Dock Receipt"` (matches Python's default behavior).

Modify `renderTitle()` to use `receipt.getAirline().getCode()` to look up the title.

### 4. Embed QR + Barcode in Excel — `ReceiptExportService.java`
After building the workbook with `SXSSFWorkbook`, convert to `XSSFWorkbook` (already done for evidence sheet). At that point, inject:

| Image | Excel Position | Pixel Size | Anchor |
|-------|---------------|------------|--------|
| QR Code | J3 (col 9, row 2) | 120×120 | `XSSFClientAnchor` |
| Barcode | H11 (col 7, row 10) | 300×70 | `XSSFClientAnchor` |

Uses `XSSFWorkbook.addPicture()` + `createDrawingPatriarch().createPicture()` — same technique as `EvidenceSheetRenderer.embedBase64Image()`.

### 5. Signature Image in Excel — `ReceiptExportService.java`
Modify `renderSignaturesSection()`: if `receipt.getDockSignature()` is non-empty (base64 data URI), decode and embed as image at the signature row (matching Python's `B56` position, 220×80 px).

### 6. Async PDF Export — `ReceiptExportService.java` + `WarehouseController.java`
**New method** `exportReceiptPdfAsync(UUID receiptId)` → `CompletableFuture<ByteArrayInputStream>`:
1. Builds an HTML string representing the receipt (same data as Excel: title, header fields, pieces table, weights, signatures)
2. Calls existing `PdfGenerationService.generatePdf(html)` to convert HTML→PDF via openhtmltopdf
3. Returns the PDF bytes wrapped in `ByteArrayInputStream`

**New endpoint** `GET /api/warehouse/receipts/{receiptId}/export/pdf`:
- Returns `ResponseEntity<CompletableFuture<Resource>>` (async)
- Content-Type: `application/pdf`
- Filename from Content-Disposition header: `RECIBO_DE_BODEGA_AWB {mawbNumber}.pdf`

### 7. Frontend PDF Button — `WarehouseReceiptsView.vue` + `receipts.js`
**`receipts.js`:** Add `exportPdf(id)` calling `GET /warehouse/receipts/{id}/export/pdf` with `responseType: 'blob'`.

**`WarehouseReceiptsView.vue`:** Add PDF download button next to existing Excel button in:
- Receipt form action bar
- MAWB row action buttons

---

## Files Modified

| # | File | Action |
|---|------|--------|
| 1 | `backend/aircargo-api/pom.xml` | Add ZXing deps |
| 2 | `backend/.../service/ReceiptGraphicsService.java` | **NEW** |
| 3 | `backend/.../service/ReceiptExportService.java` | Dynamic title, QR, barcode, signature image, PDF export |
| 4 | `backend/.../controller/WarehouseController.java` | New `/export/pdf` endpoint |
| 5 | `frontend/src/api/receipts.js` | Add `exportPdf()` |
| 6 | `frontend/src/views/WarehouseReceiptsView.vue` | PDF download button |

## Verification
1. `mvn test` — ensure no regressions
2. `npm run type-check` — ensure frontend compiles
3. Manual: Create a receipt with QR/barcode visible in Excel export
4. Manual: Download PDF and verify layout matches receipt data
