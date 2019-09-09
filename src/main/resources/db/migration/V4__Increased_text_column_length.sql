ALTER TABLE notice ALTER COLUMN text TYPE character varying(4000);

ALTER TABLE contact ALTER COLUMN further_details TYPE character varying(4000);

ALTER TABLE flexible_line ALTER COLUMN description TYPE character varying(4000);
ALTER TABLE flexible_stop_place ALTER COLUMN description TYPE character varying(4000);
ALTER TABLE service_journey ALTER COLUMN description TYPE character varying(4000);
ALTER TABLE network ALTER COLUMN description TYPE character varying(4000);
ALTER TABLE journey_pattern ALTER COLUMN description TYPE character varying(4000);

