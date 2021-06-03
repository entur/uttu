#!/bin/bash  -e

for f in ./db/migration/V1__Base_version.sql \
           ./db/migration/V2__Add_Export_filename.sql \
           ./db/migration/V3__Remove_unique_order_constraints.sql \
           ./db/migration/V4__Increased_text_column_length.sql \
           ./db/migration/V5__Increased_booking_note_text_column_length.sql \
           ./db/migration/V6__Add_simple_line.sql \
           ./db/migration/V7__Add_export_line_associations.sql \
           ./db/migration/V8__On_delete_cascade_line_fkey_in_export_line_association.sql \
           ./db/migration/V9__Add_key_list.sql \
           ./db/migration/V10__Drop_fromDate_to_Date_on_Export.sql \
           ./import.sql
do
  echo "Running migration for ${f}"
  PGPASSWORD=uttu psql -U uttu -h localhost -p 5432 -f $f
done
