
insert into codespace (pk,version,xmlns,xmlns_url,created,created_by,changed,changed_by) values (nextval('codespace_seq'), 1,'TST','http://www.rutebanken.org/ns/tst',now(),'test',now(),'test');
insert into provider (pk,version,code,name,codespace_pk,created,created_by,changed,changed_by) values (nextval('provider_seq'),1,'tst', 'Test provider',(select pk from codespace where xmlns='TST'),now(),'test',now(),'test');
insert into codespace (pk,version,xmlns,xmlns_url,created,created_by,changed,changed_by) values (nextval('codespace_seq'), 1,'FOO','http://www.rutebanken.org/ns/foo',now(),'test',now(),'test');
insert into provider (pk,version,code,name,codespace_pk,created,created_by,changed,changed_by) values (nextval('provider_seq'),1,'foo', 'Foo provider', (select pk from codespace where xmlns='FOO'),now(),'test',now(),'test');
