-- ------------------------------------------------------------------------
-- This file was generated by hibernate for SMP version 5.1-SNAPSHOT.
-- ------------------------------------------------------------------------


    create table SMP_ALERT (
       ID bigint not null auto_increment comment 'Unique alert id',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        ALERT_LEVEL varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        ALERT_STATUS varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        ALERT_STATUS_DESC varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin,
        ALERT_TYPE varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        MAIL_SUBJECT varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin,
        MAIL_TO varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin,
        PROCESSED_TIME datetime,
        REPORTING_TIME datetime,
        FOR_USERNAME varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin,
        primary key (ID)
    ) comment='SMP alerts' ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_ALERT_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        ALERT_LEVEL varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        ALERT_STATUS varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        ALERT_STATUS_DESC varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin,
        ALERT_TYPE varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        MAIL_SUBJECT varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin,
        MAIL_TO varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin,
        PROCESSED_TIME datetime,
        REPORTING_TIME datetime,
        FOR_USERNAME varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_ALERT_PROPERTY (
       ID bigint not null auto_increment comment 'Unique alert property id',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        PROPERTY_NAME varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        PROPERTY_VALUE varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_ALERT_ID bigint,
        primary key (ID)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_ALERT_PROPERTY_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        PROPERTY_NAME varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        PROPERTY_VALUE varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_ALERT_ID bigint,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_CERTIFICATE (
       ID bigint not null comment 'Shared primary key with master table SMP_CREDENTIAL',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        CERTIFICATE_ID varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Formatted Certificate id using tags: cn, o, c:serialNumber',
        CRL_URL varchar(4000)  CHARACTER SET utf8 COLLATE utf8_bin comment 'URL to the certificate revocation list (CRL)',
        ISSUER varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Certificate issuer (canonical form)',
        PEM_ENCODED_CERT longtext comment 'PEM encoded  certificate',
        SERIALNUMBER varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Certificate serial number',
        SUBJECT varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Certificate subject (canonical form)',
        VALID_FROM datetime comment 'Certificate valid from date.',
        VALID_TO datetime comment 'Certificate valid to date.',
        primary key (ID)
    ) comment='SMP user certificates' ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_CERTIFICATE_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        CERTIFICATE_ID varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin,
        CRL_URL varchar(4000)  CHARACTER SET utf8 COLLATE utf8_bin,
        ISSUER varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin,
        PEM_ENCODED_CERT longtext,
        SERIALNUMBER varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        SUBJECT varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin,
        VALID_FROM datetime,
        VALID_TO datetime,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_CONFIGURATION (
       PROPERTY_NAME varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin not null comment 'Property name/key',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        DESCRIPTION varchar(4000)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Property description',
        PROPERTY_VALUE varchar(4000)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Property value',
        primary key (PROPERTY_NAME)
    ) comment='SMP user certificates' ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_CONFIGURATION_AUD (
       PROPERTY_NAME varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        DESCRIPTION varchar(4000)  CHARACTER SET utf8 COLLATE utf8_bin,
        PROPERTY_VALUE varchar(4000)  CHARACTER SET utf8 COLLATE utf8_bin,
        primary key (PROPERTY_NAME, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_CREDENTIAL (
       ID bigint not null auto_increment comment 'Unique id',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        CREDENTIAL_ACTIVE bit not null comment 'Is credential active',
        ACTIVE_FROM datetime comment 'Date when credential starts to be active',
        CHANGED_ON datetime comment 'Last date when credential was changed',
        CREDENTIAL_TARGET varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin not null comment 'Credential target UI, API',
        CREDENTIAL_TYPE varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin not null comment 'Credential type:  USERNAME, ACCESS_TOKEN, CERTIFICATE, CAS',
        CREDENTIAL_DESC varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Credential description',
        LAST_ALERT_ON datetime comment 'Generated last password expire alert',
        EXPIRE_ON datetime comment 'Date when password will expire',
        LAST_FAILED_LOGIN_ON datetime comment 'Last failed login attempt',
        CREDENTIAL_NAME varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin not null comment 'Unique username identifier. The Username must not be null',
        RESET_EXPIRE_ON datetime comment 'Date time when reset token will expire',
        RESET_TOKEN varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Reset token for credential reset',
        LOGIN_FAILURE_COUNT integer comment 'Sequential login failure count',
        CREDENTIAL_VALUE varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Credential value - it can be encrypted value',
        FK_USER_ID bigint not null,
        primary key (ID)
    ) comment='Credentials for the users' ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_CREDENTIAL_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        CREDENTIAL_ACTIVE bit,
        ACTIVE_FROM datetime,
        CHANGED_ON datetime,
        CREDENTIAL_TARGET varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        CREDENTIAL_TYPE varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        CREDENTIAL_DESC varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin,
        LAST_ALERT_ON datetime,
        EXPIRE_ON datetime,
        LAST_FAILED_LOGIN_ON datetime,
        CREDENTIAL_NAME varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin,
        RESET_EXPIRE_ON datetime,
        RESET_TOKEN varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin,
        LOGIN_FAILURE_COUNT integer,
        CREDENTIAL_VALUE varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_USER_ID bigint,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_DOCUMENT (
       ID bigint not null auto_increment comment 'Unique document id',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        CURRENT_VERSION integer not null,
        MIME_TYPE varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        NAME varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        primary key (ID)
    ) comment='SMP document entity for resources and subresources' ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_DOCUMENT_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        CURRENT_VERSION integer,
        MIME_TYPE varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        NAME varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_DOCUMENT_PROPERTY (
       ID bigint not null auto_increment comment 'Unique document property id',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        DESCRIPTION varchar(4000)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Property description',
        PROPERTY_NAME varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        PROPERTY_VALUE varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_DOCUMENT_ID bigint,
        primary key (ID)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_DOCUMENT_PROPERTY_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        DESCRIPTION varchar(4000)  CHARACTER SET utf8 COLLATE utf8_bin,
        PROPERTY_NAME varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        PROPERTY_VALUE varchar(1024)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_DOCUMENT_ID bigint,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_DOCUMENT_VERSION (
       ID bigint not null auto_increment comment 'Unique version document id',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        DOCUMENT_CONTENT longblob comment 'Document content',
        VERSION integer not null,
        FK_DOCUMENT_ID bigint,
        primary key (ID)
    ) comment='Document content for the document version.' ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_DOCUMENT_VERSION_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        DOCUMENT_CONTENT longblob,
        VERSION integer,
        FK_DOCUMENT_ID bigint,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_DOMAIN (
       ID bigint not null auto_increment comment 'Unique domain id',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        DEFAULT_RESOURCE_IDENTIFIER varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Default resourceType code',
        DOMAIN_CODE varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin not null comment 'Domain code used as http parameter in rest webservices',
        SIGNATURE_ALGORITHM varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Set signature algorithm. Ex.: http://www.w3.org/2001/04/xmldsig-more#rsa-sha256',
        SIGNATURE_DIGEST_METHOD varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Set signature hash method. Ex.: http://www.w3.org/2001/04/xmlenc#sha256',
        SIGNATURE_KEY_ALIAS varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Signature key alias used for SML integration',
        SML_CLIENT_CERT_AUTH bit not null comment 'Flag for SML authentication type - use ClientCert header or  HTTPS ClientCertificate (key)',
        SML_CLIENT_KEY_ALIAS varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Client key alias used for SML integration',
        SML_REGISTERED bit not null comment 'Flag for: Is domain registered in SML',
        SML_SMP_ID varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin comment 'SMP ID used for SML integration',
        SML_SUBDOMAIN varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin comment 'SML subdomain',
        VISIBILITY varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin comment 'The visibility of the domain: PUBLIC, INTERNAL',
        primary key (ID)
    ) comment='SMP can handle multiple domains. This table contains domain specific data' ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_DOMAIN_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        DEFAULT_RESOURCE_IDENTIFIER varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        DOMAIN_CODE varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin,
        SIGNATURE_ALGORITHM varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin,
        SIGNATURE_DIGEST_METHOD varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin,
        SIGNATURE_KEY_ALIAS varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin,
        SML_CLIENT_CERT_AUTH bit,
        SML_CLIENT_KEY_ALIAS varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin,
        SML_REGISTERED bit,
        SML_SMP_ID varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin,
        SML_SUBDOMAIN varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin,
        VISIBILITY varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_DOMAIN_CONFIGURATION (
       ID bigint not null auto_increment comment 'Unique domain configuration id',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        DESCRIPTION varchar(4000)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Property description',
        PROPERTY_NAME varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin not null comment 'Property name/key',
        SYSTEM_DEFAULT bit not null comment 'Use system default value',
        PROPERTY_VALUE varchar(4000)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Property value',
        FK_DOMAIN_ID bigint not null,
        primary key (ID)
    ) comment='SMP domain configuration' ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_DOMAIN_CONFIGURATION_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        DESCRIPTION varchar(4000)  CHARACTER SET utf8 COLLATE utf8_bin,
        PROPERTY_NAME varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin,
        SYSTEM_DEFAULT bit,
        PROPERTY_VALUE varchar(4000)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_DOMAIN_ID bigint,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_DOMAIN_MEMBER (
       ID bigint not null auto_increment,
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        MEMBERSHIP_ROLE varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_DOMAIN_ID bigint,
        FK_USER_ID bigint,
        primary key (ID)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_DOMAIN_MEMBER_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        MEMBERSHIP_ROLE varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_DOMAIN_ID bigint,
        FK_USER_ID bigint,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_DOMAIN_RESOURCE_DEF (
       ID bigint not null auto_increment,
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        FK_DOMAIN_ID bigint,
        FK_RESOURCE_DEF_ID bigint,
        primary key (ID)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_DOMAIN_RESOURCE_DEF_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        FK_DOMAIN_ID bigint,
        FK_RESOURCE_DEF_ID bigint,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_EXTENSION (
       ID bigint not null auto_increment comment 'Unique extension id',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        DESCRIPTION varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin,
        IDENTIFIER varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        IMPLEMENTATION_NAME varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin,
        NAME varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        VERSION varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        primary key (ID)
    ) comment='SMP extension definitions' ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_EXTENSION_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        DESCRIPTION varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin,
        IDENTIFIER varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        IMPLEMENTATION_NAME varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin,
        NAME varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        VERSION varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_GROUP (
       ID bigint not null auto_increment comment 'Unique domain group id',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        DESCRIPTION varchar(4000)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Domain Group description',
        NAME varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin not null comment 'Domain Group name',
        VISIBILITY varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_DOMAIN_ID bigint not null,
        primary key (ID)
    ) comment='The group spans the resources belonging to the domain group.' ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_GROUP_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        DESCRIPTION varchar(4000)  CHARACTER SET utf8 COLLATE utf8_bin,
        NAME varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin,
        VISIBILITY varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_DOMAIN_ID bigint,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_GROUP_MEMBER (
       ID bigint not null auto_increment,
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        MEMBERSHIP_ROLE varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_GROUP_ID bigint,
        FK_USER_ID bigint,
        primary key (ID)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_GROUP_MEMBER_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        MEMBERSHIP_ROLE varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_GROUP_ID bigint,
        FK_USER_ID bigint,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_RESOURCE (
       ID bigint not null auto_increment comment 'Unique ServiceGroup id',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        IDENTIFIER_SCHEME varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin,
        IDENTIFIER_VALUE varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin not null,
        SML_REGISTERED bit not null,
        VISIBILITY varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_DOCUMENT_ID bigint not null,
        FK_DOREDEF_ID bigint not null,
        FK_GROUP_ID bigint,
        primary key (ID)
    ) comment='SMP resource Identifier and scheme' ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_RESOURCE_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        IDENTIFIER_SCHEME varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin,
        IDENTIFIER_VALUE varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin,
        SML_REGISTERED bit,
        VISIBILITY varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_DOCUMENT_ID bigint,
        FK_DOREDEF_ID bigint,
        FK_GROUP_ID bigint,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_RESOURCE_DEF (
       ID bigint not null auto_increment comment 'Unique id',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        DESCRIPTION varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin,
        HANDLER_IMPL_NAME varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin,
        IDENTIFIER varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        MIME_TYPE varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        NAME varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        URL_SEGMENT varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin comment 'resources are published under url_segment.',
        FK_EXTENSION_ID bigint,
        primary key (ID)
    ) comment='SMP extension resource definitions' ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_RESOURCE_DEF_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        DESCRIPTION varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin,
        HANDLER_IMPL_NAME varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin,
        IDENTIFIER varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        MIME_TYPE varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        NAME varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        URL_SEGMENT varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_EXTENSION_ID bigint,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_RESOURCE_MEMBER (
       ID bigint not null auto_increment,
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        MEMBERSHIP_ROLE varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_RESOURCE_ID bigint,
        FK_USER_ID bigint,
        primary key (ID)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_RESOURCE_MEMBER_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        MEMBERSHIP_ROLE varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_RESOURCE_ID bigint,
        FK_USER_ID bigint,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_REV_INFO (
       id bigint not null auto_increment,
        REVISION_DATE datetime,
        timestamp bigint not null,
        USERNAME varchar(255)  CHARACTER SET utf8 COLLATE utf8_bin,
        primary key (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_SUBRESOURCE (
       ID bigint not null auto_increment comment 'Shared primary key with master table SMP_SUBRESOURCE',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        IDENTIFIER_SCHEME varchar(500)  CHARACTER SET utf8 COLLATE utf8_bin,
        IDENTIFIER_VALUE varchar(500)  CHARACTER SET utf8 COLLATE utf8_bin not null,
        FK_DOCUMENT_ID bigint,
        FK_RESOURCE_ID bigint not null,
        FK_SUREDEF_ID bigint not null,
        primary key (ID)
    ) comment='Service metadata' ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_SUBRESOURCE_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        IDENTIFIER_SCHEME varchar(500)  CHARACTER SET utf8 COLLATE utf8_bin,
        IDENTIFIER_VALUE varchar(500)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_DOCUMENT_ID bigint,
        FK_RESOURCE_ID bigint,
        FK_SUREDEF_ID bigint,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_SUBRESOURCE_DEF (
       ID bigint not null auto_increment comment 'Unique id',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        DESCRIPTION varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        HANDLER_IMPL_NAME varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin,
        IDENTIFIER varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        MIME_TYPE varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        NAME varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        URL_SEGMENT varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin comment 'Subresources are published under url_segment. It must be unique for resource type',
        FK_RESOURCE_DEF_ID bigint,
        primary key (ID)
    ) comment='SMP extension subresource definitions' ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_SUBRESOURCE_DEF_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        DESCRIPTION varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        HANDLER_IMPL_NAME varchar(512)  CHARACTER SET utf8 COLLATE utf8_bin,
        IDENTIFIER varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        MIME_TYPE varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        NAME varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        URL_SEGMENT varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin,
        FK_RESOURCE_DEF_ID bigint,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_USER (
       ID bigint not null auto_increment comment 'Unique user id',
        CREATED_ON datetime not null,
        LAST_UPDATED_ON datetime not null,
        ACTIVE bit not null comment 'Is user active',
        APPLICATION_ROLE varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin comment 'User application role as USER, SYSTEM_ADMIN',
        EMAIL varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin comment 'User email',
        FULL_NAME varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin comment 'User full name (name and lastname)',
        SMP_LOCALE varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin comment 'DomiSMP settings: locale for the user',
        SMP_THEME varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin comment 'DomiSMP settings: theme for the user',
        USERNAME varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin not null comment 'Unique username identifier. The Username must not be null',
        primary key (ID)
    ) comment='SMP can handle multiple domains. This table contains domain specific data' ENGINE=InnoDB DEFAULT CHARSET=utf8;

    create table SMP_USER_AUD (
       ID bigint not null,
        REV bigint not null,
        REVTYPE tinyint,
        CREATED_ON datetime,
        LAST_UPDATED_ON datetime,
        ACTIVE bit,
        APPLICATION_ROLE varchar(256)  CHARACTER SET utf8 COLLATE utf8_bin,
        EMAIL varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        FULL_NAME varchar(128)  CHARACTER SET utf8 COLLATE utf8_bin,
        SMP_LOCALE varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin,
        SMP_THEME varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin,
        USERNAME varchar(64)  CHARACTER SET utf8 COLLATE utf8_bin,
        primary key (ID, REV)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    alter table SMP_CERTIFICATE 
       add constraint UK_3x3rvf6hkim9fg16caurkgg6f unique (CERTIFICATE_ID);

    alter table SMP_CREDENTIAL 
       add constraint SMP_CRD_USER_NAME_TYPE_IDX unique (CREDENTIAL_NAME, CREDENTIAL_TYPE, CREDENTIAL_TARGET);

    alter table SMP_DOCUMENT_PROPERTY 
       add constraint SMP_DOC_PROP_IDX unique (FK_DOCUMENT_ID, PROPERTY_NAME);
create index SMP_DOCVER_DOCUMENT_IDX on SMP_DOCUMENT_VERSION (FK_DOCUMENT_ID);

    alter table SMP_DOCUMENT_VERSION 
       add constraint SMP_DOCVER_UNIQ_VERSION_IDX unique (FK_DOCUMENT_ID, VERSION);

    alter table SMP_DOMAIN 
       add constraint UK_djrwqd4luj5i7w4l7fueuaqbj unique (DOMAIN_CODE);

    alter table SMP_DOMAIN_CONFIGURATION 
       add constraint SMP_DOMAIN_CONF_IDX unique (ID, PROPERTY_NAME, FK_DOMAIN_ID);

    alter table SMP_DOMAIN_MEMBER 
       add constraint SMP_DOM_MEM_IDX unique (FK_DOMAIN_ID, FK_USER_ID);

    alter table SMP_DOMAIN_RESOURCE_DEF 
       add constraint SMP_DOREDEF_UNIQ_DOM_RD_IDX unique (FK_RESOURCE_DEF_ID, FK_DOMAIN_ID);

    alter table SMP_EXTENSION 
       add constraint SMP_EXT_UNIQ_NAME_IDX unique (IMPLEMENTATION_NAME);

    alter table SMP_EXTENSION 
       add constraint UK_p4vfhgs7fvuo6uebjsuqxrglg unique (IDENTIFIER);

    alter table SMP_GROUP 
       add constraint SMP_GRP_UNIQ_DOM_IDX unique (NAME, FK_DOMAIN_ID);

    alter table SMP_GROUP_MEMBER 
       add constraint SMP_GRP_MEM_IDX unique (FK_GROUP_ID, FK_USER_ID);
create index SMP_RS_ID_IDX on SMP_RESOURCE (IDENTIFIER_VALUE);
create index SMP_RS_SCH_IDX on SMP_RESOURCE (IDENTIFIER_SCHEME);

    alter table SMP_RESOURCE 
       add constraint SMP_RS_UNIQ_IDENT_DOREDEF_IDX unique (IDENTIFIER_SCHEME, IDENTIFIER_VALUE, FK_DOREDEF_ID);

    alter table SMP_RESOURCE_DEF 
       add constraint SMP_RESDEF_UNIQ_EXTID_CODE_IDX unique (FK_EXTENSION_ID, IDENTIFIER);

    alter table SMP_RESOURCE_DEF 
       add constraint UK_k7l5fili2mmhgslv77afg4myo unique (IDENTIFIER);

    alter table SMP_RESOURCE_DEF 
       add constraint UK_jjbctkhd4h0u9whb1i9wbxwoe unique (URL_SEGMENT);

    alter table SMP_RESOURCE_MEMBER 
       add constraint SMP_RES_MEM_IDX unique (FK_RESOURCE_ID, FK_USER_ID);
create index SMP_SMD_DOC_ID_IDX on SMP_SUBRESOURCE (IDENTIFIER_VALUE);
create index SMP_SMD_DOC_SCH_IDX on SMP_SUBRESOURCE (IDENTIFIER_SCHEME);

    alter table SMP_SUBRESOURCE 
       add constraint SMP_SRS_UNIQ_ID_RES_SRT_IDX unique (FK_RESOURCE_ID, IDENTIFIER_VALUE, IDENTIFIER_SCHEME);

    alter table SMP_SUBRESOURCE_DEF 
       add constraint SMP_RD_UNIQ_RDID_UCTX_IDX unique (FK_RESOURCE_DEF_ID, URL_SEGMENT);

    alter table SMP_SUBRESOURCE_DEF 
       add constraint UK_pmdcnfwm5in2q9ky0b6dlgqvi unique (IDENTIFIER);

    alter table SMP_USER 
       add constraint UK_rt1f0anklfo05lt0my05fqq6 unique (USERNAME);

    alter table SMP_ALERT_AUD 
       add constraint FKrw0qnto448ojlirpfmfntd8v2 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_ALERT_PROPERTY 
       add constraint FK15r37w3r5ty5f6074ykr2o4i6 
       foreign key (FK_ALERT_ID) 
       references SMP_ALERT (ID);

    alter table SMP_ALERT_PROPERTY_AUD 
       add constraint FKod33qjx87ih1a0skxl2sgddar 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_CERTIFICATE 
       add constraint FK25b9apuupvmjp18wnn2b2gfg8 
       foreign key (ID) 
       references SMP_CREDENTIAL (ID);

    alter table SMP_CERTIFICATE_AUD 
       add constraint FKnrwm8en8vv10li8ihwnurwd9e 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_CONFIGURATION_AUD 
       add constraint FKd4yhbdlusovfbdti1fjkuxp9m 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_CREDENTIAL 
       add constraint FK89it2lyqvi2bl9bettx66n8n1 
       foreign key (FK_USER_ID) 
       references SMP_USER (ID);

    alter table SMP_CREDENTIAL_AUD 
       add constraint FKqjh6vxvb5tg0tvbkvi3k3xhe6 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_DOCUMENT_AUD 
       add constraint FKh9epnme26i271eixtvrpqejvi 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_DOCUMENT_PROPERTY 
       add constraint FKfag3795e9mrvfvesd00yis9yh 
       foreign key (FK_DOCUMENT_ID) 
       references SMP_DOCUMENT (ID);

    alter table SMP_DOCUMENT_PROPERTY_AUD 
       add constraint FK81057kcrugb1cfm0io5vkxtin 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_DOCUMENT_VERSION 
       add constraint FKalsuoqx4csyp9mygvng911do 
       foreign key (FK_DOCUMENT_ID) 
       references SMP_DOCUMENT (ID);

    alter table SMP_DOCUMENT_VERSION_AUD 
       add constraint FK4glqiu73939kpyyb6bhw822k3 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_DOMAIN_AUD 
       add constraint FK35qm8xmi74kfenugeonijodsg 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_DOMAIN_CONFIGURATION 
       add constraint FK4303vstoigqtmeo3t2i034gm3 
       foreign key (FK_DOMAIN_ID) 
       references SMP_DOMAIN (ID);

    alter table SMP_DOMAIN_CONFIGURATION_AUD 
       add constraint FKkelcga805bleh5x256hy5e1xb 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_DOMAIN_MEMBER 
       add constraint FK1tdwy9oiyrk6tl4mk0fakhkf5 
       foreign key (FK_DOMAIN_ID) 
       references SMP_DOMAIN (ID);

    alter table SMP_DOMAIN_MEMBER 
       add constraint FKino2nvj74wc755nyn5mo260qi 
       foreign key (FK_USER_ID) 
       references SMP_USER (ID);

    alter table SMP_DOMAIN_MEMBER_AUD 
       add constraint FKijiv1avufqo9iu5u0cj4v3pv7 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_DOMAIN_RESOURCE_DEF 
       add constraint FK563xw5tjw4rlr32va9g17cdsq 
       foreign key (FK_DOMAIN_ID) 
       references SMP_DOMAIN (ID);

    alter table SMP_DOMAIN_RESOURCE_DEF 
       add constraint FKtppp16v40ll2ch3ly8xusb8hi 
       foreign key (FK_RESOURCE_DEF_ID) 
       references SMP_RESOURCE_DEF (ID);

    alter table SMP_DOMAIN_RESOURCE_DEF_AUD 
       add constraint FKpujj9vb097i5w4loa3dxww2nj 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_EXTENSION_AUD 
       add constraint FKke7f9wbwvp1bmnlqh9hrfm0r 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_GROUP 
       add constraint FKjeomxyxjueaiyt7f0he0ls7vm 
       foreign key (FK_DOMAIN_ID) 
       references SMP_DOMAIN (ID);

    alter table SMP_GROUP_AUD 
       add constraint FKeik3quor2dxho7bmyoxc2ug9o 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_GROUP_MEMBER 
       add constraint FK3y21chrphgx1dytux0p19btxe 
       foreign key (FK_GROUP_ID) 
       references SMP_GROUP (ID);

    alter table SMP_GROUP_MEMBER 
       add constraint FK8ue5gj1rx6gyiqp19dscp85ut 
       foreign key (FK_USER_ID) 
       references SMP_USER (ID);

    alter table SMP_GROUP_MEMBER_AUD 
       add constraint FK5pmorcyhwkaysh0a8xm99x6a8 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_RESOURCE 
       add constraint FKkc5a6okrvq7dv87itfp7i1vmv 
       foreign key (FK_DOCUMENT_ID) 
       references SMP_DOCUMENT (ID);

    alter table SMP_RESOURCE 
       add constraint FK24mw8fiua39nh8rnobhgmujri 
       foreign key (FK_DOREDEF_ID) 
       references SMP_DOMAIN_RESOURCE_DEF (ID);

    alter table SMP_RESOURCE 
       add constraint FKft55kasui36i77inf0wh8utv5 
       foreign key (FK_GROUP_ID) 
       references SMP_GROUP (ID);

    alter table SMP_RESOURCE_AUD 
       add constraint FKlbbfltxw6qmph5w3i8c9qf6kb 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_RESOURCE_DEF 
       add constraint FKruu7v6uig9h333ihv34haw3ob 
       foreign key (FK_EXTENSION_ID) 
       references SMP_EXTENSION (ID);

    alter table SMP_RESOURCE_DEF_AUD 
       add constraint FKapswkgbdm9s4wwhx2cjduoniw 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_RESOURCE_MEMBER 
       add constraint FKrci5jlgnckwo1mhq2rvmfaptw 
       foreign key (FK_RESOURCE_ID) 
       references SMP_RESOURCE (ID);

    alter table SMP_RESOURCE_MEMBER 
       add constraint FKs6jx68jxlx4xfdtxy20f3s6lu 
       foreign key (FK_USER_ID) 
       references SMP_USER (ID);

    alter table SMP_RESOURCE_MEMBER_AUD 
       add constraint FKknykp2wcby9fxk234yaaix1pe 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_SUBRESOURCE 
       add constraint FK7y1ydnq350mbs3c8yrq2fhnsk 
       foreign key (FK_DOCUMENT_ID) 
       references SMP_DOCUMENT (ID);

    alter table SMP_SUBRESOURCE 
       add constraint FK7clbsapruvhkcqgekfxs8prex 
       foreign key (FK_RESOURCE_ID) 
       references SMP_RESOURCE (ID);

    alter table SMP_SUBRESOURCE 
       add constraint FKq3wmyy4ieoenuu1s55237qu9k 
       foreign key (FK_SUREDEF_ID) 
       references SMP_SUBRESOURCE_DEF (ID);

    alter table SMP_SUBRESOURCE_AUD 
       add constraint FKffihyo233ldee8nejbkyclrov 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_SUBRESOURCE_DEF 
       add constraint FKbjqilcym6p3pptva2s4d1gw8o 
       foreign key (FK_RESOURCE_DEF_ID) 
       references SMP_RESOURCE_DEF (ID);

    alter table SMP_SUBRESOURCE_DEF_AUD 
       add constraint FK1dd2l0ujtncg9u7hl3c4rte63 
       foreign key (REV) 
       references SMP_REV_INFO (id);

    alter table SMP_USER_AUD 
       add constraint FK2786r5minnkai3d22b191iiiq 
       foreign key (REV) 
       references SMP_REV_INFO (id);
