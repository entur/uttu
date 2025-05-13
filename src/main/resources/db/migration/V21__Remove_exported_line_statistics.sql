-- Drop the exported_day_type_statistics table first (it has a foreign key to exported_line_statistics)
DROP TABLE IF EXISTS exported_day_type_statistics;

-- Drop the exported_line_statistics table
DROP TABLE IF EXISTS exported_line_statistics;

-- Drop the sequences
DROP SEQUENCE IF EXISTS exported_day_type_statistics_seq;
DROP SEQUENCE IF EXISTS exported_line_statistics_seq;