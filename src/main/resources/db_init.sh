#!/bin/bash  -e

for f in ./db/migration/V1__Base_version.sql \
           ./db/migration/V2__Add_Export_filename.sql \
           ./db/migration/V3__Remove_unique_order_constraints.sql \
           ./db/migration/V4__Increased_text_column_length.sql \
           ./db/migration/V5__Increased_booking_note_text_column_length.sql \
           ./import.sql
do
  echo "Running migration for ${f}"
  PGPASSWORD=uttu psql -U uttu -h localhost -p 5432 -f $f
done
