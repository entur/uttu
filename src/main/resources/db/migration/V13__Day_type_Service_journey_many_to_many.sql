ALTER TABLE ONLY service_journey_day_types
    DROP CONSTRAINT uk_qdc46a69wd522j6oseb4fiv1a;

ALTER TABLE ONLY service_journey_day_types
    ADD CONSTRAINT unique_sj_dt_pks UNIQUE (day_types_pk, service_journey_pk);