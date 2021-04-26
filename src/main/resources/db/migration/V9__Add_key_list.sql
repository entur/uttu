CREATE TABLE key_list (
     pk bigint NOT NULL;
);

CREATE_TABLE key_value (
    pk bigint NOT NULL,
    key_list_pk bigint NOT NULL,
    type_of_key character varying(255),
    key_value_key character varying(255) NOT NULL,
    key_value_value character varying(255) NOT NULL;
);

CREATE SEQUENCE key_list_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE key_list_seq OWNER TO uttu;

ALTER TABLE key_list OWNER TO uttu;

CREATE SEQUENCE key_value_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE key_value_seq OWNER TO uttu;

ALTER TABLE key_value OWNER TO uttu;

ALTER TABLE ONLY network
    ADD COLUMN bigint key_list_pk;

ALTER TABLE ONLY line
    ADD COLUMN bigint key_list_pk;

ALTER TABLE ONLY journey_pattern
    ADD COLUMN bigint key_list_pk;

ALTER TABLE ONLY service_journey
    ADD COLUMN bigint key_list_pk;

ALTER TABLE ONLY line
    ADD COLUMN bigint key_list_pk;