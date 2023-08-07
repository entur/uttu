ALTER TABLE flexible_area ADD COLUMN  flexible_stop_place_pk bigint;
ALTER TABLE flexible_stop_place DROP COLUMN flexible_area_pk;