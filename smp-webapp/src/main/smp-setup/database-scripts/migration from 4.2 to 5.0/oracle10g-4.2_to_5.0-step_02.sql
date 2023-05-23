-- copy data from backup
insert into SMP_ALERT (ID,CREATED_ON,LAST_UPDATED_ON,ALERT_LEVEL,ALERT_STATUS,ALERT_STATUS_DESC,ALERT_TYPE,MAIL_SUBJECT,MAIL_TO,PROCESSED_TIME,REPORTING_TIME,FOR_USERNAME )
    select ID,CREATED_ON,LAST_UPDATED_ON,ALERT_LEVEL,ALERT_STATUS,ALERT_STATUS_DESC,ALERT_TYPE,MAIL_SUBJECT,MAIL_TO,PROCESSED_TIME,REPORTING_TIME,FOR_USERNAME from BCK_ALERT;
insert into SMP_ALERT_PROPERTY (ID,CREATED_ON,LAST_UPDATED_ON,PROPERTY_NAME,PROPERTY_VALUE,FK_ALERT_ID)
    select ID,CREATED_ON,LAST_UPDATED_ON,PROPERTY,VALUE,FK_ALERT_ID from BCK_ALERT_PROPERTY;
-- properties
insert into SMP_CONFIGURATION ( PROPERTY_NAME, PROPERTY_VALUE, CREATED_ON, LAST_UPDATED_ON, DESCRIPTION)
    select PROPERTY, VALUE, CREATED_ON, LAST_UPDATED_ON, DESCRIPTION from BCK_CONFIGURATION;
-- users and credentials
insert into SMP_USER ( ID,USERNAME, CREATED_ON,LAST_UPDATED_ON,ACTIVE,APPLICATION_ROLE,EMAIL)
    select ID,USERNAME, CREATED_ON,LAST_UPDATED_ON,ACTIVE, DECODE(ROLE, 'SYSTEM_ADMIN','SYSTEM_ADMIN','USER'),EMAIL from BCK_USER;
-- set password credentials
insert into SMP_CREDENTIAL (ID, CREDENTIAL_NAME, CREDENTIAL_VALUE,FK_USER_ID,CREDENTIAL_TARGET,CREDENTIAL_TYPE,CREDENTIAL_ACTIVE, ACTIVE_FROM, EXPIRE_ON,CREATED_ON,LAST_UPDATED_ON, CHANGED_ON,LAST_ALERT_ON, LAST_FAILED_LOGIN_ON,LOGIN_FAILURE_COUNT)
select SMP_CREDENTIAL_SEQ.nextval, USERNAME, PASSWORD, id, 'UI', 'USERNAME_PASSWORD', 1, null, PASSWORD_EXPIRE_ON, CREATED_ON,LAST_UPDATED_ON, PASSWORD_CHANGED,PASSWORD_LAST_ALERT_ON,LAST_FAILED_LOGIN_ON, LOGIN_FAILURE_COUNT  from BCK_USER;
-- set access token
insert into SMP_CREDENTIAL (ID, CREDENTIAL_NAME, CREDENTIAL_VALUE,FK_USER_ID,CREDENTIAL_TARGET,CREDENTIAL_TYPE,CREDENTIAL_ACTIVE, ACTIVE_FROM, EXPIRE_ON,CREATED_ON,LAST_UPDATED_ON, CHANGED_ON,LAST_ALERT_ON, LAST_FAILED_LOGIN_ON,LOGIN_FAILURE_COUNT)
select SMP_CREDENTIAL_SEQ.nextval, ACCESS_TOKEN_ID, ACCESS_TOKEN, id, 'REST_API', 'ACCESS_TOKEN', 1, null, ACCESS_TOKEN_EXPIRE_ON, CREATED_ON,LAST_UPDATED_ON, ACCESS_TOKEN_GENERATED_ON,ACCESS_TOKEN_LAST_ALERT_ON,AT_LAST_FAILED_LOGIN_ON, AT_LOGIN_FAILURE_COUNT from BCK_USER WHERE ACCESS_TOKEN_ID is not null;
-- set certificates
insert into SMP_CREDENTIAL (ID, CREDENTIAL_NAME, CREDENTIAL_VALUE,FK_USER_ID,CREDENTIAL_TARGET,CREDENTIAL_TYPE,CREDENTIAL_ACTIVE, ACTIVE_FROM, EXPIRE_ON, CREATED_ON,LAST_UPDATED_ON, CHANGED_ON,LAST_ALERT_ON, LAST_FAILED_LOGIN_ON,LOGIN_FAILURE_COUNT)
    select SMP_CREDENTIAL_SEQ.nextval, CERTIFICATE_ID, null, id, 'REST_API', 'CERTIFICATE', 1, VALID_FROM, VALID_TO, CREATED_ON,LAST_UPDATED_ON, CREATED_ON, EXPIRE_LAST_ALERT_ON,null,null from BCK_CERTIFICATE;
insert into SMP_CERTIFICATE ( ID, CREATED_ON, LAST_UPDATED_ON, CERTIFICATE_ID, CRL_URL, ISSUER,PEM_ENCODED_CERT, SERIALNUMBER, SUBJECT, VALID_FROM, VALID_TO)
    select CRE.ID, CRT.CREATED_ON, CRT.LAST_UPDATED_ON, CRT.CERTIFICATE_ID, CRT.CRL_URL, CRT.ISSUER, CRT.PEM_ENCODED_CERT, CRT.SERIALNUMBER, CRT.SUBJECT, CRT.VALID_FROM, CRT.VALID_TO  from BCK_CERTIFICATE CRT JOIN SMP_CREDENTIAL CRE ON CRT.CERTIFICATE_ID=CRE.CREDENTIAL_NAME WHERE CRT.ID=CRE.FK_USER_ID AND CRE.CREDENTIAL_TYPE='CERTIFICATE';

-- register default extension
insert into SMP_EXTENSION ( ID, IDENTIFIER,  IMPLEMENTATION_NAME, NAME, VERSION, DESCRIPTION, CREATED_ON, LAST_UPDATED_ON) values
    (1, 'edelivery-oasis-smp-extension',  'OasisSMPExtension','Oasis SMP 1.0 and 2.0','1.0', 'Oasis SMP 1.0 and 2.0 extension',  sysdate,  sysdate);

insert into SMP_RESOURCE_DEF ( ID, FK_EXTENSION_ID, URL_SEGMENT, IDENTIFIER, DESCRIPTION, MIME_TYPE, NAME, CREATED_ON, LAST_UPDATED_ON) values
    (1, 1, 'smp-1', 'edelivery-oasis-smp-1.0-servicegroup', 'Oasis SMP 1.0 ServiceGroup', 'text/xml','Oasis SMP 1.0 ServiceGroup', sysdate,  sysdate);

insert into SMP_SUBRESOURCE_DEF (ID,FK_RESOURCE_DEF_ID,URL_SEGMENT, IDENTIFIER, DESCRIPTION, MIME_TYPE, NAME, CREATED_ON, LAST_UPDATED_ON) values
    (1,1, 'services', 'edelivery-oasis-smp-1.0-servicemetadata', 'Oasis SMP 1.0 ServiceMetadata', 'text/xml','Oasis SMP 1.0 ServiceMetadata', sysdate,  sysdate);

-- domains groups
-- the group has the same id as domain
insert into SMP_DOMAIN ( ID, CREATED_ON, LAST_UPDATED_ON, DOMAIN_CODE, SIGNATURE_ALGORITHM, SIGNATURE_DIGEST_METHOD, SIGNATURE_KEY_ALIAS,SML_CLIENT_KEY_ALIAS, SML_CLIENT_CERT_AUTH, SML_REGISTERED, SML_SMP_ID, SML_SUBDOMAIN, VISIBILITY)
    select ID,CREATED_ON,LAST_UPDATED_ON,DOMAIN_CODE, 'http://www.w3.org/2001/04/xmldsig-more#rsa-sha256', 'http://www.w3.org/2001/04/xmlenc#sha256',
    SIGNATURE_KEY_ALIAS,SML_CLIENT_KEY_ALIAS,SML_BLUE_COAT_AUTH,SML_REGISTERED,SML_SMP_ID,SML_SUBDOMAIN,'PUBLIC' from BCK_DOMAIN;
-- each domain has one default group and the id is equal to domain id. If this is changed fix also insert SMP_RESOURCE...
insert into SMP_GROUP ( ID, FK_DOMAIN_ID, NAME, DESCRIPTION, VISIBILITY, CREATED_ON, LAST_UPDATED_ON)
    select  ID, ID, DOMAIN_CODE || 'Group',  'Default Group for domain: ' ||  DOMAIN_CODE, 'PUBLIC', sysdate,  sysdate from BCK_DOMAIN;

-- set the ID of connection the same as id (can be done because it is empty database and one document for the domain) service group 1.0 document type to all domains
insert into SMP_DOMAIN_RESOURCE_DEF (ID, CREATED_ON, LAST_UPDATED_ON, FK_DOMAIN_ID, FK_RESOURCE_DEF_ID)
 select ID, sysdate,  sysdate,  ID, 1 from BCK_DOMAIN;


-- create service group documents
insert into SMP_DOCUMENT (ID, CREATED_ON, LAST_UPDATED_ON, CURRENT_VERSION,MIME_TYPE, NAME )
    select SMP_DOCUMENT_SEQ.nextval,sg.CREATED_ON, sg.LAST_UPDATED_ON, 1, 'text/xml', 'ServiceGroup1.0-' || sg.id || '-' ||  sgd.FK_DOMAIN_ID FROM BCK_SERVICE_GROUP sg JOIN BCK_SERVICE_GROUP_DOMAIN sgd on sg.id = sgd.FK_SG_ID;
-- the FK_DOREDEF_ID is 1  see the SMP_RESOURCE_DEF
-- the  FK_GROUP_ID is equal to domain id see the: insert into SMP_GROUP ....
insert into SMP_RESOURCE (ID, CREATED_ON, LAST_UPDATED_ON, IDENTIFIER_SCHEME, IDENTIFIER_VALUE, SML_REGISTERED, VISIBILITY, FK_DOCUMENT_ID, FK_DOREDEF_ID, FK_GROUP_ID)
    select SMP_RESOURCE_SEQ.nextval, sg.CREATED_ON, sg.LAST_UPDATED_ON, sg.PARTICIPANT_SCHEME, sg.PARTICIPANT_IDENTIFIER, sgd.SML_REGISTERED,
    'PUBLIC', (select id from SMP_DOCUMENT where NAME= 'ServiceGroup1.0-' || sg.id || '-' ||  sgd.FK_DOMAIN_ID ),
     1, sgd.FK_DOMAIN_ID FROM BCK_SERVICE_GROUP sg JOIN BCK_SERVICE_GROUP_DOMAIN sgd on sg.id = sgd.FK_SG_ID;

insert into SMP_DOCUMENT_VERSION ( ID, CREATED_ON, LAST_UPDATED_ON, DOCUMENT_CONTENT, VERSION, FK_DOCUMENT_ID )
    select SMP_DOCUMENT_VERSION_SEQ.nextval,  sg.CREATED_ON, sg.LAST_UPDATED_ON,  '<ServiceGroup xmlns="http://docs.oasis-open.org/bdxr/ns/SMP/2016/05" xmlns:ns0="http://www.w3.org/2000/09/xmldsig#"><ParticipantIdentifier scheme="' || sg.PARTICIPANT_SCHEME || '" >' || sg.PARTICIPANT_IDENTIFIER || '</ParticipantIdentifier><ServiceMetadataReferenceCollection />' || DECODE(DBMS_LOB.SUBSTR(ex.EXTENSION), null,'',DBMS_LOB.SUBSTR(ex.EXTENSION))  || '</ServiceGroup>', 1,
    (select id from SMP_DOCUMENT where NAME='ServiceGroup1.0-' || sg.id || '-' || sgd.FK_DOMAIN_ID )
    FROM BCK_SERVICE_GROUP sg JOIN BCK_SERVICE_GROUP_DOMAIN sgd on sg.id = sgd.FK_SG_ID LEFT JOIN BCK_SG_EXTENSION ex on sg.id = ex.id ;

-- ------------------------
-- create service metadata documents
insert into SMP_DOCUMENT (ID, CREATED_ON, LAST_UPDATED_ON, CURRENT_VERSION,MIME_TYPE, NAME )
    select SMP_DOCUMENT_SEQ.nextval, sm.CREATED_ON, sm.LAST_UPDATED_ON, 1, 'text/xml', 'ServiceMetadata1.0-' || sm.id FROM BCK_SERVICE_METADATA sm;

-- the FK_DOREDEF_ID is 1  see the SMP_RESOURCE_DEF
-- the  FK_GROUP_ID is equal to domain id see the: insert into SMP_GROUP ....
insert into SMP_SUBRESOURCE (ID, CREATED_ON, LAST_UPDATED_ON, IDENTIFIER_SCHEME, IDENTIFIER_VALUE, FK_DOCUMENT_ID, FK_RESOURCE_ID, FK_SUREDEF_ID)
    select SMP_SUBRESOURCE_SEQ.nextval, sm.CREATED_ON, sm.LAST_UPDATED_ON, sm.DOCUMENT_SCHEME, sm.DOCUMENT_IDENTIFIER,
    (select id from SMP_DOCUMENT where NAME='ServiceMetadata1.0-' || sm.id), sm.FK_SG_DOM_ID, 1 FROM BCK_SERVICE_METADATA sm;

insert into SMP_DOCUMENT_VERSION (ID,  CREATED_ON, LAST_UPDATED_ON, DOCUMENT_CONTENT, VERSION, FK_DOCUMENT_ID )
    select SMP_DOCUMENT_VERSION_SEQ.nextval, smx.CREATED_ON, smx.LAST_UPDATED_ON, smx.XML_CONTENT, 1,
    (select id from SMP_DOCUMENT where NAME='ServiceMetadata1.0-' || smx.id)
    FROM BCK_SERVICE_METADATA_XML smx ;

-- finally set also the memberships
-- the SMP admins are members of all domains/groups and resources
insert into SMP_DOMAIN_MEMBER (ID, CREATED_ON, LAST_UPDATED_ON, MEMBERSHIP_ROLE, FK_DOMAIN_ID, FK_USER_ID)
    select SMP_DOMAIN_MEMBER_SEQ.nextval, sysdate, sysdate, 'ADMIN', dmn.ID, usr.ID from BCK_USER  usr CROSS JOIN SMP_DOMAIN dmn where usr.ROLE='SMP_ADMIN' ;
insert into SMP_GROUP_MEMBER ( ID, CREATED_ON, LAST_UPDATED_ON, MEMBERSHIP_ROLE, FK_GROUP_ID, FK_USER_ID)
    select SMP_GROUP_MEMBER_SEQ.nextval,  sysdate, sysdate, 'ADMIN', grp.ID, usr.ID from BCK_USER  usr CROSS JOIN SMP_GROUP grp where usr.ROLE='SMP_ADMIN' ;
insert into SMP_RESOURCE_MEMBER ( ID, CREATED_ON, LAST_UPDATED_ON, MEMBERSHIP_ROLE, FK_RESOURCE_ID, FK_USER_ID)
    select SMP_RESOURCE_MEMBER_SEQ.nextval, sysdate, sysdate, 'ADMIN', rs.ID, usr.ID from BCK_USER  usr CROSS JOIN SMP_RESOURCE rs where usr.ROLE='SMP_ADMIN' ;

-- the SMP Resource memberships
insert into SMP_RESOURCE_MEMBER (ID, CREATED_ON, LAST_UPDATED_ON, MEMBERSHIP_ROLE, FK_RESOURCE_ID, FK_USER_ID)
    select SMP_RESOURCE_MEMBER_SEQ.nextval, sysdate, sysdate, 'ADMIN', ow.FK_SG_ID, usr.ID from BCK_USER usr JOIN BCK_OWNERSHIP ow ON  ow.FK_USER_ID = usr.id where usr.ROLE='SERVICE_GROUP_ADMIN';
