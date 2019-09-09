ALTER TABLE ONLY notice ALTER COLUMN text character varying(4000) NOT NULL,

ALTER TABLE ONLY contact ALTER COLUMN further_details character varying(4000) NOT NULL,

ALTER TABLE ONLY flexible_line ALTER COLUMN description character varying(4000) NOT NULL,
ALTER TABLE ONLY flexible_stop_place ALTER COLUMN description character varying(4000) NOT NULL,
ALTER TABLE ONLY service_journey ALTER COLUMN description character varying(4000) NOT NULL,
ALTER TABLE ONLY network ALTER COLUMN description character varying(4000) NOT NULL,
ALTER TABLE ONLY journey_pattern ALTER COLUMN description character varying(4000) NOT NULL,

