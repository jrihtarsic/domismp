-- Rollback for sequence creation
drop sequence SMP_DOC_PROP_SEQ;
drop sequence SMP_DOCVER_EVENT_SEQ;
drop sequence SMP_DOMAIN_CONF_SEQ;

-- Rollback for table alterations
ALTER TABLE SMP_CREDENTIAL DROP COLUMN RESET_EXPIRE_ON;
ALTER TABLE SMP_CREDENTIAL DROP COLUMN RESET_TOKEN;
ALTER TABLE SMP_CREDENTIAL_AUD DROP COLUMN RESET_EXPIRE_ON;
ALTER TABLE SMP_CREDENTIAL_AUD DROP COLUMN RESET_TOKEN;

ALTER TABLE SMP_DOCUMENT DROP COLUMN REF_DOCUMENT_URL;
ALTER TABLE SMP_DOCUMENT DROP COLUMN SHARING_ENABLED;
ALTER TABLE SMP_DOCUMENT DROP COLUMN FK_REF_DOCUMENT_ID;
ALTER TABLE SMP_DOCUMENT_AUD DROP COLUMN REF_DOCUMENT_URL;
ALTER TABLE SMP_DOCUMENT_AUD DROP COLUMN SHARING_ENABLED;
ALTER TABLE SMP_DOCUMENT_AUD DROP COLUMN FK_REF_DOCUMENT_ID;

ALTER TABLE SMP_DOCUMENT_VERSION DROP COLUMN STATUS;
ALTER TABLE SMP_DOCUMENT_VERSION_AUD DROP COLUMN STATUS;

ALTER TABLE SMP_RESOURCE DROP COLUMN REVIEW_ENABLED;
ALTER TABLE SMP_RESOURCE_AUD DROP COLUMN REVIEW_ENABLED;

ALTER TABLE SMP_RESOURCE_MEMBER DROP COLUMN PERMISSION_REVIEW;
ALTER TABLE SMP_RESOURCE_MEMBER_AUD DROP COLUMN PERMISSION_REVIEW;


-- Rollback for constraints and indexes
ALTER TABLE SMP_DOCUMENT_PROPERTY DROP CONSTRAINT SMP_DOC_PROP_IDX;
DROP INDEX SMP_DOCVEREVNT_DOCVER_IDX;

ALTER TABLE SMP_DOMAIN_CONFIGURATION DROP CONSTRAINT SMP_DOMAIN_CONF_IDX;

-- Rollback for table creation
drop table SMP_DOCUMENT_PROPERTY;
drop table SMP_DOCUMENT_PROPERTY_AUD;
drop table SMP_DOCUMENT_VERSION_EVENT;
drop table SMP_DOMAIN_CONFIGURATION;
drop table SMP_DOMAIN_CONFIGURATION_AUD;
