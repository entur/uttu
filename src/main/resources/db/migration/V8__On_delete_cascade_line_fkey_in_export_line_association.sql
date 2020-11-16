ALTER TABLE ONLY export_line_association
    DROP CONSTRAINT line_fkey,
    ADD CONSTRAINT line_fkey FOREIGN KEY (line_pk) REFERENCES line(pk) ON DELETE CASCADE;
