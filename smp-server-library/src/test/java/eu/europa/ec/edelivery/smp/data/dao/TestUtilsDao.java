/*-
 * #START_LICENSE#
 * smp-server-library
 * %%
 * Copyright (C) 2017 - 2024 European Commission | eDelivery | DomiSMP
 * %%
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * [PROJECT_HOME]\license\eupl-1.2\license.txt or https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 * #END_LICENSE#
 */
package eu.europa.ec.edelivery.smp.data.dao;

import eu.europa.ec.edelivery.smp.config.enums.SMPDomainPropertyEnum;
import eu.europa.ec.edelivery.smp.data.enums.*;
import eu.europa.ec.edelivery.smp.data.model.DBDomain;
import eu.europa.ec.edelivery.smp.data.model.DBDomainConfiguration;
import eu.europa.ec.edelivery.smp.data.model.DBDomainResourceDef;
import eu.europa.ec.edelivery.smp.data.model.DBGroup;
import eu.europa.ec.edelivery.smp.data.model.doc.DBDocument;
import eu.europa.ec.edelivery.smp.data.model.doc.DBResource;
import eu.europa.ec.edelivery.smp.data.model.doc.DBSubresource;
import eu.europa.ec.edelivery.smp.data.model.ext.DBExtension;
import eu.europa.ec.edelivery.smp.data.model.ext.DBResourceDef;
import eu.europa.ec.edelivery.smp.data.model.ext.DBSubresourceDef;
import eu.europa.ec.edelivery.smp.data.model.user.*;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.testutil.TestDBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.Optional;

import static eu.europa.ec.edelivery.smp.testutil.TestConstants.*;
import static eu.europa.ec.edelivery.smp.testutil.TestDBUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Purpose of the class is to provide util to set-up test database data
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Repository
public class TestUtilsDao {

    @Autowired
    UserDao userDao;
    @PersistenceContext
    protected EntityManager memEManager;
    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(TestUtilsDao.class);


    DBDomain d1;
    DBDomain d2;
    DBDomain d3;
    DBResourceDef resourceDefSmp;
    DBSubresourceDef subresourceDefSmp;
    DBResourceDef resourceDefCpp;

    DBDomainResourceDef domainResourceDefD1R1;
    DBDomainResourceDef domainResourceDefD1R2;
    DBDomainResourceDef domainResourceDefD2R1;

    DBUser user1;
    DBUser user2;
    DBUser user3;

    DBUser user4;
    DBUser user5;

    DBGroup groupD1G1;
    DBGroup groupD1G2;
    DBGroup groupD2G1;

    DBResource resourceD1G1RD1;
    DBDocument documentD1G1RD1;
    DBResource resourceD2G1RD1;
    DBDocument documentD2G1RD1;

    DBDocument documentD1G1RD1_S1;
    DBDocument documentD2G1RD1_S1 ;
    DBSubresource subresourceD1G1RD1_S1;
    DBSubresource subresourceD2G1RD1_S1;

    DBDomainMember domainMemberU1D1Admin;
    DBDomainMember domainMemberU1D2Viewer;
    DBGroupMember groupMemberU1D1G1Admin;
    DBGroupMember groupMemberU1D2G1Viewer;

    DBResourceMember resourceMemberU1R1_D2G1RD1_Admin;
    DBResourceMember resourceMemberU1R2_D2G1RD1_Viewer;

    DBResource resourcePrivateD1G1RD1;
    DBExtension extension;


    boolean searchDataCreated = false;
    DBResource searchPubPubPubRes = null;
    DBResource searchPubPubPrivRes = null;
    DBResource searchPubPrivPubRes = null;
    DBResource searchPubPrivPrivRes = null;

    DBResource searchPrivPubPubRes = null;
    DBResource searchPrivPubPrivRes = null;
    DBResource searchPrivPrivPubRes = null;
    DBResource searchPrivPrivPrivRes = null;

    DBSubresource searchPubPubPubSubRes = null;
    DBSubresource searchPubPubPrivSubRes = null;
    DBSubresource searchPubPrivPubSubRes = null;
    DBSubresource searchPubPrivPrivSubRes = null;

    DBSubresource searchPrivPubPubSubRes = null;
    DBSubresource searchPrivPubPrivSubRes = null;
    DBSubresource searchPrivPrivPubSubRes = null;
    DBSubresource searchPrivPrivPrivSubRes = null;


    /**
     * Database can be cleaned by script before the next test; clean also the objects
     */
    public void clearData() {
        d1 = null;
        d2 = null;
        d3 = null;
        resourceDefSmp = null;
        subresourceDefSmp = null;
        resourceDefCpp = null;
        domainResourceDefD1R1 = null;
        domainResourceDefD1R2 = null;
        domainResourceDefD2R1 = null;
        user1 = null;
        user2 = null;
        user3 = null;
        user4 = null;
        user5 = null;
        groupD1G1 = null;
        groupD1G2 = null;
        groupD2G1 = null;
        resourceD1G1RD1 = null;
        resourceD2G1RD1 = null;
        documentD1G1RD1 = null;
        documentD2G1RD1 = null;
        subresourceD1G1RD1_S1 = null;
        subresourceD2G1RD1_S1 = null;
        documentD1G1RD1_S1 = null;
        documentD2G1RD1_S1 = null;
        domainMemberU1D1Admin = null;
        domainMemberU1D2Viewer = null;
        groupMemberU1D1G1Admin = null;
        groupMemberU1D2G1Viewer = null;
        resourceMemberU1R1_D2G1RD1_Admin = null;
        resourceMemberU1R2_D2G1RD1_Viewer = null;

        resourcePrivateD1G1RD1 = null;

        extension = null;
        searchDataCreated = false;
        searchPubPubPubRes = null;
        searchPubPubPrivRes = null;
        searchPubPrivPubRes = null;
        searchPubPrivPrivRes = null;

        searchPrivPubPubRes = null;
        searchPrivPubPrivRes = null;
        searchPrivPrivPubRes = null;
        searchPrivPrivPrivRes = null;

        searchPubPubPubSubRes = null;
        searchPubPubPrivSubRes = null;
        searchPubPrivPubSubRes = null;
        searchPubPrivPrivSubRes = null;

        searchPrivPubPubSubRes = null;
        searchPrivPubPrivSubRes = null;
        searchPrivPrivPubSubRes = null;
        searchPrivPrivPrivSubRes = null;


    }


    /**
     * Set two domains  and register the following resourceDefinitions
     * TEST_DOMAIN_CODE_1
     * <ul>
     *  <li>TEST_RESOURCE_DEF_SMP10</li>
     *  <li>TEST_RESOURCE_DEF_CPP</li>
     *  </ul>
     * TEST_DOMAIN_CODE_2
     * <ul>
     *  <li>TEST_RESOURCE_DEF_SMP10</li>
     *  </ul>
     */
    @Transactional
    public void createResourceDefinitionsForDomains() {
        if (domainResourceDefD1R1 != null) {
            LOG.trace("eResourceDefinitionsForDomains are already initialized!");
            return;
        }
        createDomains();
        createResourceDefinitions();
        // register resourceDef to Domain
        domainResourceDefD1R1 = registerDomainResourceDefinition(d1, resourceDefSmp);
        domainResourceDefD1R2 = registerDomainResourceDefinition(d1, resourceDefCpp);
        domainResourceDefD2R1 = registerDomainResourceDefinition(d2, resourceDefSmp);

        assertNotNull(domainResourceDefD1R1.getId());
        assertNotNull(domainResourceDefD1R2.getId());
        assertNotNull(domainResourceDefD2R1.getId());
    }

    /**
     * Create resource definitions with id and URLs
     * <ul>
     *  <li>TEST_RESOURCE_DEF_SMP10</li>
     *  <li>TEST_RESOURCE_DEF_CPP</li>
     *  </ul>
     */
    @Transactional
    public void createResourceDefinitions() {
        if (resourceDefSmp != null) {
            LOG.trace("ResourceDefinitions are already initialized!");
            return;
        }
        resourceDefSmp = createResourceDefinition(TEST_RESOURCE_DEF_SMP10_ID, TEST_RESOURCE_DEF_SMP10_URL);
        subresourceDefSmp =  createSubresourceDefinition(TEST_SUBRESOURCE_DEF_SMP10_ID, TEST_SUBRESOURCE_DEF_SMP10_URL, resourceDefSmp);

        resourceDefCpp = createResourceDefinition(TEST_RESOURCE_DEF_CPP, TEST_RESOURCE_DEF_CPP);

        assertNotNull(resourceDefSmp.getId());
        assertNotNull(resourceDefCpp.getId());
    }

    /**
     * Create resource definitions with id and URLs
     * <ul>
     *  <li>TEST_DOMAIN_CODE_1</li>
     *  <li>TEST_DOMAIN_CODE_2</li>
     *  </ul>
     */
    @Transactional
    public void createDomains() {
        if (d1 != null) {
            LOG.trace("Domains are already initialized!");
            return;
        }
        d1 = createDomain(TEST_DOMAIN_CODE_1);
        d2 = createDomain(TEST_DOMAIN_CODE_2);
        d3 = createRegisteredDomain(TEST_DOMAIN_CODE_3);

        assertNotNull(d1.getId());
        assertNotNull(d2.getId());
        assertNotNull(d3.getId());
    }

    @Transactional
    public void createUsers() {
        if (user1 != null) {
            LOG.trace("Domains are already initialized!");
            return;
        }
        user1 = createDBUserByUsername(USERNAME_1);
        DBCredential c1 = TestDBUtils.createDBCredentialForUser(user1, null, null, null);
        c1.setValue(BCrypt.hashpw(USERNAME_1_PASSWORD, BCrypt.gensalt()));
        user1.getUserCredentials().add(c1);

        user2 = createDBUserByCertificate(USER_CERT_2);

        user3 = createDBUserByUsername(USERNAME_3);
        DBCredential c3 = TestDBUtils.createDBCredentialForUserAccessToken(user3, null, null, null);
        c3.setValue(BCrypt.hashpw(USERNAME_3_AT_PASSWORD, BCrypt.gensalt()));
        c3.setName(USERNAME_3_AT);
        DBCredential cCert3 = TestDBUtils.createDBCredential(user3, USER_CERT_3, "", CredentialType.CERTIFICATE, CredentialTargetType.REST_API);

        user3.getUserCredentials().add(c3);
        user3.getUserCredentials().add(cCert3);

        user4 = createDBUserByUsername(USER_CERT_2);
        user5 = createDBUserByUsername(USERNAME_5);

        persistFlushDetach(user1);
        persistFlushDetach(user2);
        persistFlushDetach(user3);
        persistFlushDetach(user4);
        persistFlushDetach(user5);

        assertNotNull(user1.getId());
        assertNotNull(user2.getId());
        assertNotNull(user3.getId());
        assertNotNull(user4.getId());
        assertNotNull(user5.getId());
    }

    @Transactional
    public void deactivateUser(String username) {
        Optional<DBUser> userOpt = userDao.findUserByUsername(username);
        if (!userOpt.isPresent()) {
            LOG.warn("User [{}] not found and cannot be deactivated!", username);
            return;
        }
        DBUser user = userOpt.get();
        user.setActive(false);
        persistFlushDetach(user);

    }


    /**
     * Create domain members for
     * user1 on domain 1  as Admin
     * user1 on domain 2  as Viewer
     */
    @Transactional
    public void creatDomainMemberships() {
        if (domainMemberU1D1Admin != null) {
            LOG.trace("DomainMemberships are already initialized!");
            return;
        }
        createDomains();
        createUsers();
        domainMemberU1D1Admin = createDomainMembership(MembershipRoleType.ADMIN, user1, d1);
        domainMemberU1D2Viewer = createDomainMembership(MembershipRoleType.VIEWER, user1, d2);
    }

    @Transactional
    public void createGroupMemberships() {
        if (groupMemberU1D1G1Admin != null) {
            LOG.trace("GroupMemberships are already initialized!");
            return;
        }
        createGroups();
        createUsers();
        groupMemberU1D1G1Admin = createGroupMembership(MembershipRoleType.ADMIN, user1, groupD1G1);
        groupMemberU1D2G1Viewer = createGroupMembership(MembershipRoleType.VIEWER, user1, groupD2G1);
    }

    @Transactional
    public void createResourceMemberships() {
        if (resourceMemberU1R1_D2G1RD1_Admin != null) {
            LOG.trace("ResourceMemberships are already initialized!");
            return;
        }
        createUsers();
        createResources();
        resourceMemberU1R1_D2G1RD1_Admin = createResourceMembership(MembershipRoleType.ADMIN, user1, resourceD1G1RD1);
        resourceMemberU1R2_D2G1RD1_Viewer = createResourceMembership(MembershipRoleType.VIEWER, user1, resourceD2G1RD1);
    }

    @Transactional
    public void createResourcesForSearch() {

        if (searchDataCreated) {
            LOG.trace("Search Data is already initialized!");
            return;
        }

        createUsers();
        createResourceDefinitions();

        d1 = createDomain("publicDomain", VisibilityType.PUBLIC);
        d2 = createDomain("privateDomain", VisibilityType.PRIVATE);


        domainResourceDefD1R1 = registerDomainResourceDefinition(d1, resourceDefSmp);
        domainResourceDefD2R1 = registerDomainResourceDefinition(d2, resourceDefSmp);
        DBDomainResourceDef privateDomainResourceDef2= registerDomainResourceDefinition(d2, resourceDefCpp);
        // membership of the domain
        createDomainMembership(MembershipRoleType.VIEWER, user3, d2);

        groupD1G1  = createGroup("pubPubGroup", VisibilityType.PUBLIC, d1);
        groupD1G2 = createGroup("pubPrivGroup", VisibilityType.PRIVATE, d1);
        groupD2G1 = createGroup("privPubGroup", VisibilityType.PUBLIC, d2);
        DBGroup privPrivGroup = createGroup("privPrivGroup", VisibilityType.PRIVATE, d2);

        createGroupMembership(MembershipRoleType.VIEWER, user4, privPrivGroup);

        searchPubPubPubRes = createResource("pubPubPub", "1-1-1", VisibilityType.PUBLIC, domainResourceDefD1R1,  groupD1G1);
        searchPubPubPubSubRes = createSubresource(searchPubPubPubRes, "subres-pubPubPub", "s-1-1-1", DocumentVersionStatusType.PUBLISHED, subresourceDefSmp);
        searchPubPubPrivRes = createResource("pubPubPriv", "2-2-2", VisibilityType.PRIVATE, domainResourceDefD1R1,  groupD1G1);
        searchPubPubPrivSubRes = createSubresource(searchPubPubPrivRes, "subres-pubPubPriv", "s-2-2-2", DocumentVersionStatusType.PUBLISHED, subresourceDefSmp);
        searchPubPrivPubRes = createResource("pubPrivPub", "3-3-3", VisibilityType.PUBLIC, domainResourceDefD1R1,  groupD1G2);
        searchPubPrivPubSubRes = createSubresource(searchPubPrivPubRes, "subres-pubPrivPub", "s-3-3-3", DocumentVersionStatusType.PUBLISHED, subresourceDefSmp);
        searchPubPrivPrivRes = createResource("pubPrivPriv", "4-4-4", VisibilityType.PRIVATE, domainResourceDefD1R1,  groupD1G2);
        searchPubPrivPrivSubRes = createSubresource(searchPubPrivPrivRes, "subres-pubPrivPriv", "s-4-4-4", DocumentVersionStatusType.PUBLISHED, subresourceDefSmp);

        searchPrivPubPubRes = createResource("privPubPub", "5-5-5", VisibilityType.PUBLIC, domainResourceDefD2R1,  groupD2G1);
        searchPrivPubPubSubRes = createSubresource(searchPrivPubPubRes, "subres-privPubPub", "s-5-5-5", DocumentVersionStatusType.PUBLISHED, subresourceDefSmp);
        searchPrivPubPrivRes = createResource("privPubPriv", "6-6-6", VisibilityType.PRIVATE, domainResourceDefD2R1,  groupD2G1);
        searchPrivPubPrivSubRes = createSubresource(searchPrivPubPrivRes, "subres-privPubPriv", "s-6-6-6", DocumentVersionStatusType.PUBLISHED, subresourceDefSmp);
        searchPrivPrivPubRes = createResource("privPrivPub", "7-7-7", VisibilityType.PUBLIC, domainResourceDefD2R1,  privPrivGroup);
        searchPrivPrivPubSubRes = createSubresource(searchPrivPrivPubRes, "subres-privPrivPub", "s-7-7-7", DocumentVersionStatusType.PUBLISHED, subresourceDefSmp);
        searchPrivPrivPrivRes = createResource("privPrivPriv", "8-8-8", VisibilityType.PRIVATE, domainResourceDefD2R1,  privPrivGroup);
        searchPrivPrivPrivSubRes = createSubresource(searchPrivPrivPrivRes, "subres-privPrivPriv", "s-8-8-8", DocumentVersionStatusType.PUBLISHED, subresourceDefSmp);


        createResourceMembership(MembershipRoleType.ADMIN, user1, searchPubPubPubRes);
        createResourceMembership(MembershipRoleType.VIEWER, user2, searchPubPubPubRes);
        createResourceMembership(MembershipRoleType.ADMIN, user1, searchPubPubPrivRes);
        createResourceMembership(MembershipRoleType.VIEWER, user2, searchPubPubPrivRes);
        createResourceMembership(MembershipRoleType.ADMIN, user1, searchPubPrivPubRes);
        createResourceMembership(MembershipRoleType.VIEWER, user2, searchPubPrivPubRes);
        createResourceMembership(MembershipRoleType.ADMIN, user1, searchPubPrivPrivRes);
        createResourceMembership(MembershipRoleType.VIEWER, user2, searchPubPrivPrivRes);

        createResourceMembership(MembershipRoleType.ADMIN, user1, searchPrivPubPubRes);
        createResourceMembership(MembershipRoleType.VIEWER, user2, searchPrivPubPubRes);
        createResourceMembership(MembershipRoleType.ADMIN, user1, searchPrivPubPrivRes);
        createResourceMembership(MembershipRoleType.VIEWER, user2, searchPrivPubPrivRes);
        createResourceMembership(MembershipRoleType.ADMIN, user1, searchPrivPrivPubRes);
        createResourceMembership(MembershipRoleType.VIEWER, user2, searchPrivPrivPubRes);
        createResourceMembership(MembershipRoleType.ADMIN, user1, searchPrivPrivPrivRes);
        createResourceMembership(MembershipRoleType.VIEWER, user2, searchPrivPrivPrivRes);

        createResourceMembership(MembershipRoleType.VIEWER, user5, searchPrivPrivPrivRes);

        searchDataCreated = true;
    }

    @Transactional
    public DBDomainMember createDomainMembership(MembershipRoleType roleType, DBUser user, DBDomain domain){
        DBDomainMember domainMember = new DBDomainMember();
        domainMember.setRole(roleType);
        domainMember.setUser(user);
        domainMember.setDomain(domain);
        persistFlushDetach(domainMember);
        assertNotNull(domainMember.getId());
        return domainMember;
    }

    @Transactional
    public DBGroupMember createGroupMembership(MembershipRoleType roleType, DBUser user, DBGroup group){
        DBGroupMember member = new DBGroupMember();
        member.setRole(roleType);
        member.setUser(user);
        member.setGroup(group);
        persistFlushDetach(member);
        assertNotNull(member.getId());
        return member;
    }

    @Transactional
    public DBResourceMember createResourceMembership(MembershipRoleType roleType, DBUser user, DBResource resource){
        return createResourceMembership(roleType, user, resource, false);
    }

    @Transactional
    public DBResourceMember createResourceMembership(MembershipRoleType roleType, DBUser user, DBResource resource, boolean hasPermissionToReview){
        DBResourceMember member = new DBResourceMember();
        member.setRole(roleType);
        member.setUser(user);
        member.setHasPermissionToReview(hasPermissionToReview);
        member.setResource(resource);
        persistFlushDetach(member);
        assertNotNull(member.getId());
        return member;
    }

    /**
     * Create resources for ids:
     * TEST_SG_ID_1, TEST_SG_ID_2
     * <ul>
     *  <li>TEST_SG_ID_1 and schema TEST_SG_SCHEMA_1 for Domain TEST_DOMAIN_CODE_1 group TEST_GROUP_A and resource Type TEST_RESOURCE_DEF_SMP10 </li>
     *  <li>TEST_SG_ID_2 and schema null for Domain TEST_DOMAIN_CODE_2 group TEST_GROUP_A and resource Type TEST_RESOURCE_DEF_SMP10 </li>
     *  </ul>
     */
    @Transactional
    public void createResources() {
        if (resourceD1G1RD1 != null) {
            LOG.trace("Domains are already initialized!");
            return;
        }
        createGroups();
        createResourceDefinitionsForDomains();
        documentD1G1RD1 = createDocument(2,TEST_SG_ID_1, TEST_SG_SCHEMA_1);
        resourceD1G1RD1 = TestDBUtils.createDBResource(TEST_SG_ID_1, TEST_SG_SCHEMA_1);
        resourceD1G1RD1.setDocument(documentD1G1RD1);

        resourceD1G1RD1.setGroup(groupD1G1);
        resourceD1G1RD1.setDomainResourceDef(domainResourceDefD1R1);

        documentD2G1RD1 = createDocument(2, TEST_SG_ID_2, "");
        resourceD2G1RD1 = TestDBUtils.createDBResource(TEST_SG_ID_2, null);
        resourceD2G1RD1.setDocument(documentD2G1RD1);

        resourceD2G1RD1.setGroup(groupD2G1);
        resourceD2G1RD1.setDomainResourceDef(domainResourceDefD2R1);

        persistFlushDetach(resourceD1G1RD1);
        persistFlushDetach(resourceD2G1RD1);

        assertNotNull(resourceD1G1RD1.getId());
        assertNotNull(resourceD2G1RD1.getId());
    }

    @Transactional
    public DBResource createResource(String identifier, String schema,
                                     VisibilityType visibilityType,
                                     DBDomainResourceDef domainResourceDef,
                                     DBGroup group) {

        return createResource(identifier, schema, visibilityType, DocumentVersionStatusType.PUBLISHED, domainResourceDef, group);
    }

    @Transactional
    public DBResource createResource(String identifier, String schema,
                                     VisibilityType visibilityType,
                                     DocumentVersionStatusType status,
                                     DBDomainResourceDef domainResourceDef,
                                     DBGroup group) {

        DBResource resource = TestDBUtils.createDBResource(identifier, schema, true, status);
        resource.setVisibility(visibilityType);
        resource.setGroup(group);
        resource.setDomainResourceDef(domainResourceDef);
        resource.setReviewEnabled(true);

        persistFlushDetach(resource);
        assertNotNull(resource.getId());
        return resource;
    }

    @Transactional
    public DBSubresource createSubresource(DBResource resource, String identifier, String schema,
                                     DocumentVersionStatusType status, DBSubresourceDef subresourceDefSmp) {

        DBSubresource dbSubresource = TestDBUtils.createDBSubresource(
                resource.getIdentifierValue(),resource.getIdentifierScheme(),
                identifier, schema);


        dbSubresource.setSubresourceDef(subresourceDefSmp);

        DBDocument doc  = createDocument(1, resource.getIdentifierValue(), resource.getIdentifierScheme(),
                identifier, schema);
        doc.getDocumentVersions().get(0).setStatus(status);
        doc.setSharingEnabled(Boolean.TRUE);
        dbSubresource.setDocument(doc);
        dbSubresource.setResource(resource);


        persistFlushDetach(dbSubresource);
        assertNotNull(dbSubresource.getId());
        return dbSubresource;
    }




    /**
     * Create resources with subresources  for ids:
     * <ul>
     *  <li>TEST_SG_ID_1 and schema TEST_SG_SCHEMA_1 with subresource type: TEST_SUBRESOURCE_DEF_SMP10 with id: TEST_DOC_ID_1, TEST_DOC_SCHEMA_1 </li>
     *  <li>TEST_SG_ID_2 and schema null with subresource type: TEST_SUBRESOURCE_DEF_SMP10 with id: TEST_DOC_ID_2, TEST_DOC_SCHEMA_2  </li>
     * </ul>
     */
    @Transactional
    public void createSubresources() {
        if (subresourceD1G1RD1_S1 != null) {
            LOG.trace("Subresources are already initialized!");
            return;
        }
        createResources();

        documentD1G1RD1_S1 = createDocument(2, resourceD1G1RD1.getIdentifierValue(), resourceD1G1RD1.getIdentifierScheme(),
                TEST_DOC_ID_1, TEST_DOC_SCHEMA_1);
        subresourceD1G1RD1_S1 = TestDBUtils.createDBSubresource(
                resourceD1G1RD1.getIdentifierValue(),resourceD1G1RD1.getIdentifierScheme(),
                TEST_DOC_ID_1, TEST_DOC_SCHEMA_1);


        documentD2G1RD1_S1 = createDocument(2, resourceD2G1RD1.getIdentifierValue(),resourceD2G1RD1.getIdentifierScheme(),
                TEST_DOC_ID_2, TEST_DOC_SCHEMA_2);
        subresourceD2G1RD1_S1 = TestDBUtils.createDBSubresource(
                resourceD2G1RD1.getIdentifierValue(),resourceD2G1RD1.getIdentifierScheme(),
                TEST_DOC_ID_2, TEST_DOC_SCHEMA_2);

        subresourceD1G1RD1_S1.setDocument(documentD1G1RD1_S1);
        subresourceD2G1RD1_S1.setDocument(documentD2G1RD1_S1);

        subresourceD1G1RD1_S1.setResource(resourceD1G1RD1);
        subresourceD2G1RD1_S1.setResource(resourceD2G1RD1);

        subresourceD1G1RD1_S1.setSubresourceDef(subresourceDefSmp);
        subresourceD2G1RD1_S1.setSubresourceDef(subresourceDefSmp);

        persistFlushDetach(subresourceD1G1RD1_S1);
        persistFlushDetach(subresourceD2G1RD1_S1);

        assertNotNull(resourceD1G1RD1.getId());
        assertNotNull(resourceD2G1RD1.getId());
    }

    @Transactional
    public DBDocument createAndPersistDocument(int versions, String identifier, String schema) {
        DBDocument document = createDocument(versions, identifier, schema);
        persistFlushDetach(document);
        for (int i= 0; i< versions; i++ ) {
            assertNotNull(document.getDocumentVersions().get(i).getId());
        }
        // current version is first version all others are draft
        assertEquals(1, document.getCurrentVersion());

        return document;
    }

    public DBDocument createDocument(int versions, String identifier, String schema) {
        DBDocument document = createDBDocument();
        // add document versions to the document
        for (int i= 0; i< versions; i++ ) {
            document.addNewDocumentVersion(createDBDocumentVersion(identifier, schema));
        }
        return document;
    }

    public DBDocument createDocument(int versions, String identifier, String schema, String docIdentifier, String docSchema) {
        DBDocument document = createDBDocument();
        // add document versions to the document
        for (int i= 0; i< versions; i++ ) {
            document.addNewDocumentVersion(createDBDocumentVersion(identifier, schema, docIdentifier, docSchema));
        }
        return document;
    }


    /**
     * Set two domains  and register the following resourceDefinitions
     * TEST_DOMAIN_CODE_1
     * <ul>
     *  <li>TEST_RESOURCE_DEF_SMP10</li>
     *  <li>TEST_RESOURCE_DEF_CPP</li>
     *  </ul>
     * TEST_DOMAIN_CODE_2
     * <ul>
     *  <li>TEST_RESOURCE_DEF_SMP10</li>
     *  </ul>
     */
    @Transactional
    public void createGroups() {
        // check if domains are already created
        if (groupD1G1 != null) {
            LOG.trace("Domains are already initialized!");
            return;
        }
        createDomains();
        groupD1G1 = createGroup(TEST_GROUP_A, VisibilityType.PUBLIC, d1);
        groupD1G2 = createGroup(TEST_GROUP_B, VisibilityType.PUBLIC, d1);
        groupD2G1 = createGroup(TEST_GROUP_A, VisibilityType.PUBLIC, d2);
    }

    @Transactional
    public DBGroup createGroup(String groupName, VisibilityType visibility, DBDomain domain){
        DBGroup group = createDBGroup(groupName, visibility);
        group.setDomain(domain);
        persistFlushDetach(group);
        assertNotNull(group.getId());

        return group;
    }

    @Transactional
    public DBExtension createExtension() {
        extension = createDBExtension(TEST_EXTENSION_IDENTIFIER);
        persistFlushDetach(extension);
        return extension;
    }

    @Transactional
    public DBDomain createDomain(String domainCode) {
     return createDomain(domainCode, VisibilityType.PUBLIC);
    }

    @Transactional
    public DBDomain createRegisteredDomain(String domainCode) {
        DBDomain d = TestDBUtils.createDBDomain(domainCode);
        d.setSmlRegistered(true);
        persistFlushDetach(d);
        return d;
    }

    @Transactional
    public DBDomain createDomain(String domainCode, VisibilityType visibility) {
        DBDomain d = TestDBUtils.createDBDomain(domainCode);
        d.setVisibility(visibility);
        persistFlushDetach(d);

        createDefaultDomainProperties(d, SMPDomainPropertyEnum.RESOURCE_CASE_SENSITIVE_SCHEMES);
        createDefaultDomainProperties(d, SMPDomainPropertyEnum.RESOURCE_SCH_VALIDATION_REGEXP);
        return d;
    }

    @Transactional
    public DBDomainConfiguration createDefaultDomainProperties(DBDomain domain, SMPDomainPropertyEnum property ) {
        DBDomainConfiguration dc = new DBDomainConfiguration();
        dc.setDomain(domain);
        dc.setProperty(property.getProperty());
        dc.setValue(property.getDefValue());
        dc.setUseSystemDefault(false);
        persistFlushDetach(dc);
        return dc;
    }

    @Transactional
    public DBDomainConfiguration createDomainProperties(DBDomain domain, String property, String value) {
        DBDomainConfiguration dc = new DBDomainConfiguration();
        dc.setDomain(domain);
        dc.setProperty(property);
        dc.setValue(value);
        dc.setUseSystemDefault(false);
        persistFlushDetach(dc);
        return dc;
    }

    @Transactional
    public DBResourceDef createResourceDefinition(String identifier, String urlContextDef) {
        DBResourceDef d = TestDBUtils.createDBResourceDef(identifier, urlContextDef);
        persistFlushDetach(d);
        return d;
    }

    @Transactional
    public DBSubresourceDef createSubresourceDefinition(String identifier, String urlContextDef, DBResourceDef resourceDef) {
        DBSubresourceDef d = TestDBUtils.createDBSubresourceDef(identifier, urlContextDef);
        d.setResourceDef(resourceDef);
        persistFlushDetach(d);
        return d;
    }

    @Transactional
    public DBDomainResourceDef registerDomainResourceDefinition(DBDomain domain, DBResourceDef resourceDef) {
        DBDomainResourceDef domainResourceDef = new DBDomainResourceDef();

        domainResourceDef.setDomain(domain);
        domainResourceDef.setResourceDef(resourceDef);
        persistFlushDetach(domainResourceDef);
        return domainResourceDef;
    }

    @Transactional
    public <E> void persistFlushDetach(E entity) {
        LOG.debug("Persist entity: [{}]", entity);
        memEManager.persist(entity);
        memEManager.flush();
        memEManager.detach(entity);
    }

    @Transactional
    public <E> E merge(E entity) {
        LOG.debug("merge entity: [{}]", entity);
        return memEManager.merge(entity);
    }


    @Transactional
    public <E> E find(Class<E> clazz, Object id) {
        LOG.debug("find entity: [{}] for type [{}]", id, clazz);
        return memEManager.find(clazz, id);
    }

    public void clear() {
        memEManager.clear();
    }


    public DBDomain getD1() {
        return d1;
    }

    public DBDomain getD2() {
        return d2;
    }

    public DBDomain getD3() {
        return d3;
    }

    public DBResourceDef getResourceDefSmp() {
        return resourceDefSmp;
    }

    public DBResourceDef getResourceDefCpp() {
        return resourceDefCpp;
    }

    public DBDomainResourceDef getDomainResourceDefD1R1() {return domainResourceDefD1R1;}

    public DBDomainResourceDef getDomainResourceDefD1R2() {
        return domainResourceDefD1R2;
    }

    public DBDomainResourceDef getDomainResourceDefD2R1() {
        return domainResourceDefD2R1;
    }

    public DBUser getUser1() {
        return user1;
    }

    public DBUser getUser2() {
        return user2;
    }

    public DBUser getUser3() {
        return user3;
    }

    public DBUser getUser4() {
        return user4;
    }

    public DBUser getUser5() {
        return user5;
    }

    public DBGroup getGroupD1G1() {
        return groupD1G1;
    }

    public DBGroup getGroupD1G2() {
        return groupD1G2;
    }

    public DBGroup getGroupD2G1() {
        return groupD2G1;
    }

    public DBResource getResourceD1G1RD1() {
        return resourceD1G1RD1;
    }

    public DBSubresourceDef getSubresourceDefSmpMetadata() {
        return subresourceDefSmp;
    }

    public DBDocument getDocumentD1G1RD1_S1() {
        return documentD1G1RD1_S1;
    }

    public DBDocument getDocumentD2G1RD1_S1() {
        return documentD2G1RD1_S1;
    }

    public DBSubresource getSubresourceD1G1RD1_S1() {
        return subresourceD1G1RD1_S1;
    }

    public DBSubresource getSubresourceD2G1RD1_S1() {
        return subresourceD2G1RD1_S1;
    }

    public DBResource getResourceD2G1RD1() {
        return resourceD2G1RD1;
    }

    public DBDocument getDocumentD1G1RD1() {
        return documentD1G1RD1;
    }

    public DBDocument getDocumentD2G1RD1() {
        return documentD2G1RD1;
    }

    public DBExtension getExtension() {
        return extension;
    }

    public DBDomainMember getDomainMemberU1D1Admin() {
        return domainMemberU1D1Admin;
    }

    public DBDomainMember getDomainMemberU1D2Viewer() {
        return domainMemberU1D2Viewer;
    }

    public DBResourceMember getResourceMemberU1R1_D2G1RD1_Admin() {
        return resourceMemberU1R1_D2G1RD1_Admin;
    }

    public DBResourceMember getResourceMemberU1R2_D2G1RD1_Viewer(){
        return resourceMemberU1R2_D2G1RD1_Viewer;
    }

    public DBGroupMember getGroupMemberU1D1G1Admin() {
        return groupMemberU1D1G1Admin;
    }

    public DBGroupMember getGroupMemberU1D2G1Viewer() {
        return groupMemberU1D2G1Viewer;
    }


    public DBResource getResourceSearchPubPubPub() {
        return searchPubPubPubRes;
    }

    public DBResource getResourceSearchPubPubPriv() {
        return searchPubPubPrivRes;
    }

    public DBResource getResourceSearchPubPrivPub() {
        return searchPubPrivPubRes;
    }

    public DBResource getResourceSearchPubPrivPriv() {
        return searchPubPrivPrivRes;
    }

    public DBResource getResourceSearchPrivPubPub() {
        return searchPrivPubPubRes;
    }

    public DBResource getResourceSearchPrivPubPriv() {
        return searchPrivPubPrivRes;
    }

    public DBResource getResourceSearchPrivPrivPub() {
        return searchPrivPrivPubRes;
    }

    public DBResource getResourceSearchPrivPrivPriv() {
        return searchPrivPrivPrivRes;
    }

    public DBSubresource getSubresourceSearchPubPubPub() {
        return searchPubPubPubSubRes;
    }

    public DBSubresource getSubresourceSearchPubPubPriv() {
        return searchPubPubPrivSubRes;
    }

    public DBSubresource getSubresourceSearchPubPrivPub() {
        return searchPubPrivPubSubRes;
    }

    public DBSubresource getSubresourceSearchPubPrivPriv() {
        return searchPubPrivPrivSubRes;
    }

    public DBSubresource getSubresourceSearchPrivPubPub() {
        return searchPrivPubPubSubRes;
    }

    public DBSubresource getSubresourceSearchPrivPubPriv() {
        return searchPrivPubPrivSubRes;
    }

    public DBSubresource getSubresourceSearchPrivPrivPub() {
        return searchPrivPrivPubSubRes;
    }

    public DBSubresource getSubresourceSearchPrivPrivPriv() {
        return searchPrivPrivPrivSubRes;
    }
}
