
insert into code_space values (nextval('code_space_seq'), now(),'test',now(),'test',0,'NSB','http://www.rutebanken.org/ns/nsb');
insert into provider values  (nextval('provider_seq'), now(),'test',now(),'test',0,'NSB',(select pk from code_space where xmlns='NSB'));
