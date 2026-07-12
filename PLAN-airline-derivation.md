# Plan: Derive Airline from Flight → Booking → MAWB → Receipt

## Goal
Remove all hardcoded UPS UUIDs. Airline is selected once at flight creation and cascades automatically to bookings, MAWBs, and warehouse receipts.

## Current State
- Every entity has its own `airline_id` FK (NOT NULL)
- Only `FlightsView` has an airline dropdown
- `BookingsView`, `MawbsView`, `WarehouseReceiptsView` hardcode UPS UUID `'00000000-0000-0000-0000-000000000001'`
- Hardcoded in 5+ API files and 6+ view files

## Target Flow
```
Flight (user picks airline from dropdown)
  └─► Booking (airline derived from selected flight)
        └─► MAWB (airline derived from booking/flight)
              └─► Receipt (airline derived from MAWB)
```

---

## Changes

### 1. API Files — Remove Hardcoded UPS

**`api/bookings.js`** — Remove `UPS` constant. `getAll()` and `getByFlight()` take `airlineId` as parameter. `create()` takes `airlineId` from caller.

**`api/receipts.js`** — Remove `UPS` constant. `getAll()` takes optional `airlineId` param. `create()` takes `airlineId` from caller.

**`api/mawbs.js`** — Remove hardcoded `UPS_AIRLINE_ID` if present. Pass `airlineId` from caller.

**`stores/app.js`** — Remove `UPS` constant. `loadBookings()` and `loadReceipts()` pass airline context. Add `airlines` array + `loadAirlines()` action. Export `airlines` ref.

### 2. BookingsView — Derive Airline from Selected Flight

**`BookingsView.vue`:**
- Remove hardcoded UUID from `saveBooking()` (line 642), `confirmImport()` (line 573), and MAWB creation (lines 592, 663)
- Replace with: `airlineId: store.selectedFlight?.airlineId`
- The flight selector already shows `store.selectedFlight` — use its `airlineId`
- For MAWB creation: `airline: { id: store.selectedFlight?.airlineId }`

### 3. WarehouseReceiptsView — Derive Airline from MAWB

**`WarehouseReceiptsView.vue`:**
- Remove hardcoded UUID from `buildPayload()` (line 1902)
- Replace with: `airline: { id: m.airline?.id || m.airlineId }`
- The MAWB object (`m`) already carries `airline` from the backend — use it directly
- The receipt form opens by expanding a MAWB row, so `m.airline` is always available

### 4. MawbsView — Derive Airline from Flight Context

**`MawbsView.vue`:**
- Remove hardcoded UUID from ULD API call (line 722)
- Replace with: `airlineId: store.selectedFlight?.airlineId || ''`

### 5. Other Views — Remove Hardcoded UUIDs

**`LoadPlanningView.vue`:**
- Remove `UPS_AIRLINE_ID` constant (line 316)
- Replace with `store.selectedFlight?.airlineId`

**`UldsView.vue`:**
- Remove `UPS_AIRLINE_ID` constant (line 305)
- Replace with `store.selectedFlight?.airlineId`
- Remove hardcoded UUIDs from `createUld()` (lines 544, 583, 630)

### 6. Backend — Auto-Resolve Airline from MAWB (Safety Net)

**`WarehouseController.java`** (`emitWarehouseReceipt`):
- If `receipt.airline` is null or empty, resolve it from the MAWB:
  ```java
  if (payload.receipt.getAirline() == null || payload.receipt.getAirline().getId() == null) {
      Mawb mawb = mawbRepository.findById(payload.receipt.getMawb().getId())
          .orElseThrow(...);
      payload.receipt.setAirline(mawb.getAirline());
  }
  ```
- This is a safety net — the frontend should always send it, but the backend ensures consistency.

**`MawbController.java`** (`createMawb`):
- If `mawb.airline` is null, derive from the flight:
  ```java
  if (mawb.getAirline() == null && mawb.getFlight() != null) {
      mawb.setAirline(mawb.getFlight().getAirline());
  }
  ```

**`BookingServiceImpl.java`** (`create`):
- If `dto.airlineId` is null, derive from the flight:
  ```java
  if (dto.getAirlineId() == null && dto.getFlightId() != null) {
      Flight flight = flightRepository.findById(dto.getFlightId()).orElse(null);
      if (flight != null) dto.setAirlineId(flight.getAirline().getId());
  }
  ```

### 7. Store — Add Airlines Support

**`stores/app.js`:**
- Add `import { airlinesApi } from '../api/airlines'`
- Add `const airlines = shallowRef([])`
- Add `async function loadAirlines()` action
- Export `airlines` and `loadAirlines`
- All views that need airline context can use `store.airlines` + `store.selectedFlight?.airlineId`

### 8. BookingsView Flight Dropdown — Show Airline Code

**`BookingsView.vue`:**
- Change flight dropdown option text from `UPS-{{ flight.flightNumber }}` to show the actual airline code:
  ```vue
  {{ airlineCode(flight) }}-{{ flight.flightNumber }} ({{ flight.origin }}→{{ flight.destination }})
  ```
- Add `airlineCode(flight)` helper that looks up airline from `store.airlines`

---

## Files Modified

| # | File | Change |
|---|------|--------|
| 1 | `api/bookings.js` | Remove UPS constant, accept airlineId param |
| 2 | `api/receipts.js` | Remove UPS constant, accept airlineId param |
| 3 | `api/mawbs.js` | Remove hardcoded airline, accept airlineId |
| 4 | `stores/app.js` | Remove UPS, add airlines state + loadAirlines |
| 5 | `BookingsView.vue` | Derive airline from selectedFlight |
| 6 | `WarehouseReceiptsView.vue` | Derive airline from MAWB |
| 7 | `MawbsView.vue` | Derive airline from selectedFlight |
| 8 | `LoadPlanningView.vue` | Derive airline from selectedFlight |
| 9 | `UldsView.vue` | Derive airline from selectedFlight |
| 10 | `WarehouseController.java` | Auto-resolve airline from MAWB |
| 11 | `MawbController.java` | Auto-resolve airline from Flight |
| 12 | `BookingServiceImpl.java` | Auto-resolve airline from Flight |

## Verification
1. `mvn test` — backend tests pass
2. `npm run type-check` — frontend compiles
3. Create a flight with AMERIJET airline → create booking → create MAWB → create receipt → verify all entities have AMERIJET airline, not UPS
