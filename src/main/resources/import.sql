
insert into codespace (pk,version, xmlns, xmlns_url,created,created_by,changed,changed_by) values (nextval('codespace_seq'), 1,'NSB','http://www.rutebanken.org/ns/nsb',now(),'test',now(),'test');
insert into codespace (pk,version, xmlns, xmlns_url,created,created_by,changed,changed_by)  values (nextval('codespace_seq'),1,'RUT','http://www.rutebanken.org/ns/rut', now(),'test',now(),'test');



insert into provider (pk,version,code, name, codespace_pk,created,created_by,changed,changed_by) values  (nextval('provider_seq'),1,'nsb', 'NSB',(select pk from codespace where xmlns='NSB'), now(),'test',now(),'test');
insert into provider (pk,version,code, name, codespace_pk,created,created_by,changed,changed_by) values  (nextval('provider_seq'),1, 'rut', 'Ruter',(select pk from codespace where xmlns='RUT'),now(),'test',now(),'test');