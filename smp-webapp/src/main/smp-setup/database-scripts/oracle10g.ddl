-- ------------------------------------------------------------------------
-- This file was generated by hibernate for SMP version 4.2-SNAPSHOT.
-- ------------------------------------------------------------------------

create sequence SMP_DOMAIN_SEQ start with 1 increment by  1;
create sequence SMP_REVISION_SEQ start with 1 increment by  1;
create sequence SMP_SERVICE_GROUP_DOMAIN_SEQ start with 1 increment by  1;
create sequence SMP_SERVICE_GROUP_SEQ start with 1 increment by  1;
create sequence SMP_SERVICE_METADATA_SEQ start with 1 increment by  1;
create sequence SMP_USER_SEQ start with 1 increment by  1;

    create table SMP_CERTIFICATE (
       ID number(19,0) not null,
        CERTIFICATE_ID varchar2(1024 char),
        CREATED_ON timestamp not null,
        CRL_URL varchar2(4000 char),
        ISSUER varchar2(1024 char),
        LAST_UPDATED_ON timestamp not null,
        PEM_ENCODED_CERT clob,
        SERIALNUMBER varchar2(128 char),
        SUBJECT varchar2(1024 char),
        VALID_FROM timestamp,
        VALID_TO timestamp,
        primary key (ID)
    );

    comment on table SMP_CERTIFICATE is
        'SMP user certificates';

    comment on column SMP_CERTIFICATE.ID is
        'Shared primary key with master table SMP_USER';

    comment on column SMP_CERTIFICATE.CERTIFICATE_ID is
        'Formatted Certificate id using tags: cn, o, c:serialNumber';

    comment on column SMP_CERTIFICATE.CRL_URL is
        'URL to the certificate revocation list (CRL)';

    comment on column SMP_CERTIFICATE.ISSUER is
        'Certificate issuer (canonical form)';

    comment on column SMP_CERTIFICATE.PEM_ENCODED_CERT is
        'PEM encoded  certificate';

    comment on column SMP_CERTIFICATE.SERIALNUMBER is
        'Certificate serial number';

    comment on column SMP_CERTIFICATE.SUBJECT is
        'Certificate subject (canonical form)';

    comment on column SMP_CERTIFICATE.VALID_FROM is
        'Certificate valid from date.';

    comment on column SMP_CERTIFICATE.VALID_TO is
        'Certificate valid to date.';

    create table SMP_CERTIFICATE_AUD (
       ID number(19,0) not null,
        REV number(19,0) not null,
        REVTYPE number(3,0),
        CERTIFICATE_ID varchar2(1024 char),
        CREATED_ON timestamp,
        CRL_URL varchar2(4000 char),
        ISSUER varchar2(1024 char),
        LAST_UPDATED_ON timestamp,
        PEM_ENCODED_CERT clob,
        SERIALNUMBER varchar2(128 char),
        SUBJECT varchar2(1024 char),
        VALID_FROM timestamp,
        VALID_TO timestamp,
        primary key (ID, REV)
    );

    create table SMP_CONFIGURATION (
       PROPERTY varchar2(512 char) not null,
        CREATED_ON timestamp not null,
        DESCRIPTION varchar2(4000 char),
        LAST_UPDATED_ON timestamp not null,
        VALUE varchar2(4000 char),
        primary key (PROPERTY)
    );

    comment on table SMP_CONFIGURATION is
        'SMP user certificates';

    comment on column SMP_CONFIGURATION.PROPERTY is
        'Property name/key';

    comment on column SMP_CONFIGURATION.CREATED_ON is
        'Row inserted on date';

    comment on column SMP_CONFIGURATION.DESCRIPTION is
        'Property description';

    comment on column SMP_CONFIGURATION.LAST_UPDATED_ON is
        'Row modified on date';

    comment on column SMP_CONFIGURATION.VALUE is
        'Property value';

    create table SMP_DOMAIN (
       ID number(19,0) not null,
        CREATED_ON timestamp not null,
        DOMAIN_CODE varchar2(256 char) not null,
        LAST_UPDATED_ON timestamp not null,
        SIGNATURE_KEY_ALIAS varchar2(256 char),
        SML_BLUE_COAT_AUTH number(1,0) not null,
        SML_CLIENT_CERT_HEADER varchar2(4000 char),
        SML_CLIENT_KEY_ALIAS varchar2(256 char),
        SML_PARTC_IDENT_REGEXP varchar2(4000 char),
        SML_REGISTERED number(1,0) not null,
        SML_SMP_ID varchar2(256 char),
        SML_SUBDOMAIN varchar2(256 char),
        primary key (ID)
    );

    comment on table SMP_DOMAIN is
        'SMP can handle multiple domains. This table contains domain specific data';

    comment on column SMP_DOMAIN.ID is
        'Unique domain id';

    comment on column SMP_DOMAIN.DOMAIN_CODE is
        'Domain code used as http parameter in rest webservices';

    comment on column SMP_DOMAIN.SIGNATURE_KEY_ALIAS is
        'Signature key alias used for SML integration';

    comment on column SMP_DOMAIN.SML_BLUE_COAT_AUTH is
        'Flag for SML authentication type - use CLientCert header or  HTTPS ClientCertificate (key)';

    comment on column SMP_DOMAIN.SML_CLIENT_CERT_HEADER is
        'Client-Cert header used behind RP - BlueCoat for SML integration';

    comment on column SMP_DOMAIN.SML_CLIENT_KEY_ALIAS is
        'Client key alias used for SML integration';

    comment on column SMP_DOMAIN.SML_PARTC_IDENT_REGEXP is
        'Reqular expresion for participant ids';

    comment on column SMP_DOMAIN.SML_REGISTERED is
        'Flag for: Is domain registered in SML';

    comment on column SMP_DOMAIN.SML_SMP_ID is
        'SMP ID used for SML integration';

    comment on column SMP_DOMAIN.SML_SUBDOMAIN is
        'SML subdomain';

    create table SMP_DOMAIN_AUD (
       ID number(19,0) not null,
        REV number(19,0) not null,
        REVTYPE number(3,0),
        CREATED_ON timestamp,
        DOMAIN_CODE varchar2(256 char),
        LAST_UPDATED_ON timestamp,
        SIGNATURE_KEY_ALIAS varchar2(256 char),
        SML_BLUE_COAT_AUTH number(1,0),
        SML_CLIENT_CERT_HEADER varchar2(4000 char),
        SML_CLIENT_KEY_ALIAS varchar2(256 char),
        SML_PARTC_IDENT_REGEXP varchar2(4000 char),
        SML_REGISTERED number(1,0),
        SML_SMP_ID varchar2(256 char),
        SML_SUBDOMAIN varchar2(256 char),
        primary key (ID, REV)
    );

    create table SMP_OWNERSHIP (
       FK_SG_ID number(19,0) not null,
        FK_USER_ID number(19,0) not null,
        primary key (FK_SG_ID, FK_USER_ID)
    );

    create table SMP_OWNERSHIP_AUD (
       REV number(19,0) not null,
        FK_SG_ID number(19,0) not null,
        FK_USER_ID number(19,0) not null,
        REVTYPE number(3,0),
        primary key (REV, FK_SG_ID, FK_USER_ID)
    );

    create table SMP_REV_INFO (
       id number(19,0) not null,
        REVISION_DATE timestamp,
        timestamp number(19,0) not null,
        USERNAME varchar2(255 char),
        primary key (id)
    );

    create table SMP_SERVICE_GROUP (
       ID number(19,0) not null,
        CREATED_ON timestamp not null,
        LAST_UPDATED_ON timestamp not null,
        PARTICIPANT_IDENTIFIER varchar2(256 char) not null,
        PARTICIPANT_SCHEME varchar2(256 char) not null,
        primary key (ID)
    );

    comment on table SMP_SERVICE_GROUP is
        'Service group data - Identifier and scheme';

    comment on column SMP_SERVICE_GROUP.ID is
        'Unique Servicegroup id';

    create table SMP_SERVICE_GROUP_AUD (
       ID number(19,0) not null,
        REV number(19,0) not null,
        REVTYPE number(3,0),
        CREATED_ON timestamp,
        LAST_UPDATED_ON timestamp,
        PARTICIPANT_IDENTIFIER varchar2(256 char),
        PARTICIPANT_SCHEME varchar2(256 char),
        primary key (ID, REV)
    );

    create table SMP_SERVICE_GROUP_DOMAIN (
       ID number(19,0) not null,
        CREATED_ON timestamp not null,
        LAST_UPDATED_ON timestamp not null,
        SML_REGISTERED number(1,0) not null,
        FK_DOMAIN_ID number(19,0),
        FK_SG_ID number(19,0),
        primary key (ID)
    );

    create table SMP_SERVICE_GROUP_DOMAIN_AUD (
       ID number(19,0) not null,
        REV number(19,0) not null,
        REVTYPE number(3,0),
        CREATED_ON timestamp,
        LAST_UPDATED_ON timestamp,
        SML_REGISTERED number(1,0),
        FK_DOMAIN_ID number(19,0),
        FK_SG_ID number(19,0),
        primary key (ID, REV)
    );

    create table SMP_SERVICE_METADATA (
       ID number(19,0) not null,
        CREATED_ON timestamp not null,
        DOCUMENT_IDENTIFIER varchar2(500 char) not null,
        DOCUMENT_SCHEME varchar2(500 char),
        LAST_UPDATED_ON timestamp not null,
        FK_SG_DOM_ID number(19,0) not null,
        primary key (ID)
    );

    comment on table SMP_SERVICE_METADATA is
        'Service metadata';

    comment on column SMP_SERVICE_METADATA.ID is
        'Shared primary key with master table SMP_SERVICE_METADATA';

    create table SMP_SERVICE_METADATA_AUD (
       ID number(19,0) not null,
        REV number(19,0) not null,
        REVTYPE number(3,0),
        CREATED_ON timestamp,
        DOCUMENT_IDENTIFIER varchar2(500 char),
        DOCUMENT_SCHEME varchar2(500 char),
        LAST_UPDATED_ON timestamp,
        FK_SG_DOM_ID number(19,0),
        primary key (ID, REV)
    );

    create table SMP_SERVICE_METADATA_XML (
       ID number(19,0) not null,
        CREATED_ON timestamp not null,
        LAST_UPDATED_ON timestamp not null,
        XML_CONTENT blob,
        primary key (ID)
    );

    comment on table SMP_SERVICE_METADATA_XML is
        'Service group metadata xml blob';

    comment on column SMP_SERVICE_METADATA_XML.XML_CONTENT is
        'XML service metadata ';

    create table SMP_SERVICE_METADATA_XML_AUD (
       ID number(19,0) not null,
        REV number(19,0) not null,
        REVTYPE number(3,0),
        CREATED_ON timestamp,
        LAST_UPDATED_ON timestamp,
        XML_CONTENT blob,
        primary key (ID, REV)
    );

    create table SMP_SG_EXTENSION (
       ID number(19,0) not null,
        CREATED_ON timestamp not null,
        EXTENSION blob,
        LAST_UPDATED_ON timestamp not null,
        primary key (ID)
    );

    comment on table SMP_SG_EXTENSION is
        'Service group extension blob';

    comment on column SMP_SG_EXTENSION.EXTENSION is
        'XML extension(s) for servicegroup ';

    create table SMP_SG_EXTENSION_AUD (
       ID number(19,0) not null,
        REV number(19,0) not null,
        REVTYPE number(3,0),
        CREATED_ON timestamp,
        EXTENSION blob,
        LAST_UPDATED_ON timestamp,
        primary key (ID, REV)
    );

    create table SMP_USER (
       ID number(19,0) not null,
        ACCESS_TOKEN varchar2(256 char),
        PAT_GENERATED timestamp,
        ACCESS_TOKEN_ID varchar2(256 char),
        ACTIVE number(1,0) not null,
        CREATED_ON timestamp not null,
        EMAIL varchar2(256 char),
        LAST_UPDATED_ON timestamp not null,
        PASSWORD varchar2(256 char),
        PASSWORD_CHANGED timestamp,
        ROLE varchar2(256 char),
        USERNAME varchar2(256 char),
        primary key (ID)
    );

    comment on table SMP_USER is
        'SMP can handle multiple domains. This table contains domain specific data';

    comment on column SMP_USER.ID is
        'Unique user id';

    comment on column SMP_USER.ACCESS_TOKEN is
        'BCrypted personal access token';

    comment on column SMP_USER.PAT_GENERATED is
        'Date when personal access token was generated';

    comment on column SMP_USER.ACCESS_TOKEN_ID is
        'Personal access token id';

    comment on column SMP_USER.ACTIVE is
        'Is user active';

    comment on column SMP_USER.EMAIL is
        'User email';

    comment on column SMP_USER.PASSWORD is
        'BCrypted password for username/password login';

    comment on column SMP_USER.PASSWORD_CHANGED is
        'Last date when password was changed';

    comment on column SMP_USER.ROLE is
        'User role';

    comment on column SMP_USER.USERNAME is
        'Login username';

    create table SMP_USER_AUD (
       ID number(19,0) not null,
        REV number(19,0) not null,
        REVTYPE number(3,0),
        ACCESS_TOKEN varchar2(256 char),
        PAT_GENERATED timestamp,
        ACCESS_TOKEN_ID varchar2(256 char),
        ACTIVE number(1,0),
        CREATED_ON timestamp,
        EMAIL varchar2(256 char),
        LAST_UPDATED_ON timestamp,
        PASSWORD varchar2(256 char),
        PASSWORD_CHANGED timestamp,
        ROLE varchar2(256 char),
        USERNAME varchar2(256 char),
        primary key (ID, REV)
    );

    alter table SMP_CERTIFICATE 
       add constraint UK_3x3rvf6hkim9fg16caurkgg6f unique (CERTIFICATE_ID);

    alter table SMP_DOMAIN 
       add constraint UK_djrwqd4luj5i7w4l7fueuaqbj unique (DOMAIN_CODE);

    alter table SMP_DOMAIN 
       add constraint UK_likb3jn0nlxlekaws0xx10uqc unique (SML_SUBDOMAIN);
create index SMP_SG_PART_ID_IDX on SMP_SERVICE_GROUP (PARTICIPANT_IDENTIFIER);
create index SMP_SG_PART_SCH_IDX on SMP_SERVICE_GROUP (PARTICIPANT_SCHEME);

    alter table SMP_SERVICE_GROUP 
       add constraint SMP_SG_UNIQ_PARTC_IDX unique (PARTICIPANT_SCHEME, PARTICIPANT_IDENTIFIER);
create index SMP_SMD_DOC_ID_IDX on SMP_SERVICE_METADATA (DOCUMENT_IDENTIFIER);
create index SMP_SMD_DOC_SCH_IDX on SMP_SERVICE_METADATA (DOCUMENT_SCHEME);

    alter table SMP_SERVICE_METADATA 
       add constraint SMP_MT_UNIQ_SG_DOC_IDX unique (FK_SG_DOM_ID, DOCUMENT_IDENTIFIER, DOCUMENT_SCHEME);

    alter table SMP_USER 
       add constraint UK_tk9bjsmd2mevgt3b997i6pl27 unique (ACCESS_TOKEN_ID);

    alter table SMP_USER 
       add constraint UK_rt1f0anklfo05lt0my05fqq6 unique (USERNAME);

    alter table SMP_CERTIFICATE 
       add constraint FKayqgpj5ot3o8vrpduul7sstta 
       foreign key (ID) 
       references SMP_USER;

    alter table SMP_CERTIFICATE_AUD 
       add constraint FKnrwm8en8vv10li8ihwnurwd9e 
       foreign key (REV) 
       references SMP_REV_INFO;

    alter table SMP_DOMAIN_AUD 
       add constraint FK35qm8xmi74kfenugeonijodsg 
       foreign key (REV) 
       references SMP_REV_INFO;

    alter table SMP_OWNERSHIP 
       add constraint FKrnqwq06lbfwciup4rj8nvjpmy 
       foreign key (FK_USER_ID) 
       references SMP_USER;

    alter table SMP_OWNERSHIP 
       add constraint FKgexq5n6ftsid8ehqljvjh8p4i 
       foreign key (FK_SG_ID) 
       references SMP_SERVICE_GROUP;

    alter table SMP_OWNERSHIP_AUD 
       add constraint FK1lqynlbk8ow1ouxetf5wybk3k 
       foreign key (REV) 
       references SMP_REV_INFO;

    alter table SMP_SERVICE_GROUP_AUD 
       add constraint FKj3caimhegwyav1scpwrxoslef 
       foreign key (REV) 
       references SMP_REV_INFO;

    alter table SMP_SERVICE_GROUP_DOMAIN 
       add constraint FKo186xtefda6avl5p1tuqchp3n 
       foreign key (FK_DOMAIN_ID) 
       references SMP_DOMAIN;

    alter table SMP_SERVICE_GROUP_DOMAIN 
       add constraint FKgcvhnk2n34d3c6jhni5l3s3x3 
       foreign key (FK_SG_ID) 
       references SMP_SERVICE_GROUP;

    alter table SMP_SERVICE_GROUP_DOMAIN_AUD 
       add constraint FK6uc9r0eqw16baooxtmqjkih0j 
       foreign key (REV) 
       references SMP_REV_INFO;

    alter table SMP_SERVICE_METADATA 
       add constraint FKfvcml6b8x7kn80m30h8pxs7jl 
       foreign key (FK_SG_DOM_ID) 
       references SMP_SERVICE_GROUP_DOMAIN;

    alter table SMP_SERVICE_METADATA_AUD 
       add constraint FKbqr9pdnik1qxx2hi0xn4n7f61 
       foreign key (REV) 
       references SMP_REV_INFO;

    alter table SMP_SERVICE_METADATA_XML 
       add constraint FK4b1x06xlavcgbjnuilgksi7nm 
       foreign key (ID) 
       references SMP_SERVICE_METADATA;

    alter table SMP_SERVICE_METADATA_XML_AUD 
       add constraint FKevatmlvvwoxfnjxkvmokkencb 
       foreign key (REV) 
       references SMP_REV_INFO;

    alter table SMP_SG_EXTENSION 
       add constraint FKtf0mfonugp2jbkqo2o142chib 
       foreign key (ID) 
       references SMP_SERVICE_GROUP;

    alter table SMP_SG_EXTENSION_AUD 
       add constraint FKmdo9v2422adwyebvl34qa3ap6 
       foreign key (REV) 
       references SMP_REV_INFO;

    alter table SMP_USER_AUD 
       add constraint FK2786r5minnkai3d22b191iiiq 
       foreign key (REV) 
       references SMP_REV_INFO;
