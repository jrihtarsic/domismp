insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ACCESS_TOKEN_ID, ACCESS_TOKEN, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (1, 'peppol_user@test-mail.eu','peppol_user', '$2a$10$.pqNZZ4fRDdNbLhNlnEYg.1/d4yAGpLDgeXpJFI0sw7.WtyKphFzu','peppol_user', '$2a$10$.pqNZZ4fRDdNbLhNlnEYg.1/d4yAGpLDgeXpJFI0sw7.WtyKphFzu', 'SMP_ADMIN', 1, NOW(), NOW());
insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ACCESS_TOKEN_ID, ACCESS_TOKEN, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (2, 'the_admin@test-mail.eu','the_admin', '','the_admin', '', 'SMP_ADMIN', 1, NOW(), NOW());
insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ACCESS_TOKEN_ID, ACCESS_TOKEN, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (3, 'AdminSMP1TEST@test-mail.eu','AdminSMP1TEST', '$2a$06$u6Hym7Zrbsf4gEIeAsJRceK.Kg7tei3kDypwucQQdky0lXOLCkrCO','AdminSMP1TEST', '$2a$06$u6Hym7Zrbsf4gEIeAsJRceK.Kg7tei3kDypwucQQdky0lXOLCkrCO', 'SMP_ADMIN', 1, NOW(), NOW());
insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ACCESS_TOKEN_ID, ACCESS_TOKEN, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (4, 'AdminSMP2TEST@test-mail.eu','AdminSMP2TEST', '$2a$10$h8Q3Kjbs6ZrGkU6ditjNueINlJOMDJ/g/OKiqFZy32WmdhLjV5TAi','AdminSMP2TEST', '$2a$10$h8Q3Kjbs6ZrGkU6ditjNueINlJOMDJ/g/OKiqFZy32WmdhLjV5TAi', 'SMP_ADMIN', 1, NOW(), NOW());
insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ACCESS_TOKEN_ID, ACCESS_TOKEN, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (5, 'test@test-mail.eu','test', '','test', '',  'SMP_ADMIN', 1, NOW(), NOW());
insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ACCESS_TOKEN_ID, ACCESS_TOKEN, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (6, 'test1@test-mail.eu','test1', '$2a$06$toKXJgjqQINZdjQqSao3NeWz2n1S64PFPhVU1e8gIHh4xdbwzy1Uy','test1', '$2a$06$toKXJgjqQINZdjQqSao3NeWz2n1S64PFPhVU1e8gIHh4xdbwzy1Uy', 'SMP_ADMIN', 1, NOW(), NOW());
insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ACCESS_TOKEN_ID, ACCESS_TOKEN, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (7, 'system@test-mail.eu','system', '$2a$06$FDmjewn/do3C219uysNm9.XG8mIn.ubHnMydAzC8lsv61HsRpOR36','system', '$2a$06$FDmjewn/do3C219uysNm9.XG8mIn.ubHnMydAzC8lsv61HsRpOR36', 'SYSTEM_ADMIN', 1, NOW(), NOW());
insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ACCESS_TOKEN_ID, ACCESS_TOKEN, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (8, null,'smp', '$2a$06$FDmjewn/do3C219uysNm9.XG8mIn.ubHnMydAzC8lsv61HsRpOR36','smp', '$2a$06$FDmjewn/do3C219uysNm9.XG8mIn.ubHnMydAzC8lsv61HsRpOR36', 'SMP_ADMIN', 1, NOW(), NOW());
insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ACCESS_TOKEN_ID, ACCESS_TOKEN, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (9, 'user@test-mail.eu','user', '$2a$06$FDmjewn/do3C219uysNm9.XG8mIn.ubHnMydAzC8lsv61HsRpOR36','user', '$2a$06$FDmjewn/do3C219uysNm9.XG8mIn.ubHnMydAzC8lsv61HsRpOR36', 'SERVICE_GROUP_ADMIN', 1, NOW(), NOW());


insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (10,'EHEALTH_SMP_EC@test-mail.eu', 'EHEALTH_SMP_EC', '', 'SERVICE_GROUP_ADMIN', 1, NOW(), NOW());
insert into SMP_CERTIFICATE (ID, CERTIFICATE_ID, VALID_FROM, VALID_TO, CREATED_ON, LAST_UPDATED_ON) values (10, 'CN=EHEALTH_SMP_EC,O=European Commission,C=BE:f71ee8b11cb3b787', date_add(NOW(),interval -1 year), date_add(NOW(),interval 1 year), NOW(), NOW());

insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (11,'EHEALTH_z_ẞ_W@test-mail.eu', 'EHEALTH_ż_ẞ_Ẅ_,O', '', 'SMP_ADMIN', 1, NOW(), NOW());
insert into SMP_CERTIFICATE (ID, CERTIFICATE_ID, VALID_FROM, VALID_TO, CREATED_ON, LAST_UPDATED_ON) values (11, 'CN=EHEALTH_z_ẞ_W_,O=European_z_ẞ_W_Commission,C=BE:f71ee8b11cb3b787', null,null, NOW(), NOW());

insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (12,'EHEALTH_SMP_1000000007-1@test-mail.eu', 'EHEALTH_SMP_1000000007-1', '', 'SERVICE_GROUP_ADMIN', 1, NOW(), NOW());
insert into SMP_CERTIFICATE (ID, CERTIFICATE_ID, VALID_FROM, VALID_TO, CREATED_ON, LAST_UPDATED_ON) values (12, 'CN=EHEALTH_SMP_1000000007,O=DG-DIGIT,C=BE:000000000123ABCD', null,null, NOW(), NOW());


insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (14,'EHEALTH_SMP_1000000007-2@test-mail.eu', 'EHEALTH_SMP_1000000007-2', '', 'SMP_ADMIN', 1, NOW(), NOW());
insert into SMP_CERTIFICATE (ID, CERTIFICATE_ID, VALID_FROM, VALID_TO, CREATED_ON, LAST_UPDATED_ON) values (14, 'CN=EHEALTH_SMP_1000000007,O=DG-DIGIT,C=BE', null,null, NOW(), NOW());

insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (15,'EHEALTH-SMP_EC@test-mail.eu', 'EHEALTH&SMP_EC', '', 'SMP_ADMIN', 1, NOW(), NOW());
insert into SMP_CERTIFICATE (ID, CERTIFICATE_ID, VALID_FROM, VALID_TO, CREATED_ON, LAST_UPDATED_ON) values (15, 'CN=EHEALTH&SMP_EC,O=European&Commission,C=B&E:f71ee8b11cb3b787', null,null, NOW(), NOW());

insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (16,'EHEALTH_SMP_EC2@test-mail.eu', 'EHEALTH_SMP_EC2', '', 'SMP_ADMIN', 1, NOW(), NOW());
insert into SMP_CERTIFICATE (ID, CERTIFICATE_ID, VALID_FROM, VALID_TO, CREATED_ON, LAST_UPDATED_ON) values (16, 'CN=EHEALTH_SMP_EC,O=European Commission,C=BE:000000000000100f', null,null, NOW(), NOW());

insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (17,'SMP_1000000007-3@test-mail.eu', 'SMP_1000000007-3', '', 'SMP_ADMIN', 1, NOW(), NOW());
insert into SMP_CERTIFICATE (ID, CERTIFICATE_ID, VALID_FROM, VALID_TO, CREATED_ON, LAST_UPDATED_ON) values (17, 'CN=SMP_1000000007,O=DG-DIGIT,C=BE', null,null, NOW(), NOW());

insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (18,'SMP_1000000007-4@test-mail.eu', 'SMP_1000000007-4', '', 'SMP_ADMIN', 1, NOW(), NOW());
insert into SMP_CERTIFICATE (ID, CERTIFICATE_ID, VALID_FROM, VALID_TO, CREATED_ON, LAST_UPDATED_ON) values (18, 'CN=SMP_1000000007,O=DG-DIGIT,C=BE:000000000123ABCD', null,null, NOW(), NOW());

insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (19,'SMP_1000000181@test-mail.eu', 'SMP_1000000181,O=DIGIT,C=DK:123456789', '$2a$10$v2d/2E99dWHBM2ipTIip1enyaRKBTi.Xj/Iz0K8g0gjHBWdKRsHaC', 'SMP_ADMIN', 1, NOW(), NOW());
insert into SMP_CERTIFICATE (ID, CERTIFICATE_ID, VALID_FROM, VALID_TO, CREATED_ON, LAST_UPDATED_ON) values (19, 'CN=SMP_1000000181,O=DIGIT,C=DK:123456789', null,null, NOW(), NOW());

insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (20,'red_gw@test-mail.eu', 'red_gw', '', 'SERVICE_GROUP_ADMIN', 1, NOW(), NOW());
insert into SMP_CERTIFICATE (ID, CERTIFICATE_ID, VALID_FROM, VALID_TO, CREATED_ON, LAST_UPDATED_ON) values (20, 'CN=red_gw,O=eDelivery,C=BE:9792CE69BC89F14C', null,null, NOW(), NOW());

insert into SMP_USER (ID, EMAIL, USERNAME, PASSWORD, ROLE, ACTIVE, CREATED_ON, LAST_UPDATED_ON) values (21,'blue_gw@test-mail.eu', 'blue_gw', '', 'SERVICE_GROUP_ADMIN', 1, NOW(), NOW());
insert into SMP_CERTIFICATE (ID, CERTIFICATE_ID, VALID_FROM, VALID_TO, CREATED_ON, LAST_UPDATED_ON) values (21, 'CN=blue_gw,O=eDelivery,C=BE:e07b6b956330a19a', null,null, NOW(), NOW());

-- insert domain
insert into SMP_DOMAIN (ID, DOMAIN_CODE, SML_SUBDOMAIN, SML_SMP_ID, SIGNATURE_KEY_ALIAS, SML_CLIENT_KEY_ALIAS, SML_BLUE_COAT_AUTH,SML_REGISTERED, CREATED_ON, LAST_UPDATED_ON, SML_CLIENT_CERT_HEADER)
values (1, 'domain','','CEF-SMP-001', 'sample_key','sample_key', 1,1, NOW(), NOW(),'sno=1&subject=CN=SMP_TEST-PRE-SET-EXAMPLE, OU=eDelivery, O=DIGITAL, C=BE&validfrom=Dec 6 17:41:42 2016 GMT&validto=Jul 9 23:59:00 2050 GMT&issuer=CN=rootCNTest,OU=B4,O=DIGIT,L=Brussels,ST=BE,C=BE');

insert into SMP_DOMAIN (ID, DOMAIN_CODE, SML_SUBDOMAIN, SML_SMP_ID, SIGNATURE_KEY_ALIAS, SML_BLUE_COAT_AUTH,SML_REGISTERED, CREATED_ON, LAST_UPDATED_ON) values (2, 'domainB','subdomain002', 'CEF-SMP-002','sample_key',1,0, CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_DOMAIN (ID, DOMAIN_CODE, SML_SUBDOMAIN, SML_SMP_ID, SIGNATURE_KEY_ALIAS, SML_BLUE_COAT_AUTH,SML_REGISTERED, CREATED_ON, LAST_UPDATED_ON) values (3, 'domainC','subdomain003', 'CEF-SMP-003','sample_key',1,0, CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_DOMAIN (ID, DOMAIN_CODE, SML_SUBDOMAIN, SML_SMP_ID, SIGNATURE_KEY_ALIAS, SML_BLUE_COAT_AUTH,SML_REGISTERED, CREATED_ON, LAST_UPDATED_ON) values (4, 'domainD','subdomain004', 'CEF-SMP-004','sample_key',1,0, CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_DOMAIN (ID, DOMAIN_CODE, SML_SUBDOMAIN, SML_SMP_ID, SIGNATURE_KEY_ALIAS, SML_BLUE_COAT_AUTH,SML_REGISTERED, CREATED_ON, LAST_UPDATED_ON) values (5, 'domainE','subdomain005', 'CEF-SMP-005','sample_key',1,0, CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_DOMAIN (ID, DOMAIN_CODE, SML_SUBDOMAIN, SML_SMP_ID, SIGNATURE_KEY_ALIAS, SML_BLUE_COAT_AUTH,SML_REGISTERED, CREATED_ON, LAST_UPDATED_ON) values (6, 'domainF','subdomain006', 'CEF-SMP-006','sample_key',1,0, CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_DOMAIN (ID, DOMAIN_CODE, SML_SUBDOMAIN, SML_SMP_ID, SIGNATURE_KEY_ALIAS, SML_BLUE_COAT_AUTH,SML_REGISTERED, CREATED_ON, LAST_UPDATED_ON) values (7, 'domainG','subdomain007', 'CEF-SMP-007','sample_key',1,0, CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_DOMAIN (ID, DOMAIN_CODE, SML_SUBDOMAIN, SML_SMP_ID, SIGNATURE_KEY_ALIAS, SML_BLUE_COAT_AUTH,SML_REGISTERED, CREATED_ON, LAST_UPDATED_ON) values (8, 'domainH','subdomain008', 'CEF-SMP-008','sample_key',1,0, CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_DOMAIN (ID, DOMAIN_CODE, SML_SUBDOMAIN, SML_SMP_ID, SIGNATURE_KEY_ALIAS, SML_BLUE_COAT_AUTH,SML_REGISTERED, CREATED_ON, LAST_UPDATED_ON) values (9, 'domainI','subdomain009', 'CEF-SMP-009','sample_key',1,0, CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_DOMAIN (ID, DOMAIN_CODE, SML_SUBDOMAIN, SML_SMP_ID, SIGNATURE_KEY_ALIAS, SML_BLUE_COAT_AUTH,SML_REGISTERED, CREATED_ON, LAST_UPDATED_ON) values (10, 'domainJ','subdomain010', 'CEF-SMP-010','sample_key',1,0,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_DOMAIN (ID, DOMAIN_CODE, SML_SUBDOMAIN, SML_SMP_ID, SIGNATURE_KEY_ALIAS, SML_BLUE_COAT_AUTH,SML_REGISTERED, CREATED_ON, LAST_UPDATED_ON) values (11, 'domainK','subdomain011', 'CEF-SMP-011','sample_key',1,0,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
insert into SMP_DOMAIN (ID, DOMAIN_CODE, SML_SUBDOMAIN, SML_SMP_ID, SIGNATURE_KEY_ALIAS, SML_BLUE_COAT_AUTH,SML_REGISTERED, CREATED_ON, LAST_UPDATED_ON) values (12, 'domainL','subdomain012', 'CEF-SMP-012','sample_key',1,0,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());

