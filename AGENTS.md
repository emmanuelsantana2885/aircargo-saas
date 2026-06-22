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
| `frontend/src/stores/ulds.ts` | Added `airlineId` to `loadUldsForFlight` API call; `loadFlights` now hard-codes `UPS_AIRLINE_ID` |
| `frontend/src/stores/app.js` | Added `loadAllMawbs()` action (accumulates MAWBs across all flights); exported it |
| `frontend/src/views/BookingsView.vue` | Added `awbNumber`, `skids`, `units` to form and DTO; auto-creates MAWB via `store.createMawb()` after booking creation; removed `status` from DTO (not in entity); reloads both bookings + MAWBs after save; added XLSX import button + preview modal + bulk create (uses `xlsx` library to parse `/Downloads/Miercoles06may2026B767.xlsx` format); added flight selector dropdown in header; **now loads ALL bookings on mount (no flight filter)**; added `visibleBookings` computed + `flightNumber()` helper; shows all bookings when no flight selected |
| `frontend/src/views/UldsView.vue` | MAWB dropdown label shows commodity type: `{{ m.awbNumber }} — {{ m.shipperName }} [{{ m.commodityType }}]` |
| `frontend/src/views/WarehouseReceiptsView.vue` | Complete redesign: MAWB table with traffic-light status flow (4 dots); expandable receipt form in 5 steps (Header/Pieces/Remarks/Evidence/Signatures); auto-calculated dimensional weight and chargeable weight; matches Excel template `Downloads/RECIBO_DE_BODEGA_AWB_406-04429714.xlsx` |
| `frontend/src/api/airlines.js` | New module — `GET /api/airlines` and `GET /api/airlines/{id}` |
| `frontend/src/views/FlightsView.vue` | Added airline selector dropdown (loads from `/api/airlines`); removed hard-coded UPS UUID from form; validates airline is selected; added error message when airlines fail to load |
| `frontend/src/views/LoadPlanningView.vue` | Replaced placeholder template with real table (ULD columns + MAWB rows); fixed flight dropdown to show `flightNumber`; fixed meta fields to use `aircraftReg`, `origin`/`destination`, `flightDate`; added `syncFlightMetadata` to load ULDs on flight change |
| `frontend/src/views/DashboardView.vue` | `onMounted` now loads all bookings (`loadBookings()`), all MAWBs (`loadAllMawbs()`), and all ULDs (`loadUlds()`) without flightId filter; fixed metric to use `isConfirmed` instead of missing `status` field |
| `frontend/src/views/MawbsView.vue` | Complete rewrite: flight filter dropdown ("Todos los vuelos" + per-flight options); 5-card stats row (Total, Booked, Received, A Tiempo, Con Recibo); 12-column grid table with MAWB info + 4-dot BKD/RCV/MNF/DEP status flow + receipt indicator (✓ OK / ✕ Pend.) + on-time indicator (✓ A Tiempo / ✕ Atrasado / — Pend.); `isOnTime()`/`isLate()` derive timing from flight date |
| `frontend/src/components/SignaturePad.vue` | New — canvas-based signature capture (mouse + touch) |
| `frontend/src/components/CameraCapture.vue` | New — `getUserMedia()` camera modal with snapshot |
| `frontend/src/api/flights.js` | Fixed `create`/`update` to spread `dto` first so form-selected airline overrides UPS fallback |
| `backend/aircargo-api/src/main/resources/db/migration/V3__widen_signature_columns.sql` | New — changes 4 signature columns to `text` |
| `backend/aircargo-api/src/main/java/com/aircargo/entity/WarehouseReceipt.java` | Replaced `@Lob` with `@Column(columnDefinition = "text")` on signature URL fields; added `@Builder.Default` on 7 fields with initializers |
| `backend/aircargo-api/src/main/java/com/aircargo/controller/MawbController.java` | Added HAWB dedup logic: if MAWB with same airline+awbNumber exists with < 2 HAWBs, reuse it; inject `HawbRepository` |
| `backend/aircargo-api/src/main/java/com/aircargo/service/ReceiptExportService.java` | New — generates Excel (`.xlsx`) from a warehouse receipt matching the `RECIBO_DE_BODEGA_AWB_406-04429714.xlsx` template: single-sheet layout with title, header fields (left) + checkboxes (right), pieces table, totals, actual/chargeable weight, remarks, and signatures |
| `backend/aircargo-api/src/main/java/com/aircargo/service/WarehouseService.java` | On receipt emit, now also updates the MAWB status to `RECEIVED` so the status flow diagram advances past BOOKED |
| `backend/aircargo-api/src/main/java/com/aircargo/controller/WarehouseController.java` | Added `GET /{receiptId}/export` endpoint returning Excel file |
| `frontend/src/api/hawbs.js` | New — `GET /api/cargo/hawbs/mawb/{mawbId}` |
| `frontend/src/api/receipts.js` | Added `export(id)` method (returns blob) |
| `frontend/src/views/WarehouseReceiptsView.vue` | Added HAWB breakdown table in Step 1 (fetched on expand); added "Descargar Excel" button after successful receipt submission; stores `generatedReceiptId` |
| `frontend/src/views/BookingsView.vue` | When booking creates a MAWB, now links booking to MAWB via `store.updateBooking()` with `mawbId`; same for XLSX import flow |
| `backend/aircargo-api/src/main/java/com/aircargo/service/UldServiceImpl.java` | `assignFlight()` now handles null `flightId` (sets `uld.setFlight(null)` for unassign) |
| `backend/aircargo-api/src/main/java/com/aircargo/controller/UldController.java` | `PATCH /api/ulds/{id}/flight` accepts null/blank/`"null"` flightId → calls `assignFlight(id, null)` |
| `backend/aircargo-api/src/main/java/com/aircargo/entity/Uld.java` | Removed `nullable = false` from `@JoinColumn(name = "flight_id")` |
| `backend/aircargo-api/src/main/resources/db/migration/V5__make_uld_flight_id_nullable.sql` | New — `ALTER TABLE uld ALTER COLUMN flight_id DROP NOT NULL` |
| `database/migrations/V3__widen_signature_columns.sql` | Synced from backend |
| `database/migrations/V4__add_left_behind_uld_status.sql` | Synced from backend |
| `database/migrations/V5__make_uld_flight_id_nullable.sql` | Synced from backend |
| `frontend/src/views/LoadPlanningView.vue` | Replace alert with two-option dialog (Asignar a otro vuelo / Dejar flotante) on table→floating drag without history; `detachToFloating()` calls PATCH with null flightId; amber ring uses direct `classList` manipulation with `requestAnimationFrame` removal for guaranteed visual feedback; flight picker state reset on open |
| `AGENTS.md` | Documented this session's changes |

## Build

```sh
npm run build         # Vite build succeeds (type-check has pre-existing errors)
npm run type-check    # Fails on pre-existing .vue module declarations + ulds.ts strict mode
```

## Import paths

Frontend uses `@/` → `./src/` (configured in `vite.config.ts` and `tsconfig.app.json`).
