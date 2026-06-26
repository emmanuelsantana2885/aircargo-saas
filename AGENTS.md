# Aircargo — agent notes

## Structure

Monorepo with three directories:

| Dir | Stack | Entrypoint / notes |
|-----|-------|-------------------|
| `frontend/` | Vue 3 + Vite + Pinia + Vue Router + Tailwind + TS | Vite dev on port 5173, proxy `/api` → `localhost:9091` |
| `backend/aircargo-api/` | Spring Boot 3.3 + Java 21 + Maven + JPA + Flyway + PostgreSQL | `com.aircargo.AircargoApiApplication`, port 9091 |
| `database/migrations/` | PostgreSQL Flyway migrations | Root copy — see "Migrations" below |

## Commands

```sh
# Frontend
npm install          # in frontend/
npm run dev          # Vite dev server (port 5173)
npm run type-check   # vue-tsc --build
npm run build        # type-check + vite build (via npm-run-all2)

# Backend
mvn test                               # unit + integration tests (H2, Flyway disabled)
mvn spring-boot:run                    # starts on port 9091 (needs Postgres)
mvn clean compile spring-boot:run      # full rebuild + run

# Database
docker compose up -d                   # PostgreSQL 16 alpine on :5432
```

## Hard-coded constants

- UPS airline UUID `00000000-0000-0000-0000-000000000001` is hard-coded in every frontend API file and seeded in `V1__init.sql` (backend copy only). Never change it without updating both sides.
- Vite proxy in `frontend/vite.config.ts` assumes backend is on `localhost:9091`.

## Migrations

Flyway migrations live in **two places** with **different content**:
- **Source of truth (Flyway picks up):** `backend/aircargo-api/src/main/resources/db/migration/`
- **Root copy:** `database/migrations/` — differs from the backend copy (has full Postgres schema with functions, triggers, permissions; backend copy is Hibernate-compressed)

The backend copy also seeds the UPS airline row; the root copy does not. Keep both in sync.

## Backend quirks

- **No Spring Security** — `spring-boot-starter-security` not in `pom.xml`. Auth (Supabase?) is not wired in.
- **Test pattern:** All 50 tests pass (`mvn test`): service-layer tests use Mockito (no Spring context); integration tests use `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional` with H2 in PostgreSQL mode. Flyway disabled in tests.
- **Apache POI** for Excel ramp manifest parsing (`LoadPlanningImportService`).
- **Fixes applied:**
  - Deleted duplicate `com.aircargo.WebConfig` (root package) — kept `com.aircargo.config.WebConfig` (ports 5173 + 5174) since both beans named `webConfig` caused `ConflictingBeanDefinitionException`.
  - Removed `@CrossOrigin(origins = "*")` from 5 controllers (Hawb, Warehouse, Mawb, FlightManifest, LoadPlanning) — conflicted with `WebConfig.allowCredentials(true)` causing CORS rejection.
  - Added `= BigDecimal.ZERO` default to `Booking.reservedKg` and null guard in `BookingDTO.toEntity()` — field was `nullable = false` but had no default, causing 500 on booking create.

## Frontend quirks

- **Stale files (removed):** `src/stores/ulds.js` was deleted — superseded by `src/stores/ulds.ts`. `src/stores/counter.ts` is a Pinia template leftover. The API layer (`src/api/*.js`) is plain JS while stores are mixed JS/TS.
- **No frontend tests** exist (no vitest/jest config found).
- `README.md` is the default Vite template — ignore it.
- **Pre-existing type-check errors:** missing `env.d.ts` for `.vue` module declarations, `ulds.ts` strict mode issues. These do not block the Vite build.

## Recent session changes (June 2026)

| File | Change |
|------|--------|
| `frontend/src/views/WarehouseReceiptsView.vue` | Major redesign: Step 1 two-column layout (left: data fields, right: HAWB table + checkboxes group); shipper/consignee preloaded from MAWB, editable, sync'd to backend on blur; 5 checkboxes (Cash Only, Booked in ACOMS, Documents Provided, Export Customs Completed, Pre-built) grouped in bordered card; Step 2 professional dark-header table with bordered inputs, proper spacing; multi-HAWB pieces section with per-HAWB tables; Step 5 compact signatures (grid 2-col, smaller pads); added PDF + HTML download buttons for supporting evidence; added MAWB-level evidence manager modal (folder icon in status column); **NEW**: HAWB count input with `syncHawbCount()` to add/remove HAWB rows dynamically; fully editable HAWB table (hawbNumber, consignee, pieces, weightKg, destination); filename read from `Content-Disposition` header |
| `frontend/src/api/mawbs.js` | Added `update(mawbId, dto)`, `getSupportingDocs(id)`, `updateSupportingDocs(id, docs)`, `getSupportingDocsPdf(id)` |
| `frontend/src/api/receipts.js` | Added `getSupportingDocsJson(id)`, `getSupportingDocsHtml(id)`, `getSupportingDocsPdf(id)` |
| `backend/aircargo-api/src/main/java/com/aircargo/controller/MawbController.java` | Added `PUT /{mawbId}`, `GET /{mawbId}/supporting-docs`, `PUT /{mawbId}/supporting-docs`, `GET /{mawbId}/supporting-docs/pdf` — MAWB evidence CRUD + PDF generation |
| `backend/aircargo-api/src/main/java/com/aircargo/controller/WarehouseController.java` | Added `ReceiptPayload.supportingDocs` field; `GET /{receiptId}/supporting-docs` (JSON), `GET /{receiptId}/supporting-docs/html`, `GET /{receiptId}/supporting-docs/pdf`; updated `emit` to pass supportingDocs to service; fixed Excel filename to `RECIBO_DE_BODEGA_AWB {mawbNumber}.xlsx` |
| `backend/aircargo-api/src/main/java/com/aircargo/entity/WarehouseReceipt.java` | Added `supportingDocs` text column (default `"[]"`) |
| `backend/aircargo-api/src/main/java/com/aircargo/entity/Mawb.java` | Added `supportingDocs` text column (default `"[]"`) |
| `backend/aircargo-api/src/main/java/com/aircargo/service/WarehouseService.java` | Overloaded `processWarehouseReceipt` to accept `List<Map<String,String>> supportingDocs`; stores as JSON; added `generateSupportingDocsHtml()` + `generateSupportingDocsPdf()` |
| `backend/aircargo-api/src/main/java/com/aircargo/service/PdfGenerationService.java` | New — uses openhtmltopdf to convert HTML+CSS (with embedded base64 images) to PDF |
| `backend/aircargo-api/src/main/java/com/aircargo/repository/WarehouseReceiptRepository.java` | Added `findByMawbId(UUID)` |
| `backend/aircargo-api/pom.xml` | Added `openhtmltopdf-pdfbox:1.0.10` dependency |
| `backend/aircargo-api/src/main/resources/db/migration/V6__add_supporting_docs_to_receipt.sql` | New — `ALTER TABLE warehouse_receipt ADD COLUMN supporting_docs text` |
| `backend/aircargo-api/src/main/resources/db/migration/V7__add_supporting_docs_to_mawb.sql` | New — `ALTER TABLE mawb ADD COLUMN supporting_docs text` |
| `database/migrations/V6__add_supporting_docs_to_receipt.sql` | Synced from backend |
| `database/migrations/V7__add_supporting_docs_to_mawb.sql` | Synced from backend |
| `backend/aircargo-api/src/main/java/com/aircargo/service/ReceiptExportService.java` | **Rewritten** — matches reference template `RECIBO_DE_BODEGA_AWB.xlsx`: same merged cells layout (B-C labels, D-E values, F-G checkboxes), same column widths (A-L), 25 data rows, `Dim Weight` = vol/366 (KGS), `Dim LBS` = vol/194; Tahoma font |
| `frontend/src/views/WarehouseReceiptsView.vue` | **HAWB section redesigned**: bigger (text-[10px] inputs), dark green border-2 border-emerald-800, emerald header bg, emerald-800 accent checkboxes group with shadow |
| `frontend/src/views/BookingsView.vue` | **MAWB status column** added (col-span-2): shows real MAWB status per booking with colored square indicator; stats now use MAWB status for Received %; flujograma reduced to col-span-2 with `h-1.5 w-1.5` squares (no rounded-full) |
| `backend/aircargo-api/src/main/java/com/aircargo/service/WarehouseService.java` | **PDF evidence rewritten**: replaced CSS grid + object-fit with table layout; max image size reduced to 150KB; removed unsupported CSS properties for openhtmltopdf compatibility; added `xmlEscape()` helper |
| `backend/aircargo-api/src/main/java/com/aircargo/controller/MawbController.java` | **MAWB PDF evidence rewritten**: same table layout as receipt PDF; added `xmlEscape()` helper; replaced flex/grid with table; reduced image max to 150KB; replaced `→` unicode with `&#8594;` |
| `backend/aircargo-api/src/main/java/com/aircargo/controller/MawbController.java` | Fixed XML/HTML entities for openhtmltopdf: self-closing `<meta/>`, `&mdash;` → `&#8212;`; added `xmlEscape()` helper for all dynamic content to prevent SAXParseException on `&` in user data |
| `backend/aircargo-api/src/main/java/com/aircargo/service/WarehouseService.java` | Fixed XML/HTML entities for openhtmltopdf: self-closing `<meta/>`, `&mdash;` → `&#8212;`, `&middot;` → `&#183;`; added `xmlEscape()` helper for all dynamic content |
| `backend/aircargo-api/src/main/java/com/aircargo/service/PdfGenerationService.java` | **Rewritten** — decodes base64 data URIs in HTML to temp files with `file:///` paths for reliable PDF image rendering; auto-cleanup in `finally` block |
| `backend/aircargo-api/src/main/java/com/aircargo/service/WarehouseService.java` | Simplified `generateSupportingDocsPdf()` — removed temp file handling (now delegated to `PdfGenerationService`) |
| `frontend/src/views/WarehouseReceiptsView.vue` | **HAWB inputs enlarged**: `text-[10px]` → `text-xs`, wider columns (w-20→w-24, w-14→w-16), bigger padding; **Labels darkened**: all form `<label>` elements `text-slate-400` → `text-slate-700`; secondary text darkened to `text-slate-500`; step headers darkened; evidence upload/camera labels darkened; pieza count text darkened |
| `backend/aircargo-api/src/main/java/com/aircargo/service/ReceiptExportService.java` | **Rewritten** — matches reference template `RECIBO_DE_BODEGA_AWB.xlsx`: same merged cells layout (B-C labels, D-E values, F-G checkboxes), same column widths (A-L), 25 data rows, `Dim Weight` = vol/366 (KGS), `Dim LBS` = vol/194; Tahoma font; **Added Evidencias sheet**: signatures (dock/deliveredBy/broker) + supporting docs embedded as images; **Sheet protection**: main sheet protected with password `aircargo2024`, value cells unlocked; uses `ObjectMapper` to parse `supportingDocs` JSON |
| `frontend/src/views/MawbsView.vue` | **Complete redesign**: 6 frozen columns (MAWB/Shipper/Pzas/Kg/Dest/Pcs Disp) with sticky positioning; date headers enlarged `text-[6px]` → `text-[11px]` bold; all table content `text-[10px]` → `text-xs`; piece cells use **chalkboard texture** (dark green gradient + line noise + `radial-gradient`), white chalk-like text with `text-shadow` glow; **SVG mini arc** showing piece distribution per flight; **pop-in animation** on cell entrance with staggered delays; **column hover glow** via `ring-1 ring-inset ring-white/20`; **minimap widget** (teleported) with viewport overlay and `@scroll` tracking; MAWB click navigates to `/receipts?mawbId=xxx` using router push + query params; `WarehouseReceiptsView` reads `route.query.mawbId` on mount to auto-expand the target MAWB; `float button` toggle for minimap visibility |
| `frontend/src/views/MawbsView.vue` | **June 22 updates**: Removed St column; added Pcs Dispatched column (right-aligned, amber ⚠ when > received); Shipper + Consignee stacked vertically in same column; MAWB cell gets status-based background + left border color (gray=BOOKED, blue=RECEIVED, amber=MANIFESTED, emerald=DEPARTED) with Spanish status text subtitle; `totalPieces` capped at warehouse received quantity (`Math.min(dispatched, received)`); per-ULD breakdown in tooltip + "N ULDs" subtitle when >1 ULD per flight; stats bar includes Despachadas count with exceso indicator |
| `frontend/src/views/UldsView.vue` | **Autocomplete MAWB**: replaced `<select>` with text input + filtered suggestions dropdown; keyboard navigation (arrows + enter + escape); MAWB search filters by awbNumber/shipperName; pending pieces logic (receipt > booking) |
| `frontend/src/views/DashboardView.vue` | **Fixed tare formulas**: `netLbs` = gross - realTare (was gross - bellyTare); `payloadLbs` = netLbs (was netLbs + 5 dummy) |
| `frontend/src/components/layout/Sidebar.vue` | **Icon swap**: `IconPackage` → `IconPackageExport` for ULDs; all nav icons increased 16→18 (+10%) |
| `backend/aircargo-api/src/main/java/com/aircargo/service/WarehouseService.java` | **Piece accumulation fix + Booking MAWB sync**: `processWarehouseReceipt()` now deletes existing receipt+pieces for a MAWB before re-emitting; after saving, updates linked Booking's `awbNumber` from MAWB |
| `backend/aircargo-api/src/main/java/com/aircargo/repository/BookingRepository.java` | Added `findByMawbId(UUID)` for booking lookup by MAWB |
| `backend/aircargo-api/src/main/java/com/aircargo/service/ReceiptExportService.java` | Fixed `Workbook` → `XSSFWorkbook` cast error in `createEvidenceSheet` call |
| All 10 `src/views/*View.vue` + `WarehouseForm.vue` | **Font-size standardisation**: table data → `text-[10px]`, table headers → `text-[11px]`, titles → `text-[12px]` |
| `frontend/src/views/WarehouseReceiptsView.vue` | **Pieces loaded from existing receipt**: when editing a receipt, existing pieces are now loaded via `receiptsApi.getPieces()` and displayed in the form; `editReceipt` rewritten to send pieces+evidence via POST `/emit` (backend replaces old receipt entirely); signatures (dock/deliveredBy/broker) from existing receipt shown as images in MAWB evidence section |
| `frontend/src/api/receipts.js` | Added `getPieces(id)` — calls `GET /warehouse/receipts/{id}/pieces` |

## Build

```sh
npm run build         # Vite build succeeds (type-check has pre-existing errors)
npm run type-check    # Fails on pre-existing .vue module declarations + ulds.ts strict mode
```

## Import paths

Frontend uses `@/` → `./src/` (configured in `vite.config.ts` and `tsconfig.app.json`).
