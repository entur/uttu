CREATE TABLE branding (
 id bigint NOT NULL,
 name character varying(255),
 short_name character varying(255),
 description character varying(255),
 url character varying(255),
 image_url character varying(255)
);

ALTER TABLE branding OWNER TO uttu;

ALTER TABLE ONLY branding
    ADD CONSTRAINT branding_pkey PRIMARY KEY (id);

CREATE SEQUENCE branding_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE branding_seq
    OWNER TO uttu;
