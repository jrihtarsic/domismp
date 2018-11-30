-- Copyright 2018 European Commission | CEF eDelivery
--
-- Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
-- You may not use this work except in compliance with the Licence.
--
-- You may obtain a copy of the Licence attached in file: LICENCE-EUPL-v1.2.pdf
--
-- Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

insert into SMP_USER (ID, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (1, 'smp_admin', '$2a$06$AXSSUDJlpzzq/gPZb7eIBeb8Mi0.PTKqDjzujZH.bWPwj5.ePEInW', 'SMP_ADMIN', 1,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_USER (ID, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (2, 'sg_admin', '$2a$06$AXSSUDJlpzzq/gPZb7eIBeb8Mi0.PTKqDjzujZH.bWPwj5.ePEInW', 'SERVICE_GROUP_ADMIN', 1,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_USER (ID, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (3, 'sys_admin', '$2a$06$AXSSUDJlpzzq/gPZb7eIBeb8Mi0.PTKqDjzujZH.bWPwj5.ePEInW', 'SYSTEM_ADMIN', 1,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());

insert into SMP_USER(ID, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (4, 'test_user_hashed_pass',                     '$2a$06$AXSSUDJlpzzq/gPZb7eIBeb8Mi0.PTKqDjzujZH.bWPwj5.ePEInW', 'SERVICE_GROUP_ADMIN',1,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_USER(ID, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (5, 'test_user_clear_pass',                      'test123',                                                     'SERVICE_GROUP_ADMIN',1,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_USER(ID, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (6, 'cert1', '',                                                             'SMP_ADMIN', 1,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_CERTIFICATE (ID, CERTIFICATE_ID, VALID_FROM, VALID_TO, CREATED_ON, LAST_UPDATED_ON) values (6, 'CN=comon name,O=org,C=BE:0000000000000066', null,null,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());

insert into SMP_USER(ID, USERNAME, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (7, 'cert2', 'SMP_ADMIN', 1,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_CERTIFICATE (ID, CERTIFICATE_ID, VALID_FROM, VALID_TO, CREATED_ON, LAST_UPDATED_ON) values (7, 'CN=EHEALTH_SMP_TEST_BRAZIL,O=European Commission,C=BE:48b681ee8e0dcc08', null,null,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());

insert into SMP_USER(ID, USERNAME, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (8, 'Cert3', 'SMP_ADMIN', 1,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_CERTIFICATE (ID, CERTIFICATE_ID, VALID_FROM, VALID_TO, CREATED_ON, LAST_UPDATED_ON) values (8, 'CN=utf-8_ż_SMP,O=EC,C=BE:0000000000000666', null,null,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());

insert into SMP_USER(ID, USERNAME, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (9, 'Cert4', 'SMP_ADMIN', 1,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_CERTIFICATE (ID, CERTIFICATE_ID, VALID_FROM, VALID_TO, CREATED_ON, LAST_UPDATED_ON) values (9, 'CN=EHEALTH_SMP_EC,O=European Commission,C=BEf71ee8b11cb3b787', null,null,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());


-- set the ids to higher values - tests are using sequnce which stars from 1
insert into SMP_SERVICE_GROUP(ID, PARTICIPANT_IDENTIFIER, PARTICIPANT_SCHEME, CREATED_ON, LAST_UPDATED_ON) values (100000, 'urn:australia:ncpb', 'ehealth-actorid-qns', CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_SERVICE_GROUP(ID, PARTICIPANT_IDENTIFIER, PARTICIPANT_SCHEME, CREATED_ON, LAST_UPDATED_ON) values (200000, 'urn:brazil:ncpb', 'ehealth-actorid-qns', CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());

-- set ownership
insert into SMP_OWNERSHIP (FK_SG_ID, FK_USER_ID) values (100000, 4);
insert into SMP_OWNERSHIP (FK_SG_ID, FK_USER_ID) values (200000, 2);

insert into SMP_DOMAIN (ID, DOMAIN_CODE, SML_SUBDOMAIN, SML_SMP_ID, SIGNATURE_KEY_ALIAS,SML_REGISTERED,SML_BLUE_COAT_AUTH, CREATED_ON, LAST_UPDATED_ON) values (1, 'domain','subdomain', 'CEF-SMP-001','single_domain_key',0,1,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());


insert into SMP_SERVICE_GROUP_DOMAIN (ID, FK_SG_ID, FK_DOMAIN_ID,SML_REGISTERED,  CREATED_ON, LAST_UPDATED_ON) values (1000,200000, 1, 0, CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());

insert into SMP_SERVICE_GROUP_DOMAIN (ID, FK_SG_ID, FK_DOMAIN_ID,SML_REGISTERED,  CREATED_ON, LAST_UPDATED_ON) values (1001,100000, 1, 0 ,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_SERVICE_METADATA (ID, FK_SG_DOM_ID, DOCUMENT_IDENTIFIER, DOCUMENT_SCHEME, LAST_UPDATED_ON, CREATED_ON)  values (1000,1001,'doc_7','busdox-docid-qns',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());


