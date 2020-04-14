CREATE TABLE line (
   pk bigint NOT NULL,
   changed timestamp without time zone NOT NULL,
   changed_by character varying(255) NOT NULL,
   created timestamp without time zone NOT NULL,
   created_by character varying(255) NOT NULL,
   version bigint NOT NULL,
   netex_id character varying(255) NOT NULL,
   description character varying(255),
   name character varying(255),
   private_code character varying(255),
   short_name character varying(255),
   operator_ref character varying(255),
   public_code character varying(255),
   transport_mode character varying(255) NOT NULL,
   transport_submode character varying(255) NOT NULL,
   provider_pk bigint NOT NULL,
   network_pk bigint NOT NULL
);

ALTER TABLE line OWNER TO uttu;

INSERT INTO line SELECT pk, changed, changed_by, created, created_by, version, netex_id, description, name,
                        private_code, short_name, operator_ref, public_code, transport_mode, transport_submode,
                        provider_pk, network_pk FROM flexible_line;

ALTER TABLE ONLY line
    ADD CONSTRAINT line_pkey PRIMARY KEY (pk);

ALTER TABLE flexible_line
    DROP COLUMN changed,
    DROP COLUMN changed_by,
    DROP COLUMN created,
    DROP COLUMN created_by,
    DROP COLUMN version,
    DROP COLUMN netex_id,
    DROP COLUMN description,
    DROP COLUMN name,
    DROP COLUMN private_code,
    DROP COLUMN short_name,
    DROP COLUMN operator_ref,
    DROP COLUMN public_code,
    DROP COLUMN transport_mode,
    DROP COLUMN transport_submode,
    DROP COLUMN provider_pk,
    DROP COLUMN network_pk;

ALTER TABLE flexible_line
    ADD CONSTRAINT flexible_line_fkey FOREIGN KEY (pk) REFERENCES line(pk) ON DELETE CASCADE;

CREATE TABLE fixed_line (
    pk bigint NOT NULL
);

ALTER TABLE fixed_line OWNER TO uttu;

ALTER TABLE fixed_line
    ADD CONSTRAINT fixed_line_fkey FOREIGN KEY (pk) REFERENCES line(pk) ON DELETE CASCADE;

ALTER TABLE journey_pattern
    ADD COLUMN line_pk bigint;

UPDATE journey_pattern
    SET line_pk = flexible_line_pk;

ALTER TABLE journey_pattern
    ALTER COLUMN line_pk SET NOT NULL;

ALTER TABLE journey_pattern
    DROP COLUMN flexible_line_pk;

ALTER TABLE ONLY journey_pattern
    ADD CONSTRAINT journey_pattern_line_fk FOREIGN KEY (line_pk) REFERENCES line(pk);

ALTER TABLE ONLY line
    ADD CONSTRAINT line_network_fk FOREIGN KEY (network_pk) REFERENCES network(pk);

ALTER SEQUENCE flexible_line_seq RENAME TO line_seq;

CREATE TABLE line_notices (
   line_pk bigint NOT NULL,
   notices_pk bigint NOT NULL
);

ALTER TABLE line_notices OWNER TO uttu;

INSERT INTO line_notices SELECT flexible_line_pk as line_pk, notices_pk from flexible_line_notices;

DROP TABLE flexible_line_notices;

ALTER TABLE ONLY line_notices
    ADD CONSTRAINT line_notices_notices_fk FOREIGN KEY (notices_pk) REFERENCES notice(pk);

ALTER TABLE ONLY line_notices
    ADD CONSTRAINT line_notices_line_fk FOREIGN KEY (line_pk) REFERENCES line(pk);

ALTER TABLE ONLY line
    ADD CONSTRAINT line_unique_name_constraint UNIQUE (provider_pk, name);
