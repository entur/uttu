CREATE TABLE exported_line_statistics
(
    id                    bigint                      NOT NULL,
    export_pk             bigint                      NOT NULL,
    line_name             character varying(255),
    public_code           character varying(255),
    operating_period_from timestamp without time zone NOT NULL,
    operating_period_to   timestamp without time zone NOT NULL
);

ALTER TABLE ONLY exported_line_statistics
    ADD CONSTRAINT export_fkey FOREIGN KEY (export_pk) REFERENCES export (pk);

ALTER TABLE ONLY exported_line_statistics
    ADD CONSTRAINT exported_line_statistics_pkey PRIMARY KEY (id);

CREATE SEQUENCE exported_line_statistics_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE exported_line_statistics_seq
    OWNER TO uttu;

ALTER TABLE exported_line_statistics
    OWNER TO uttu;

CREATE TABLE exported_day_type_statistics
(
    id                          bigint                      NOT NULL,
    exported_line_statistics_id bigint                      NOT NULL,
    day_type_netex_id           character varying(255)      NOT NULL,
    operating_period_from       timestamp without time zone NOT NULL,
    operating_period_to         timestamp without time zone NOT NULL
);

ALTER TABLE ONLY exported_day_type_statistics
    ADD CONSTRAINT exported_line_statistics_fkey FOREIGN KEY (exported_line_statistics_id) REFERENCES exported_line_statistics (id);

ALTER TABLE ONLY exported_day_type_statistics
    ADD CONSTRAINT exported_day_type_statistics_pkey PRIMARY KEY (id);

CREATE SEQUENCE exported_day_type_statistics_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE exported_day_type_statistics_seq
    OWNER TO uttu;

ALTER TABLE exported_day_type_statistics
    OWNER TO uttu;
