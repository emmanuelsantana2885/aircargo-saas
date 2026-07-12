# AirCargo SDQ

Sistema integral de gestión de carga aérea — control de vuelos, reservas, MAWBs, recepción en bodega, ULDs, planificación de carga y compliance aduanal.

## Stack Tecnológico

| Capa | Tecnología |
|------|------------|
| Frontend | Vue 3 + Vite + Pinia + Vue Router + Tailwind CSS + TypeScript |
| Backend | Spring Boot 3.3 + Java 21 + Maven + JPA + Flyway |
| Base de datos | PostgreSQL 16 (Docker) |
| Autenticación | JWT (jjwt 0.12.6) + BCrypt |
| Reportes | Apache POI (Excel) + openhtmltopdf (PDF) |

## Estructura del Proyecto

```
aircargo-saas/
├── frontend/              # Vue 3 + Vite (puerto 5173)
│   └── src/
│       ├── api/           # Llamadas HTTP (axios)
│       ├── stores/        # Pinia stores
│       ├── views/         # Vistas principales
│       └── components/    # Componentes reutilizables
├── backend/
│   └── aircargo-api/      # Spring Boot (puerto 9091)
│       └── src/main/java/com/aircargo/
│           ├── controller/
│           ├── service/
│           ├── entity/
│           ├── repository/
│           └── config/
├── database/
│   └── migrations/        # Flyway SQL migrations
├── docker-compose.yml     # PostgreSQL
└── .env.example
```

## Inicio Rápido

### 1. Base de datos

```bash
docker compose up -d
```

### 2. Backend

```bash
cd backend/aircargo-api
mvn spring-boot:run
```

El backend arranca en `http://localhost:9091`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

El frontend arranca en `http://localhost:5173` y proxea `/api` al backend.

## Funcionalidades

- **Vuelos** — Registro y seguimiento de vuelos por aerolínea
- **Reservas (Bookings)** — Control de bookings con fulfilment tracking
- **MAWBs** — Gestión de Air Waybill maestros con timeline de estados
- **HAWBs** — House Air Waybill por MAWB
- **Recepción en Bodega** — Emisión de recibos con firmas, evidencias y PDF
- **ULDs** — Unidades de carga con asignación a vuelos y transferencias
- **Planificación de Carga** — Importación de ramp manifests, cálculo de posiciones
- **Compliance** — Registro de DUA (declaraciones aduanales)
- **Tracking** — Timeline completo de cada MAWB
- **Exportación** — CSV, XLSX, JSON de todas las entidades
- **Business Intelligence** — API dedicada para Power BI (`/api/bi/*`)

## API para Power BI

Endpoints de solo lectura optimizados para ingestión en Power BI:

| Endpoint | Descripción |
|----------|-------------|
| `GET /api/bi/flights` | Vuelos con métricas de ULDs y peso |
| `GET /api/bi/bookings` | Reservas con fulfilment |
| `GET /api/bi/mawbs` | MAWBs con estado y conteos |
| `GET /api/bi/receipts` | Recepciones de bodega |
| `GET /api/bi/ulds` | ULDs con pesos y estado |
| `GET /api/bi/dashboard` | KPIs globales |
| `GET /api/bi/daily` | Métricas diarias |

Conecta desde Power BI: **Get Data → Web → `http://localhost:9091/api/bi/flights`**

## Usuarios de Prueba

| Email | Rol |
|-------|-----|
| readonly@aircargo.com | READ_ONLY |
| warehouse@aircargo.com | WAREHOUSE_ASSISTANT |
| operations@aircargo.com | OPERATIONS |
| traffic@aircargo.com | TRAFFIC |
| loadplanner@aircargo.com | LOAD_PLANNER |
| admin@aircargo.com | ADMIN |
| supervisor@aircargo.com | SUPER_USER |

---

**Emmanuel Santana Solano** ----emmanuelsantana2885@gmail.com -- Todos los derechos reservados----
