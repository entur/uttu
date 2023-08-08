CREATE TABLE flexible_area_key_values (
                                                flexible_area_pk bigint NOT NULL,
                                                key_values_id bigint NOT NULL,
                                                key_values_key character varying(255) NOT NULL
);

ALTER TABLE flexible_area_key_values OWNER TO uttu;

ALTER TABLE ONLY flexible_area_key_values
    ADD CONSTRAINT flexible_area_key_values_pkey PRIMARY KEY (flexible_area_pk, key_values_key);

ALTER TABLE ONLY flexible_area_key_values
    ADD CONSTRAINT flexible_area_key_values_unique_id UNIQUE (key_values_id);

ALTER TABLE ONLY flexible_area_key_values
    ADD CONSTRAINT flexible_area_key_values_fkey_value FOREIGN KEY (key_values_id) REFERENCES value(id);

ALTER TABLE ONLY flexible_area_key_values
    ADD CONSTRAINT flexible_area_key_values_fkey_stop_place FOREIGN KEY (flexible_area_pk) REFERENCES flexible_area(pk);
