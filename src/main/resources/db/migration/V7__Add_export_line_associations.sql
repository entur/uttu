CREATE TABLE export_line_association (
   id bigint NOT NULL,
   export_pk bigint NOT NULL,
   line_pk bigint NOT NULL
);

ALTER TABLE ONLY export_line_association
    ADD CONSTRAINT line_fkey FOREIGN KEY (line_pk) REFERENCES line(pk),
    ADD CONSTRAINT export_fkey FOREIGN KEY (export_pk) REFERENCES export(pk);

CREATE SEQUENCE export_line_association_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE export_line_association_seq OWNER TO uttu;

ALTER TABLE export_line_association OWNER TO uttu;

