-- DROP SCHEMA public;

CREATE SCHEMA public AUTHORIZATION pg_database_owner;

COMMENT ON SCHEMA public IS 'standard public schema';

-- DROP TYPE public."aircraft_type";

CREATE TYPE public."aircraft_type" AS ENUM (
	'MD11',
	'B747',
	'B757',
	'B737',
	'A300',
	'A310',
	'A330',
	'DC8',
	'OTHER',
	'B767',
	'F350');

-- DROP TYPE public."commodity_type";

CREATE TYPE public."commodity_type" AS ENUM (
	'DRY CARGO',
	'ELECTRONICS',
	'PERISHABLE',
	'HIGH VALUES',
	'CIGARETTES',
	'SMALL_PACKAGES',
	'WWEF',
	'LIVE_PLANTS',
	'GENERAL',
	'COMAT',
	'FCC',
	'EMPTY_ULD',
	'EMPTY_PALLET',
	'RED_TAG',
	'EMPTY_BAGS',
	'NETS',
	'SDQ_SDF',
	'SDQ_MIA');

-- DROP TYPE public."flight_status";

CREATE TYPE public."flight_status" AS ENUM (
	'SCHEDULED',
	'BOARDING',
	'DEPARTED',
	'ARRIVED',
	'CANCELLED',
	'DELAYED');

-- DROP TYPE public."mawb_status";

CREATE TYPE public."mawb_status" AS ENUM (
	'BOOKED',
	'RECEIVED',
	'MANIFESTED',
	'DEPARTED',
	'ARRIVED',
	'CANCELLED');

-- DROP TYPE public."uld_status";

CREATE TYPE public."uld_status" AS ENUM (
	'OPEN',
	'BUILT',
	'SEALED',
	'LOADED',
	'OFFLOADED');

-- DROP TYPE public."uld_type";

CREATE TYPE public."uld_type" AS ENUM (
	'PMC',
	'PAH',
	'PAG',
	'PAJ',
	'AAY',
	'AAZ',
	'AAD',
	'PIP',
	'BULK',
	'AMP',
	'AMJ');

-- DROP TYPE public."user_role";

CREATE TYPE public."user_role" AS ENUM (
	'ADMIN',
	'OPERATIONS',
	'WAREHOUSE',
	'READ_ONLY');
-- public.airline definition

-- Drop table

-- DROP TABLE public.airline;

CREATE TABLE public.airline (
	id uuid DEFAULT uuid_generate_v4() NOT NULL,
	code varchar(10) NOT NULL,
	"name" varchar(100) NOT NULL,
	iata_code varchar(3) NULL,
	country varchar(60) NULL,
	is_active bool DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT airline_code_key UNIQUE (code),
	CONSTRAINT airline_pkey PRIMARY KEY (id)
);

-- Table Triggers

create trigger trg_airline_updated before
update
    on
    public.airline for each row execute function set_updated_at();

-- Permissions

ALTER TABLE public.airline OWNER TO aircargo_user;
GRANT ALL ON TABLE public.airline TO aircargo_user;


-- public.flyway_schema_history definition

-- Drop table

-- DROP TABLE public.flyway_schema_history;

CREATE TABLE public.flyway_schema_history (
	installed_rank int4 NOT NULL,
	"version" varchar(50) NULL,
	description varchar(200) NOT NULL,
	"type" varchar(20) NOT NULL,
	script varchar(1000) NOT NULL,
	checksum int4 NULL,
	installed_by varchar(100) NOT NULL,
	installed_on timestamp DEFAULT now() NOT NULL,
	execution_time int4 NOT NULL,
	success bool NOT NULL,
	CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank)
);
CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);

-- Permissions

ALTER TABLE public.flyway_schema_history OWNER TO aircargo_user;
GRANT ALL ON TABLE public.flyway_schema_history TO aircargo_user;


-- public.app_user definition

-- Drop table

-- DROP TABLE public.app_user;

CREATE TABLE public.app_user (
	id uuid DEFAULT uuid_generate_v4() NOT NULL,
	airline_id uuid NOT NULL,
	supabase_uid uuid NULL,
	email varchar(200) NOT NULL,
	full_name varchar(150) NULL,
	"role" public."user_role" DEFAULT 'READ_ONLY'::user_role NOT NULL,
	is_active bool DEFAULT true NOT NULL,
	last_login timestamptz NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT app_user_email_key UNIQUE (email),
	CONSTRAINT app_user_pkey PRIMARY KEY (id),
	CONSTRAINT app_user_supabase_uid_key UNIQUE (supabase_uid),
	CONSTRAINT app_user_airline_id_fkey FOREIGN KEY (airline_id) REFERENCES public.airline(id) ON DELETE CASCADE
);
CREATE INDEX idx_user_airline ON public.app_user USING btree (airline_id);
CREATE INDEX idx_user_email ON public.app_user USING btree (email);
COMMENT ON TABLE public.app_user IS 'Usuario por aerolínea — rol y acceso multi-tenant';

-- Table Triggers

create trigger trg_app_user_updated before
update
    on
    public.app_user for each row execute function set_updated_at();

-- Permissions

ALTER TABLE public.app_user OWNER TO aircargo_user;
GRANT ALL ON TABLE public.app_user TO aircargo_user;


-- public.flight definition

-- Drop table

-- DROP TABLE public.flight;

CREATE TABLE public.flight (
	id uuid DEFAULT uuid_generate_v4() NOT NULL,
	airline_id uuid NOT NULL,
	flight_number varchar(20) NOT NULL,
	origin bpchar(3) DEFAULT 'SDQ'::bpchar NOT NULL,
	destination bpchar(3) NOT NULL,
	aircraft_reg varchar(20) NULL,
	"aircraft_type" public."aircraft_type" NULL,
	flight_date date NOT NULL,
	status public."flight_status" DEFAULT 'SCHEDULED'::flight_status NOT NULL,
	max_payload_kg numeric(10, 2) NULL,
	total_positions int4 NULL,
	notes text NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT flight_airline_id_flight_number_flight_date_key UNIQUE (airline_id, flight_number, flight_date),
	CONSTRAINT flight_pkey PRIMARY KEY (id),
	CONSTRAINT flight_airline_id_fkey FOREIGN KEY (airline_id) REFERENCES public.airline(id) ON DELETE CASCADE
);
CREATE INDEX idx_flight_airline_date ON public.flight USING btree (airline_id, flight_date);
CREATE INDEX idx_flight_status ON public.flight USING btree (status);
COMMENT ON TABLE public.flight IS 'Vuelo por aerolínea — contenedor de bookings, MAWBs y ULDs';

-- Table Triggers

create trigger trg_flight_updated before
update
    on
    public.flight for each row execute function set_updated_at();

-- Permissions

ALTER TABLE public.flight OWNER TO aircargo_user;
GRANT ALL ON TABLE public.flight TO aircargo_user;


-- public.mawb definition

-- Drop table

-- DROP TABLE public.mawb;

CREATE TABLE public.mawb (
	id uuid DEFAULT uuid_generate_v4() NOT NULL,
	airline_id uuid NOT NULL,
	flight_id uuid NULL,
	awb_number varchar(20) NOT NULL,
	shipper_name varchar(150) NULL,
	consignee_name varchar(150) NULL,
	origin bpchar(3) DEFAULT 'SDQ'::bpchar NOT NULL,
	destination bpchar(3) NOT NULL,
	pieces int4 DEFAULT 1 NOT NULL,
	reported_weight_kg numeric(10, 2) NULL,
	chargeable_weight_kg numeric(10, 2) NULL,
	"commodity_type" public."commodity_type" DEFAULT 'DRY CARGO'::commodity_type NOT NULL,
	status public."mawb_status" DEFAULT 'BOOKED'::mawb_status NOT NULL,
	cash_only bool DEFAULT false NULL,
	booked_in_acoms bool DEFAULT false NULL,
	docs_provided bool DEFAULT false NULL,
	customs_completed bool DEFAULT false NULL,
	pre_built bool DEFAULT false NULL,
	loose_tender bool DEFAULT false NULL,
	notes text NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT mawb_airline_id_awb_number_key UNIQUE (airline_id, awb_number),
	CONSTRAINT mawb_pkey PRIMARY KEY (id),
	CONSTRAINT mawb_airline_id_fkey FOREIGN KEY (airline_id) REFERENCES public.airline(id) ON DELETE CASCADE,
	CONSTRAINT mawb_flight_id_fkey FOREIGN KEY (flight_id) REFERENCES public.flight(id) ON DELETE SET NULL
);
CREATE INDEX idx_mawb_airline ON public.mawb USING btree (airline_id);
CREATE INDEX idx_mawb_awb_number ON public.mawb USING btree (awb_number);
CREATE INDEX idx_mawb_flight ON public.mawb USING btree (flight_id);
CREATE INDEX idx_mawb_status ON public.mawb USING btree (status);
COMMENT ON TABLE public.mawb IS 'Master Air Waybill — documento legal de embarque por vuelo';

-- Table Triggers

create trigger trg_mawb_updated before
update
    on
    public.mawb for each row execute function set_updated_at();

-- Permissions

ALTER TABLE public.mawb OWNER TO aircargo_user;
GRANT ALL ON TABLE public.mawb TO aircargo_user;


-- public.uld definition

-- Drop table

-- DROP TABLE public.uld;

CREATE TABLE public.uld (
	id uuid DEFAULT uuid_generate_v4() NOT NULL,
	airline_id uuid NOT NULL,
	flight_id uuid NOT NULL,
	uld_number varchar(30) NOT NULL,
	"uld_type" public."uld_type" NOT NULL,
	"position" varchar(10) NULL,
	config varchar(10) NULL,
	seal_number varchar(50) NULL,
	tare_lbs numeric(8, 2) DEFAULT 0 NOT NULL,
	tare_notes varchar(200) NULL,
	gross_weight_lbs numeric(10, 2) DEFAULT 0 NOT NULL,
	net_weight_lbs numeric(10, 2) GENERATED ALWAYS AS ((gross_weight_lbs - tare_lbs)) STORED NULL,
	tare_kg numeric(8, 2) GENERATED ALWAYS AS (round(tare_lbs * 0.453592, 2)) STORED NULL,
	gross_weight_kg numeric(10, 2) GENERATED ALWAYS AS (round(gross_weight_lbs * 0.453592, 2)) STORED NULL,
	net_weight_kg numeric(10, 2) GENERATED ALWAYS AS (round((gross_weight_lbs - tare_lbs) * 0.453592, 2)) STORED NULL,
	status public."uld_status" DEFAULT 'OPEN'::uld_status NOT NULL,
	built_at timestamptz NULL,
	loaded_at timestamptz NULL,
	notes text NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT uld_pkey PRIMARY KEY (id),
	CONSTRAINT uld_airline_id_fkey FOREIGN KEY (airline_id) REFERENCES public.airline(id) ON DELETE CASCADE,
	CONSTRAINT uld_flight_id_fkey FOREIGN KEY (flight_id) REFERENCES public.flight(id) ON DELETE CASCADE
);
CREATE INDEX idx_uld_airline ON public.uld USING btree (airline_id);
CREATE INDEX idx_uld_flight ON public.uld USING btree (flight_id);
CREATE INDEX idx_uld_number ON public.uld USING btree (uld_number);
CREATE INDEX idx_uld_status ON public.uld USING btree (status);
COMMENT ON TABLE public.uld IS 'Unit Load Device — pesos en libras, conversión a kg automática';

-- Table Triggers

create trigger trg_uld_updated before
update
    on
    public.uld for each row execute function set_updated_at();

-- Permissions

ALTER TABLE public.uld OWNER TO aircargo_user;
GRANT ALL ON TABLE public.uld TO aircargo_user;


-- public.uld_awb definition

-- Drop table

-- DROP TABLE public.uld_awb;

CREATE TABLE public.uld_awb (
	id uuid DEFAULT uuid_generate_v4() NOT NULL,
	uld_id uuid NOT NULL,
	mawb_id uuid NULL,
	mawb_label varchar(50) NULL,
	description public."commodity_type" DEFAULT 'DRY CARGO'::commodity_type NOT NULL,
	destination bpchar(3) NULL,
	pieces int4 DEFAULT 0 NULL,
	pieces_pct int4 DEFAULT 100 NULL,
	temp_inbound numeric(6, 2) NULL,
	temp_outbound numeric(6, 2) NULL,
	hc bool DEFAULT false NULL,
	"comments" text NULL,
	consumption_pallets numeric(6, 3) NULL,
	start_time time NULL,
	end_time time NULL,
	avg_time_per_piece_sec int4 DEFAULT 5 NULL,
	lapse_minutes numeric(8, 2) GENERATED ALWAYS AS ((EXTRACT(epoch FROM end_time - start_time) / 60::numeric)) STORED NULL,
	pcs_per_min numeric(8, 3) GENERATED ALWAYS AS (
CASE
    WHEN (EXTRACT(epoch FROM end_time - start_time) / 60::numeric) > 0::numeric THEN round(pieces::numeric / (EXTRACT(epoch FROM end_time - start_time) / 60::numeric), 3)
    ELSE 0::numeric
END) STORED NULL,
	operative_worked_hours numeric(8, 3) GENERATED ALWAYS AS (round(EXTRACT(epoch FROM end_time - start_time) / 3600::numeric, 3)) STORED NULL,
	earned_hours numeric(8, 3) GENERATED ALWAYS AS (round((pieces * avg_time_per_piece_sec)::numeric / 3600::numeric, 3)) STORED NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT chk_mawb_reference CHECK (((mawb_id IS NOT NULL) OR (mawb_label IS NOT NULL))),
	CONSTRAINT uld_awb_pkey PRIMARY KEY (id),
	CONSTRAINT uld_awb_mawb_id_fkey FOREIGN KEY (mawb_id) REFERENCES public.mawb(id) ON DELETE SET NULL,
	CONSTRAINT uld_awb_uld_id_fkey FOREIGN KEY (uld_id) REFERENCES public.uld(id) ON DELETE CASCADE
);
CREATE INDEX idx_uld_awb_mawb ON public.uld_awb USING btree (mawb_id);
CREATE INDEX idx_uld_awb_uld ON public.uld_awb USING btree (uld_id);
COMMENT ON TABLE public.uld_awb IS 'Relación N:M ULD-MAWB — mawb_id NULL cuando es EMPTY ULD, RED TAG, NETS, etc.';

-- Permissions

ALTER TABLE public.uld_awb OWNER TO aircargo_user;
GRANT ALL ON TABLE public.uld_awb TO aircargo_user;


-- public.uld_type_config definition

-- Drop table

-- DROP TABLE public.uld_type_config;

CREATE TABLE public.uld_type_config (
	id uuid DEFAULT uuid_generate_v4() NOT NULL,
	airline_id uuid NOT NULL,
	"uld_type" public."uld_type" NOT NULL,
	default_tare_lbs numeric(8, 2) NOT NULL,
	max_gross_lbs numeric(10, 2) NULL,
	notes text NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT uld_type_config_airline_id_uld_type_key UNIQUE (airline_id, uld_type),
	CONSTRAINT uld_type_config_pkey PRIMARY KEY (id),
	CONSTRAINT uld_type_config_airline_id_fkey FOREIGN KEY (airline_id) REFERENCES public.airline(id) ON DELETE CASCADE
);
COMMENT ON TABLE public.uld_type_config IS 'Tares de referencia en libras por tipo de ULD — editables por aerolínea';

-- Table Triggers

create trigger trg_uld_type_config_updated before
update
    on
    public.uld_type_config for each row execute function set_updated_at();

-- Permissions

ALTER TABLE public.uld_type_config OWNER TO aircargo_user;
GRANT ALL ON TABLE public.uld_type_config TO aircargo_user;


-- public.warehouse_receipt definition

-- Drop table

-- DROP TABLE public.warehouse_receipt;

CREATE TABLE public.warehouse_receipt (
	id uuid DEFAULT uuid_generate_v4() NOT NULL,
	airline_id uuid NOT NULL,
	mawb_id uuid NOT NULL,
	gateway_cfs varchar(10) DEFAULT 'SDQ'::character varying NULL,
	shipper_name varchar(150) NULL,
	consignee_name varchar(150) NULL,
	agent_name varchar(150) NULL,
	origin bpchar(3) DEFAULT 'SDQ'::bpchar NULL,
	destination bpchar(3) NULL,
	awb_reported_pieces int4 NULL,
	mawb_weight_greatest numeric(10, 3) NULL,
	shipper_reported_weight numeric(10, 3) NULL,
	start_datetime timestamptz NULL,
	receipt_date timestamptz DEFAULT now() NULL,
	cash_only bool DEFAULT false NULL,
	booked_in_acoms bool DEFAULT false NULL,
	docs_provided bool DEFAULT false NULL,
	customs_completed bool DEFAULT false NULL,
	pre_built bool DEFAULT false NULL,
	loose_tender bool DEFAULT false NULL,
	piece_count int4 DEFAULT 0 NULL,
	dim_factor_dom numeric(6) DEFAULT 194 NULL,
	dim_factor_intl numeric(6) DEFAULT 366 NULL,
	actual_weight_lbs numeric(10, 3) DEFAULT 0 NULL,
	actual_weight_kg numeric(10, 3) DEFAULT 0 NULL,
	chargeable_weight_lbs numeric(10, 3) DEFAULT 0 NULL,
	chargeable_weight_kg numeric(10, 3) DEFAULT 0 NULL,
	shipper_comment text NULL,
	observations text NULL,
	remarks text NULL,
	created_by_user_id uuid NULL,
	created_by_name varchar(150) NULL,
	delivered_by_name varchar(150) NULL,
	delivered_by_id_num varchar(50) NULL,
	delivered_by_id_doc_url text NULL,
	delivered_by_sig_url text NULL,
	received_by_name varchar(150) NULL,
	received_by_id_num varchar(50) NULL,
	received_by_id_doc_url text NULL,
	received_by_sig_url text NULL,
	broker_name varchar(150) NULL,
	broker_id_num varchar(50) NULL,
	broker_id_doc_url text NULL,
	broker_sig_url text NULL,
	receipt_doc_url text NULL,
	dock_signature varchar(150) NULL,
	print_name varchar(150) NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT warehouse_receipt_pkey PRIMARY KEY (id),
	CONSTRAINT warehouse_receipt_airline_id_fkey FOREIGN KEY (airline_id) REFERENCES public.airline(id) ON DELETE CASCADE,
	CONSTRAINT warehouse_receipt_created_by_user_id_fkey FOREIGN KEY (created_by_user_id) REFERENCES public.app_user(id) ON DELETE SET NULL,
	CONSTRAINT warehouse_receipt_mawb_id_fkey FOREIGN KEY (mawb_id) REFERENCES public.mawb(id) ON DELETE CASCADE
);
CREATE INDEX idx_receipt_airline ON public.warehouse_receipt USING btree (airline_id);
CREATE INDEX idx_receipt_created_by ON public.warehouse_receipt USING btree (created_by_user_id);
CREATE INDEX idx_receipt_mawb ON public.warehouse_receipt USING btree (mawb_id);
COMMENT ON TABLE public.warehouse_receipt IS 'Recibo de Almacén — UPS Air Cargo Dock Receipt con firmas y documentos';

-- Table Triggers

create trigger trg_receipt_updated before
update
    on
    public.warehouse_receipt for each row execute function set_updated_at();

-- Permissions

ALTER TABLE public.warehouse_receipt OWNER TO aircargo_user;
GRANT ALL ON TABLE public.warehouse_receipt TO aircargo_user;


-- public.booking definition

-- Drop table

-- DROP TABLE public.booking;

CREATE TABLE public.booking (
	id uuid DEFAULT uuid_generate_v4() NOT NULL,
	airline_id uuid NOT NULL,
	flight_id uuid NOT NULL,
	mawb_id uuid NULL,
	client_name varchar(150) NOT NULL,
	contact_name varchar(100) NOT NULL,
	reserved_kg numeric(10, 3) NOT NULL,
	awb_number varchar(20) NULL,
	cnee varchar(150) NULL,
	shipper_name varchar(150) NULL,
	skids int4 DEFAULT 0 NULL,
	units int4 DEFAULT 0 NULL,
	confirmed_kg numeric(10, 3) DEFAULT 0 NULL,
	received_kg numeric(10, 3) DEFAULT 0 NULL,
	fulfillment_pct numeric(6, 4) DEFAULT 0 NULL,
	destination bpchar(3) NULL,
	priority int4 DEFAULT 0 NULL,
	"commodity_type" public."commodity_type" DEFAULT 'DRY CARGO'::commodity_type NOT NULL,
	day_received varchar(5) NULL,
	time_hours numeric(6, 4) DEFAULT 0 NULL,
	positions numeric(8, 4) DEFAULT 0 NULL,
	real_positions numeric(8, 4) DEFAULT 0 NULL,
	last_week_kg numeric(10, 3) DEFAULT 0 NULL,
	last_week_positions numeric(8, 4) DEFAULT 0 NULL,
	is_confirmed bool DEFAULT false NULL,
	notes text NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT booking_pkey PRIMARY KEY (id),
	CONSTRAINT chk_awb_format CHECK (((awb_number IS NULL) OR ((awb_number)::text ~ '^\d{3}-\d{8}$'::text))),
	CONSTRAINT booking_airline_id_fkey FOREIGN KEY (airline_id) REFERENCES public.airline(id) ON DELETE CASCADE,
	CONSTRAINT booking_flight_id_fkey FOREIGN KEY (flight_id) REFERENCES public.flight(id) ON DELETE CASCADE,
	CONSTRAINT booking_mawb_id_fkey FOREIGN KEY (mawb_id) REFERENCES public.mawb(id) ON DELETE SET NULL
);
CREATE INDEX idx_booking_airline ON public.booking USING btree (airline_id);
CREATE INDEX idx_booking_flight ON public.booking USING btree (flight_id);
CREATE INDEX idx_booking_mawb ON public.booking USING btree (mawb_id);
COMMENT ON TABLE public.booking IS 'Hoja de vuelo — solo filas con client_name, contact_name y reserved_kg no nulos';

-- Table Triggers

create trigger trg_booking_updated before
update
    on
    public.booking for each row execute function set_updated_at();

-- Permissions

ALTER TABLE public.booking OWNER TO aircargo_user;
GRANT ALL ON TABLE public.booking TO aircargo_user;


-- public.hawb definition

-- Drop table

-- DROP TABLE public.hawb;

CREATE TABLE public.hawb (
	id uuid DEFAULT uuid_generate_v4() NOT NULL,
	mawb_id uuid NOT NULL,
	airline_id uuid NOT NULL,
	hawb_number varchar(30) NOT NULL,
	consignee_name varchar(150) NULL,
	destination bpchar(3) NULL,
	pieces int4 DEFAULT 1 NOT NULL,
	weight_kg numeric(10, 2) NULL,
	"commodity_type" public."commodity_type" DEFAULT 'DRY CARGO'::commodity_type NULL,
	status public."mawb_status" DEFAULT 'BOOKED'::mawb_status NOT NULL,
	notes text NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT hawb_mawb_id_hawb_number_key UNIQUE (mawb_id, hawb_number),
	CONSTRAINT hawb_pkey PRIMARY KEY (id),
	CONSTRAINT hawb_airline_id_fkey FOREIGN KEY (airline_id) REFERENCES public.airline(id) ON DELETE CASCADE,
	CONSTRAINT hawb_mawb_id_fkey FOREIGN KEY (mawb_id) REFERENCES public.mawb(id) ON DELETE CASCADE
);
CREATE INDEX idx_hawb_airline ON public.hawb USING btree (airline_id);
CREATE INDEX idx_hawb_mawb ON public.hawb USING btree (mawb_id);
COMMENT ON TABLE public.hawb IS 'House Air Waybill -- consignee individual bajo un MAWB. Shipper heredado del MAWB.';

-- Table Triggers

create trigger trg_hawb_updated before
update
    on
    public.hawb for each row execute function set_updated_at();

-- Permissions

ALTER TABLE public.hawb OWNER TO aircargo_user;
GRANT ALL ON TABLE public.hawb TO aircargo_user;


-- public.receipt_piece definition

-- Drop table

-- DROP TABLE public.receipt_piece;

CREATE TABLE public.receipt_piece (
	id uuid DEFAULT uuid_generate_v4() NOT NULL,
	receipt_id uuid NOT NULL,
	piece_number int4 NOT NULL,
	length_in numeric(8, 2) DEFAULT 0 NULL,
	width_in numeric(8, 2) DEFAULT 0 NULL,
	height_in numeric(8, 2) DEFAULT 0 NULL,
	scale_weight_lbs numeric(10, 3) DEFAULT 0 NULL,
	scale_weight_kg numeric(10, 3) DEFAULT 0 NULL,
	dim_weight_lbs numeric(10, 3) DEFAULT 0 NULL,
	dim_weight_kg numeric(10, 3) DEFAULT 0 NULL,
	chargeable_lbs numeric(10, 3) DEFAULT 0 NULL,
	chargeable_kg numeric(10, 3) DEFAULT 0 NULL,
	CONSTRAINT receipt_piece_pkey PRIMARY KEY (id),
	CONSTRAINT receipt_piece_receipt_id_piece_number_key UNIQUE (receipt_id, piece_number),
	CONSTRAINT receipt_piece_receipt_id_fkey FOREIGN KEY (receipt_id) REFERENCES public.warehouse_receipt(id) ON DELETE CASCADE
);
COMMENT ON TABLE public.receipt_piece IS 'Pieza individual del recibo — dim weight calculado por pieza';

-- Permissions

ALTER TABLE public.receipt_piece OWNER TO aircargo_user;
GRANT ALL ON TABLE public.receipt_piece TO aircargo_user;



-- DROP FUNCTION public.set_updated_at();

CREATE OR REPLACE FUNCTION public.set_updated_at()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$function$
;

-- Permissions

ALTER FUNCTION public.set_updated_at() OWNER TO aircargo_user;
GRANT ALL ON FUNCTION public.set_updated_at() TO aircargo_user;

-- DROP FUNCTION public.uuid_generate_v1();

CREATE OR REPLACE FUNCTION public.uuid_generate_v1()
 RETURNS uuid
 LANGUAGE c
 PARALLEL SAFE STRICT
AS '$libdir/uuid-ossp', $function$uuid_generate_v1$function$
;

-- Permissions

ALTER FUNCTION public.uuid_generate_v1() OWNER TO aircargo_user;
GRANT ALL ON FUNCTION public.uuid_generate_v1() TO aircargo_user;

-- DROP FUNCTION public.uuid_generate_v1mc();

CREATE OR REPLACE FUNCTION public.uuid_generate_v1mc()
 RETURNS uuid
 LANGUAGE c
 PARALLEL SAFE STRICT
AS '$libdir/uuid-ossp', $function$uuid_generate_v1mc$function$
;

-- Permissions

ALTER FUNCTION public.uuid_generate_v1mc() OWNER TO aircargo_user;
GRANT ALL ON FUNCTION public.uuid_generate_v1mc() TO aircargo_user;

-- DROP FUNCTION public.uuid_generate_v3(uuid, text);

CREATE OR REPLACE FUNCTION public.uuid_generate_v3(namespace uuid, name text)
 RETURNS uuid
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/uuid-ossp', $function$uuid_generate_v3$function$
;

-- Permissions

ALTER FUNCTION public.uuid_generate_v3(uuid, text) OWNER TO aircargo_user;
GRANT ALL ON FUNCTION public.uuid_generate_v3(uuid, text) TO aircargo_user;

-- DROP FUNCTION public.uuid_generate_v4();

CREATE OR REPLACE FUNCTION public.uuid_generate_v4()
 RETURNS uuid
 LANGUAGE c
 PARALLEL SAFE STRICT
AS '$libdir/uuid-ossp', $function$uuid_generate_v4$function$
;

-- Permissions

ALTER FUNCTION public.uuid_generate_v4() OWNER TO aircargo_user;
GRANT ALL ON FUNCTION public.uuid_generate_v4() TO aircargo_user;

-- DROP FUNCTION public.uuid_generate_v5(uuid, text);

CREATE OR REPLACE FUNCTION public.uuid_generate_v5(namespace uuid, name text)
 RETURNS uuid
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/uuid-ossp', $function$uuid_generate_v5$function$
;

-- Permissions

ALTER FUNCTION public.uuid_generate_v5(uuid, text) OWNER TO aircargo_user;
GRANT ALL ON FUNCTION public.uuid_generate_v5(uuid, text) TO aircargo_user;

-- DROP FUNCTION public.uuid_nil();

CREATE OR REPLACE FUNCTION public.uuid_nil()
 RETURNS uuid
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/uuid-ossp', $function$uuid_nil$function$
;

-- Permissions

ALTER FUNCTION public.uuid_nil() OWNER TO aircargo_user;
GRANT ALL ON FUNCTION public.uuid_nil() TO aircargo_user;

-- DROP FUNCTION public.uuid_ns_dns();

CREATE OR REPLACE FUNCTION public.uuid_ns_dns()
 RETURNS uuid
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/uuid-ossp', $function$uuid_ns_dns$function$
;

-- Permissions

ALTER FUNCTION public.uuid_ns_dns() OWNER TO aircargo_user;
GRANT ALL ON FUNCTION public.uuid_ns_dns() TO aircargo_user;

-- DROP FUNCTION public.uuid_ns_oid();

CREATE OR REPLACE FUNCTION public.uuid_ns_oid()
 RETURNS uuid
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/uuid-ossp', $function$uuid_ns_oid$function$
;

-- Permissions

ALTER FUNCTION public.uuid_ns_oid() OWNER TO aircargo_user;
GRANT ALL ON FUNCTION public.uuid_ns_oid() TO aircargo_user;

-- DROP FUNCTION public.uuid_ns_url();

CREATE OR REPLACE FUNCTION public.uuid_ns_url()
 RETURNS uuid
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/uuid-ossp', $function$uuid_ns_url$function$
;

-- Permissions

ALTER FUNCTION public.uuid_ns_url() OWNER TO aircargo_user;
GRANT ALL ON FUNCTION public.uuid_ns_url() TO aircargo_user;

-- DROP FUNCTION public.uuid_ns_x500();

CREATE OR REPLACE FUNCTION public.uuid_ns_x500()
 RETURNS uuid
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/uuid-ossp', $function$uuid_ns_x500$function$
;

-- Permissions

ALTER FUNCTION public.uuid_ns_x500() OWNER TO aircargo_user;
GRANT ALL ON FUNCTION public.uuid_ns_x500() TO aircargo_user;


-- Permissions

GRANT ALL ON SCHEMA public TO pg_database_owner;
GRANT USAGE ON SCHEMA public TO public;


