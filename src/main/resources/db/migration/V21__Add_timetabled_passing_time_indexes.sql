
CREATE INDEX IF NOT EXISTS idx_timetabled_passing_time_sj ON timetabled_passing_time(service_journey_pk, order_val);

CREATE INDEX IF NOT EXISTS idx_timetabled_passing_time_covering ON timetabled_passing_time(service_journey_pk, order_val)
INCLUDE (
    arrival_time,
    departure_time,
    arrival_day_offset,
    departure_day_offset,
    earliest_departure_time,
    latest_arrival_time,
    earliest_departure_day_offset,
    latest_arrival_day_offset
);
