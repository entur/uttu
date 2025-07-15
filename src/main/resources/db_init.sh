#!/bin/bash  -e

SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_PATH/db/migration"

for f in V1__Base_version.sql \
         V2__Add_Export_filename.sql \
         V3__Remove_unique_order_constraints.sql \
         V4__Increased_text_column_length.sql \
         V5__Increased_booking_note_text_column_length.sql \
         V6__Add_simple_line.sql \
         V7__Add_export_line_associations.sql \
         V8__On_delete_cascade_line_fkey_in_export_line_association.sql \
         V9__Add_key_list.sql \
         V10__Drop_fromDate_to_Date_on_Export.sql \
         V11__Add_exported_line_statistics.sql \
         V12__Add_service_journey_name.sql \
         V13__Day_type_Service_journey_many_to_many.sql \
         V14__Use_time_no_timestamp_in_timetable.sql \
         V15__Add_line_type.sql \
         V16__Flexible_stop_place_flexible_areas_one_to_many.sql \
         V17__Flexible_area_key_valyes.sql \
         V18__Create_branding_table.sql \
         V19__Add_generate_service_links_to_export_table.sql \
         V20__Add_indexes.sql \
         V21__Remove_exported_line_statistics.sql
do
  echo "Running migration for ${f}"
  PGPASSWORD=uttu psql -U uttu -h localhost -p 5432 -f $f
done
