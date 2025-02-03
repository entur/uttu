CREATE TABLE branding (
 pk bigint NOT NULL,
 changed timestamp without time zone NOT NULL,
 changed_by character varying(255) NOT NULL,
 created timestamp without time zone NOT NULL,
 created_by character varying(255) NOT NULL,
 version bigint NOT NULL,
 netex_id character varying(255) NOT NULL,
 provider_pk bigint NOT NULL,
 name character varying(255),
 short_name character varying(255),
 description character varying(255),
 url character varying(255),
 image_url character varying(255)
);

ALTER TABLE branding OWNER TO uttu;

ALTER TABLE ONLY branding
    ADD CONSTRAINT branding_pkey PRIMARY KEY (pk);

CREATE SEQUENCE branding_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE branding_seq
    OWNER TO uttu;

ALTER TABLE ONLY line ADD COLUMN
    branding_pk bigint;

ALTER TABLE ONLY line
    ADD CONSTRAINT line_branding_fk FOREIGN KEY (branding_pk) REFERENCES branding(pk);
