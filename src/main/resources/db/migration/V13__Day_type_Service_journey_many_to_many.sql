ALTER TABLE ONLY service_journey_day_types
    DROP CONSTRAINT uk_qdc46a69wd522j6oseb4fiv1a;

ALTER TABLE ONLY service_journey_day_types
    ADD CONSTRAINT unique_sj_dt_pks2 UNIQUE (service_journey_pk, day_types_pk);

ALTER TABLE ONLY service_journey_day_types
    ADD PRIMARY KEY (service_journey_pk, day_types_pk);

ALTER TABLE ONLY day_type
    ADD COLUMN name character varying(255);