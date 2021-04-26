CREATE TABLE value (
    id bigint NOT NULL
);

ALTER TABLE value OWNER TO uttu;

ALTER TABLE ONLY value
    ADD CONSTRAINT value_pkey PRIMARY KEY (id);

CREATE TABLE value_items (
    value_id bigint NOT NULL,
    items character varying(255)
);

ALTER TABLE value_items OWNER TO uttu;

CREATE INDEX value_id_index ON value_items USING btree (value_id);

CREATE INDEX items_index ON value_items USING btree (items);

ALTER TABLE ONLY value_items
    ADD CONSTRAINT valute_items_foreign_key_constraint FOREIGN KEY (value_id) REFERENCES value(id);

CREATE TABLE flexible_stop_place_key_values (
    flexible_stop_place_pk bigint NOT NULL,
    key_values_id bigint NOT NULL,
    key_values_key character varying(255) NOT NULL
);

ALTER TABLE flexible_stop_place_key_values OWNER TO uttu;

ALTER TABLE ONLY flexible_stop_place_key_values
    ADD CONSTRAINT flexible_stop_place_key_values_pkey PRIMARY KEY (flexible_stop_place_pk, key_values_key);

ALTER TABLE ONLY flexible_stop_place_key_values
    ADD CONSTRAINT flexible_stop_place_key_values_unique_id UNIQUE (key_values_id);

ALTER TABLE ONLY flexible_stop_place_key_values
    ADD CONSTRAINT flexible_stop_place_key_values_fkey_value FOREIGN KEY (key_values_id) REFERENCES value(id);

ALTER TABLE ONLY flexible_stop_place_key_values
    ADD CONSTRAINT flexible_stop_place_key_values_fkey_stop_place FOREIGN KEY (flexible_stop_place_pk) REFERENCES flexible_stop_place(pk);
