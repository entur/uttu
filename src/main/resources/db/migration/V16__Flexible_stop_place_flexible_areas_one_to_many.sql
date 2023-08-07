ALTER TABLE flexible_area ADD COLUMN  flexible_stop_place_pk bigint;

UPDATE flexible_area SET flexible_stop_place_pk = flexible_stop_place.pk
                    FROM flexible_stop_place
                    WHERE flexible_area.pk = flexible_stop_place.flexible_area_pk;

ALTER TABLE flexible_stop_place DROP COLUMN flexible_area_pk;