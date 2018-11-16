ALTER TABLE ONLY stop_point_in_journey_pattern
    drop CONSTRAINT stop_point_in_jp_unique_order_constraint ;


ALTER TABLE ONLY timetabled_passing_time
    drop CONSTRAINT timetabled_passing_time_unique_order_constraint;