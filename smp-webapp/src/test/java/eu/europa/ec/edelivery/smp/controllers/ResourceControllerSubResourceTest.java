/*-
 * #START_LICENSE#
 * smp-webapp
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
package eu.europa.ec.edelivery.smp.controllers;

import eu.europa.ec.edelivery.smp.data.enums.VisibilityType;
import eu.europa.ec.edelivery.smp.servlet.WebConstants;
import eu.europa.ec.edelivery.smp.ui.AbstractControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;

import static eu.europa.ec.edelivery.smp.ServiceGroupBodyUtil.generateServiceMetadata;
import static eu.europa.ec.edelivery.smp.ServiceGroupBodyUtil.getSampleServiceGroupBody;
import static eu.europa.ec.edelivery.smp.server.security.SecurityConfigurationTest.PASSWORD;
import static eu.europa.ec.edelivery.smp.server.security.SecurityConfigurationTest.TEST_USERNAME_DB_HASHED_PASS;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ResourceControllerSubResourceTest extends AbstractControllerTest {

    public static final Logger LOG = LoggerFactory.getLogger(ResourceControllerSingleDomainTest.class);

    private static final String IDENTIFIER_SCHEME = "ehealth-participantid-qns";
    private static final String DOCUMENT_SCHEME = "doctype";
    public static final String GROUP_01 = "domain group";
    public static final String GROUP_02 = "Second group";
    public static final String GROUP_03 = "Third group";


    @BeforeEach
    public void setupController() throws IOException {
        // for database setup check the webapp_integration_test_data.sql
        super.setup();
    }


    /**
     * Test get permissions for resource with different creation parameters. The user data match
     * the data in the database: webapp_integration_test_data.sql
     */
    @ParameterizedTest
    @CsvSource({"'Get with same user as resource admin: OK', 200, pat_smp_admin, 123456, pat_smp_admin, 123456, ''",
            "'Non resource memeber is trying to get it ', 401, pat_smp_admin, 123456, test_pat_hashed_pass, 123456,''",
            "'Resource member with bad password, bad credentials: Fail', 401, pat_smp_admin, 123456, pat_smp_admin, 000000,''",
            "'Set same Owner as admin user: OK', 200, pat_smp_admin, 123456, pat_smp_admin, 123456,'pat_smp_admin'",
            "'Set resource owner user: OK', 200, test_pat_hashed_pass, 123456, test_pat_hashed_pass, 123456,'test_user_hashed_pass'",
            "'Legacy: Set owner user, but default admin owner deletes: OK', 200, test_pat_hashed_pass, 123456, pat_smp_admin, 123456, 'test_pat_hashed_pass'",
    })
    void getPrivateSubResourcePermissions(String desc, int expectedStatus,
                                          String resourceAdminCreateATId, String resourceCreateATSecret,
                                          String getUserATId, String getUserPassword,
                                          String resourceOwnerId) throws Exception {
        LOG.info(desc);

        String xmlSG = getSampleServiceGroupBody(IDENTIFIER_SCHEME, PARTICIPANT_ID);
        String xmlMD = generateServiceMetadata(PARTICIPANT_ID, IDENTIFIER_SCHEME, DOCUMENT_ID, DOCUMENT_SCHEME, "test");
        // owner headers
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(WebConstants.HTTP_PARAM_RESOURCE_VISIBILITY, VisibilityType.PRIVATE.name());
        if (StringUtils.isNotBlank(resourceOwnerId)) {
            httpHeaders.add(WebConstants.HTTP_PARAM_ADMIN, resourceOwnerId);
        }
        // crate service group
        mvc.perform(put(URL_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .headers(httpHeaders)
                        .contentType(APPLICATION_XML_VALUE)
                        .content(xmlSG))
                .andExpect(status().isCreated());
        // add subresource/service-metadata with appropriate owner
        mvc.perform(put(URL_DOC_PATH)
                        .with(httpBasic(resourceAdminCreateATId, resourceCreateATSecret))
                        .contentType(APPLICATION_XML_VALUE)
                        .content(xmlMD))
                .andExpect(status().isCreated());

        // get subresource/service-metadata with test owner
        mvc.perform(get(URL_DOC_PATH)
                        .with(httpBasic(getUserATId, getUserPassword)))
                .andExpect(status().is(expectedStatus));
    }


    /**
     * Test update permissions for resource with different creation parameters. The user data match
     * the data in the database: webapp_integration_test_data.sql
     */
    @ParameterizedTest
    @CsvSource({"'Resource owner is admin: OK', 201, pat_smp_admin, 123456,''",
            "'Resource owner, but user updates: Fail', 401, test_pat_hashed_pass, 123456,''",
            "'Default owner is admin, bad credentials: Fail', 401, pat_smp_admin, 000000,''",
            "'Set owner is same group admin: OK', 201, pat_smp_admin, 123456,'pat_smp_admin'",
            "'Set owner user: OK', 201, test_pat_hashed_pass, 123456,'test_pat_hashed_pass'",
            "'Set owner username: OK', 201, test_pat_hashed_pass, 123456,'test_user_hashed_pass'",
            "'Set owner user, but admin updates: Fail', 401, test_pat_hashed_pass, 123456,'pat_smp_admin'",
    })
    void createSubResourcePermissions(String desc, int expectedStatus,
                                      String resourceAdminATId, String resourceATSecret,
                                      String resourceOwnerId) throws Exception {
        LOG.info(desc);

        String xmlSG = getSampleServiceGroupBody(IDENTIFIER_SCHEME, PARTICIPANT_ID);
        String xmlMD = generateServiceMetadata(PARTICIPANT_ID, IDENTIFIER_SCHEME, DOCUMENT_ID, DOCUMENT_SCHEME, "test");
        // owner headers
        HttpHeaders httpHeaders = new HttpHeaders();
        if (StringUtils.isNotBlank(resourceOwnerId)) {
            httpHeaders.add(WebConstants.HTTP_PARAM_ADMIN, resourceOwnerId);
        }
        // crate service group
        mvc.perform(put(URL_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .headers(httpHeaders)
                        .contentType(APPLICATION_XML_VALUE)
                        .content(xmlSG))
                .andExpect(status().isCreated());
        // add subresource/service-metadata
        mvc.perform(put(URL_DOC_PATH)
                        .with(httpBasic(resourceAdminATId, resourceATSecret))
                        .contentType(APPLICATION_XML_VALUE)
                        .content(xmlMD))
                .andExpect(status().is(expectedStatus));
    }

    /**
     * Test update permissions for resource with different creation parameters. The user data match
     * the data in the database: webapp_integration_test_data.sql
     */
    @ParameterizedTest
    @CsvSource({"'Resource owner is admin: OK', 200, pat_smp_admin, 123456, pat_smp_admin, 123456, ''",
            "'Admin is Resource owner, but user deletes: Fail', 401, pat_smp_admin, 123456, test_pat_hashed_pass, 123456,''",
            "'Default owner is admin, bad credentials: Fail', 401, pat_smp_admin, 123456, pat_smp_admin, 000000,''",
            "'Set owner is same group admin: OK', 200, pat_smp_admin, 123456, pat_smp_admin, 123456,'pat_smp_admin'",
            "'Set resource owner user: OK', 200, test_pat_hashed_pass, 123456, test_pat_hashed_pass, 123456,'test_pat_hashed_pass'",
            "'Set resource owner user: OK', 200, test_pat_hashed_pass, 123456, test_pat_hashed_pass, 123456,'test_user_hashed_pass'",
            "'Legacy: Set owner user, but default admin owner deletes: OK', 200, test_pat_hashed_pass, 123456, pat_smp_admin, 123456, 'test_pat_hashed_pass'",
    })
    void deleteSubResourcePermissions(String desc, int expectedStatus,
                                      String resourceAdminCreateATId, String resourceCreateATSecret,
                                      String deleteAdminCreateATId, String deleteCreateATSecret,
                                      String resourceOwnerId) throws Exception {
        LOG.info(desc);

        String xmlSG = getSampleServiceGroupBody(IDENTIFIER_SCHEME, PARTICIPANT_ID);
        String xmlMD = generateServiceMetadata(PARTICIPANT_ID, IDENTIFIER_SCHEME, DOCUMENT_ID, DOCUMENT_SCHEME, "test");
        // owner headers
        HttpHeaders httpHeaders = new HttpHeaders();
        if (StringUtils.isNotBlank(resourceOwnerId)) {
            httpHeaders.add(WebConstants.HTTP_PARAM_ADMIN, resourceOwnerId);
        }
        // crate service group
        mvc.perform(put(URL_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .headers(httpHeaders)
                        .contentType(APPLICATION_XML_VALUE)
                        .content(xmlSG))
                .andExpect(status().isCreated());
        // add subresource/service-metadata with appropriate owner
        mvc.perform(put(URL_DOC_PATH)
                        .with(httpBasic(resourceAdminCreateATId, resourceCreateATSecret))
                        .contentType(APPLICATION_XML_VALUE)
                        .content(xmlMD))
                .andExpect(status().isCreated());

        // delete subresource/service-metadata with test owner
        mvc.perform(delete(URL_DOC_PATH)
                        .with(httpBasic(deleteAdminCreateATId, deleteCreateATSecret)))
                .andExpect(status().is(expectedStatus));
    }

    /**
     * Test update permissions for resource with different creation parameters. The user data match
     * the data in the database: webapp_integration_test_data.sql
     */
    @ParameterizedTest
    @CsvSource({"'The right group: OK', 201, 'Third group'",
            "'Wrong group', 401, 'domain group'",
            "'Group not exists', 404, 'NotExits'",
            "'If group is not given it figures it out the right group', 201, ''",
    })
    void createSubResourceForGroupPermissions(String desc, int expectedStatus,
                                              String targetGroup) throws Exception {
        LOG.info(desc);

        String xmlSG = getSampleServiceGroupBody(IDENTIFIER_SCHEME, PARTICIPANT_ID);
        String xmlMD = generateServiceMetadata(PARTICIPANT_ID, IDENTIFIER_SCHEME, DOCUMENT_ID, DOCUMENT_SCHEME, "test");
        // owner headers and group
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(WebConstants.HTTP_PARAM_ADMIN, TEST_USERNAME_DB_HASHED_PASS);
        httpHeaders.add(WebConstants.HTTP_PARAM_RESOURCE_GROUP, GROUP_03);

        // crate service group
        mvc.perform(put(URL_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .headers(httpHeaders)
                        .contentType(APPLICATION_XML_VALUE)
                        .content(xmlSG))
                .andExpect(status().isCreated());
        // add subresource/service-metadata with owner user
        HttpHeaders httpHeadersTest = new HttpHeaders();
        httpHeadersTest.add(WebConstants.HTTP_PARAM_RESOURCE_GROUP, targetGroup);
        mvc.perform(put(URL_DOC_PATH)
                        .with(httpBasic(TEST_USERNAME_DB_HASHED_PASS, PASSWORD))
                        .headers(httpHeadersTest)
                        .contentType(APPLICATION_XML_VALUE)
                        .content(xmlMD))
                .andExpect(status().is(expectedStatus));
    }

    /**
     * Test update permissions for resource with different creation parameters. The user data match
     * the data in the database: webapp_integration_test_data.sql
     */
    @ParameterizedTest
    @CsvSource({"'The right group: OK', 200, 'Third group'",
            "'Wrong group not allowed', 401, 'domain group'",
            "'Group not exists', 404, 'NotExits'",
            "'If group is not given it figures it out the right group', 200, ''",
    })
    void deleteSubResourceForGroupPermissions(String desc, int expectedStatus,
                                              String targetGroup) throws Exception {
        LOG.info(desc);

        String xmlSG = getSampleServiceGroupBody(IDENTIFIER_SCHEME, PARTICIPANT_ID);
        String xmlMD = generateServiceMetadata(PARTICIPANT_ID, IDENTIFIER_SCHEME, DOCUMENT_ID, DOCUMENT_SCHEME, "test");
        // owner headers
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(WebConstants.HTTP_PARAM_ADMIN, TEST_USERNAME_DB_HASHED_PASS);
        httpHeaders.add(WebConstants.HTTP_PARAM_RESOURCE_GROUP, GROUP_03);
        // crate service group
        mvc.perform(put(URL_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .headers(httpHeaders)
                        .contentType(APPLICATION_XML_VALUE)
                        .content(xmlSG))
                .andExpect(status().isCreated());
        // add subresource/service-metadata with appropriate owner and group
        HttpHeaders httpHeadersTestCreateSubR = new HttpHeaders();
        httpHeadersTestCreateSubR.add(WebConstants.HTTP_PARAM_RESOURCE_GROUP, GROUP_03);
        mvc.perform(put(URL_DOC_PATH)
                        .with(httpBasic(TEST_USERNAME_DB_HASHED_PASS, PASSWORD))
                        .headers(httpHeadersTestCreateSubR)
                        .contentType(APPLICATION_XML_VALUE)
                        .content(xmlMD))
                .andExpect(status().isCreated());

        // delete subresource/service-metadata with test owner
        // add subresource/service-metadata with owner user
        HttpHeaders httpHeadersTest = new HttpHeaders();
        httpHeadersTest.add(WebConstants.HTTP_PARAM_RESOURCE_GROUP, targetGroup);
        mvc.perform(delete(URL_DOC_PATH)
                        .with(httpBasic(TEST_USERNAME_DB_HASHED_PASS, PASSWORD))
                        .headers(httpHeadersTest))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void existingSubResourceCanBeRetrievedByEverybodyNoDomain() throws Exception {

        String xmlSG = getSampleServiceGroupBody(IDENTIFIER_SCHEME, PARTICIPANT_ID);
        String xmlMD = generateServiceMetadata(PARTICIPANT_ID, IDENTIFIER_SCHEME, DOCUMENT_ID, DOCUMENT_SCHEME, "test");
        // crate service group
        mvc.perform(put(URL_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .contentType(APPLICATION_XML_VALUE)
                        .content(xmlSG))
                .andExpect(status().isCreated());
        // add service metadata

        mvc.perform(put(URL_DOC_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .contentType(APPLICATION_XML_VALUE)

                        .content(xmlMD))
                .andExpect(status().isCreated());

        MvcResult mr = mvc.perform(get(URL_PATH).header("X-Forwarded-Host", "ec.test.eu")
                .header("X-Forwarded-Port", "443")
                .header("X-Forwarded-Proto", "https")).andReturn();
        System.out.println(mr.getResponse().getContentAsString());
        mvc.perform(get(URL_PATH))
                .andExpect(content().xml("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ServiceGroup xmlns=\"http://docs.oasis-open.org/bdxr/ns/SMP/2016/05\" xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\"><ParticipantIdentifier scheme=\"ehealth-participantid-qns\">urn:poland:ncpb</ParticipantIdentifier><ServiceMetadataReferenceCollection><ServiceMetadataReference href=\"http://localhost/ehealth-participantid-qns%3A%3Aurn%3Apoland%3Ancpb/services/doctype%3A%3Ainvoice\"/></ServiceMetadataReferenceCollection></ServiceGroup>"));

    }
}
