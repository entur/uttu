CREATE INDEX IF NOT EXISTS idx_service_journey_day_types_dt ON service_journey_day_types(day_types_pk);
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
CREATE INDEX IF NOT EXISTS idx_service_journey_journey_pattern ON service_journey(journey_pattern_pk);
CREATE INDEX IF NOT EXISTS idx_line_netex_id_provider ON line(netex_id, provider_pk);
CREATE INDEX IF NOT EXISTS idx_journey_pattern_netex_id_provider ON journey_pattern(netex_id, provider_pk);
CREATE INDEX IF NOT EXISTS idx_service_journey_netex_id_provider ON service_journey(netex_id, provider_pk);
CREATE INDEX IF NOT EXISTS idx_day_type_netex_id_provider ON day_type(netex_id, provider_pk);
CREATE INDEX IF NOT EXISTS idx_network_netex_id_provider ON network(netex_id, provider_pk);
CREATE INDEX IF NOT EXISTS idx_notice_netex_id_provider ON notice(netex_id, provider_pk);
CREATE INDEX IF NOT EXISTS idx_branding_netex_id_provider ON branding(netex_id, provider_pk);
CREATE INDEX IF NOT EXISTS idx_stop_point_in_journey_pattern_netex_id_provider ON stop_point_in_journey_pattern(netex_id, provider_pk);
CREATE INDEX IF NOT EXISTS idx_flexible_stop_place_netex_id_provider ON flexible_stop_place(netex_id, provider_pk);
CREATE INDEX IF NOT EXISTS idx_destination_display_netex_id_provider ON destination_display(netex_id, provider_pk);
CREATE INDEX IF NOT EXISTS idx_timetabled_passing_time_netex_id_provider ON timetabled_passing_time(netex_id, provider_pk);
CREATE INDEX IF NOT EXISTS idx_provider_code ON provider(code);
CREATE INDEX IF NOT EXISTS idx_day_type_netex_id ON day_type(netex_id);
CREATE INDEX IF NOT EXISTS idx_day_type_day_type_assignments_dt ON day_type_day_type_assignments(day_type_pk);
CREATE INDEX IF NOT EXISTS idx_day_type_days_of_week_dt ON day_type_days_of_week(day_type_pk);
CREATE INDEX IF NOT EXISTS idx_service_journey_notices_sj ON service_journey_notices(service_journey_pk);
CREATE INDEX IF NOT EXISTS idx_journey_pattern_notices_jp ON journey_pattern_notices(journey_pattern_pk);
CREATE INDEX IF NOT EXISTS idx_spijp_notices_spijp ON stop_point_in_journey_pattern_notices(stop_point_in_journey_pattern_pk);
