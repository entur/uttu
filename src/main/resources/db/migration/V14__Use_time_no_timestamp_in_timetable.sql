ALTER TABLE timetabled_passing_time
    ALTER COLUMN arrival_time TYPE time without time zone,
    ALTER COLUMN departure_time TYPE time without time zone,
    ALTER COLUMN latest_arrival_time TYPE time without time zone,
    ALTER COLUMN earliest_departure_time TYPE time without time zone;

ALTER TABLE booking_arrangement
    ALTER COLUMN latest_booking_time TYPE time without time zone;
