
insert into codespace values (nextval('codespace_seq'), now(),'test',now(),'test',0,'NSB','http://www.rutebanken.org/ns/nsb');
insert into provider values  (nextval('provider_seq'), now(),'test',now(),'test',0,'NSB',(select pk from codespace where xmlns='NSB'));
