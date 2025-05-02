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
           ./db/migration/V11__Add_exported_line_statistics.sql \
           ./db/migration/V12__Add_service_journey_name.sql \
           ./db/migration/V13__Day_type_Service_journey_many_to_many.sql \
           ./db/migration/V14__Use_time_no_timestamp_in_timetable.sql \
           ./db/migration/V15__Add_line_type.sql \
           ./db/migration/V16__Flexible_stop_place_flexible_areas_one_to_many.sql \
           ./db/migration/V17__Flexible_area_key_valyes.sql \
           ./db/migration/V18__Create_branding_table.sql \
           ./db/migration/V19__Add_generate_service_links_to_export_table.sql \
           ./db/migration/V20__Add_indexes.sql \
           ./db/migration/V21__Remove_exported_line_statistics.sql
do
  echo "Running migration for ${f}"
  PGPASSWORD=uttu psql -U uttu -h localhost -p 5432 -f $f
done
