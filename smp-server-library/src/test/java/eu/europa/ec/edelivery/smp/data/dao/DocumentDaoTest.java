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

import eu.europa.ec.edelivery.smp.data.enums.DocumentVersionStatusType;
import eu.europa.ec.edelivery.smp.data.enums.MembershipRoleType;
import eu.europa.ec.edelivery.smp.data.enums.VisibilityType;
import eu.europa.ec.edelivery.smp.data.model.doc.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DocumentDaoTest extends AbstractBaseDao {

    @Autowired
    DocumentDao testInstance;

    @BeforeEach
    public void prepareDatabase() {
        testUtilsDao.clearData();
        testUtilsDao.createResourceMemberships();
        testUtilsDao.createSubresources();
    }

    @Test
    void testPersistDocument() {

        DBDocument document = testUtilsDao.createAndPersistDocument(2, "value1", "schema1");

        assertNotNull(document.getId());
        assertEquals(2, document.getDocumentVersions().size());
        assertEquals(2, document.getCurrentVersion());
    }


    @Test
    void getDocumentForResource() {
        Optional<DBDocument> result = testInstance.getDocumentForResource(testUtilsDao.getResourceD1G1RD1());

        assertTrue(result.isPresent());
        assertEquals(testUtilsDao.getDocumentD1G1RD1(), result.get());
        // the default setup  createResources  sets two versions (0 and 1 ) with current version 1
        assertEquals(2, result.get().getCurrentVersion());
    }


    @Test
    void getDocumentVersionsForResource() {
        List<DBDocumentVersion> result = testInstance.getDocumentVersionsForResource(testUtilsDao.getResourceD1G1RD1());

        assertEquals(2, result.size());
    }

    @Test
    void getCurrentDocumentVersionForResource() {

        Optional<DBDocumentVersion> result = testInstance.getCurrentDocumentVersionForResource(testUtilsDao.getResourceD1G1RD1());

        assertTrue(result.isPresent());
        // the default setup  createResources  sets two versions (0 and 1 ) with current version 1
        assertEquals(2, result.get().getVersion());
        // note that the versions are ordered by version desc
        assertEquals(testUtilsDao.getDocumentD1G1RD1().getDocumentVersions().get(0), result.get());
    }


    @Test
    void getDocumentVersionsForSubresource() {
        List<DBDocumentVersion> result = testInstance.getDocumentVersionsForSubresource(testUtilsDao.getSubresourceD1G1RD1_S1());

        assertEquals(2, result.size());
    }

    @Test
    void getCurrentDocumentVersionForSubresource() {

        Optional<DBDocumentVersion> result = testInstance.getCurrentDocumentVersionForSubresource(testUtilsDao.getSubresourceD1G1RD1_S1());

        assertTrue(result.isPresent());
        // the default setup  createResources  sets two versions (1 and 2 ) with current version 2
        assertEquals(2, result.get().getVersion());
        // note that the versions are ordered by version desc
        assertEquals(testUtilsDao.getDocumentD1G1RD1_S1().getDocumentVersions().get(0), result.get());
    }

    @Test
    @Transactional
    void testPersistDocumentProperty() {
        // given
        DBDocument document = testUtilsDao.createDocument(2, "value1", "schema1");
        DBDocumentProperty property1 = new DBDocumentProperty("property1", "value1", document);
        DBDocumentProperty property2 = new DBDocumentProperty("property2", "value1", document);
        document.getDocumentProperties().add(property1);
        document.getDocumentProperties().add(property2);
        // when
        testInstance.persistFlushDetach(document);
        // then
        DBDocument result = testInstance.find(document.getId());
        assertNotNull(result.getId());
        // different object instances
        assertNotSame(document, result);
        assertEquals(2, result.getDocumentProperties().size());
        assertEquals(property1, result.getDocumentProperties().get(0));
    }

    @Test
    void testPersistUnderReviewDocument() {
        DBResource createResourceWithStatusReview = testUtilsDao.createResource("review", "1-1-1", VisibilityType.PUBLIC,
                DocumentVersionStatusType.UNDER_REVIEW,
                testUtilsDao.getDomainResourceDefD1R1(), testUtilsDao.getGroupD1G1());

        testUtilsDao.createResourceMembership(MembershipRoleType.ADMIN, testUtilsDao.getUser1(), createResourceWithStatusReview, true);

        List<DBReviewDocumentVersion> dbDocumentVersions =  testInstance.getDocumentReviewListForUser(testUtilsDao.getUser1().getId());
        assertEquals(1, dbDocumentVersions.size());

    }

    @ParameterizedTest
    @CsvSource({
            "UNDER_REVIEW, UNDER_REVIEW, 2",
            "UNDER_REVIEW, DRAFT, 1",
            "DRAFT, UNDER_REVIEW, 1",
            "DRAFT, DRAFT, 0"
    })
    void testPersistUnderReviewDocumentSubresource(String resourceStatus, String subresourceStatus, int expectedSize) {
        testUtilsDao.createResourceDefinitions();
        DBResource resource = testUtilsDao.createResource("review", "1-1-1", VisibilityType.PUBLIC,
                DocumentVersionStatusType.valueOf(resourceStatus),
                testUtilsDao.getDomainResourceDefD1R1(), testUtilsDao.getGroupD1G1());

        DBSubresource subres = testUtilsDao.createSubresource(resource, "1-1-1", "1-1-1",
                DocumentVersionStatusType.valueOf(subresourceStatus),
                testUtilsDao.getSubresourceDefSmpMetadata());

        testUtilsDao.createResourceMembership(MembershipRoleType.ADMIN, testUtilsDao.getUser1(), resource, true);

        List<DBReviewDocumentVersion> dbDocumentVersions =  testInstance.getDocumentReviewListForUser(testUtilsDao.getUser1().getId());
        assertEquals(expectedSize, dbDocumentVersions.size());

    }
}
