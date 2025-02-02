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

import eu.europa.ec.edelivery.smp.server.security.SignatureUtil;
import eu.europa.ec.edelivery.smp.ui.AbstractControllerTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.io.InputStream;

import static eu.europa.ec.edelivery.smp.ServiceGroupBodyUtil.getSampleServiceGroupBodyWithScheme;
import static java.lang.String.format;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author gutowpa
 * @since 3.0
 */
public class ResourceControllerSingleDomainTest extends AbstractControllerTest {

    public static final Logger LOG = LoggerFactory.getLogger(ResourceControllerSingleDomainTest.class);

    private static final String SERVICE_GROUP_INPUT_BODY = getSampleServiceGroupBodyWithScheme(IDENTIFIER_SCHEME);
    private static final String HTTP_HEADER_KEY_DOMAIN = "Domain";
    private static final String HTTP_HEADER_KEY_SERVICE_GROUP_OWNER = "Resource-Admin";
    private static final String HTTP_HEADER_KEY_RESOURCE_GROUP = "Resource-Group";
    private static final String HTTP_HEADER_KEY_RESOURCE_VISIBILITY = "Resource-Visibility";
    private static final String OTHER_OWNER_NAME = "CN=EHEALTH_SMP_TEST_BRAZIL,O=European Commission,C=BE:48b681ee8e0dcc08";

    @BeforeEach
    void initApplication() throws IOException {
        super.setup();
    }

    @Test
    void adminCanCreateResourceNoDomain() throws Exception {
        mvc.perform(put(URL_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .contentType(APPLICATION_XML_VALUE)
                        .content(SERVICE_GROUP_INPUT_BODY))
                .andExpect(status().isCreated());
    }

    /**
     * Test update permissions for resource with different creation parameters. The user data match
     * the data in the database: webapp_integration_test_data.sql
     */
    @ParameterizedTest
    @CsvSource({"'Default owner is admin: OK', 200, pat_smp_admin, 123456,''",
            "'Default owner Admin, but user updates: Fail', 401, test_pat_hashed_pass, 123456,''",
            "'Default owner is admin, bad credentials: Fail', 401, pat_smp_admin, 000000,''",
            "'Set owner is same group admin: OK', 200, pat_smp_admin, 123456,'pat_smp_admin'",
            "'Set owner user: OK', 200, test_pat_hashed_pass, 123456,'test_pat_hashed_pass'",
            "'Set owner username: OK', 200, test_pat_hashed_pass, 123456,'test_user_hashed_pass'",
            "'Set owner user, but admin updates: Fail', 401, test_pat_hashed_pass, 123456,'pat_smp_admin'",
    })
    void groupAdminCanUpdateResourceNoDomain(String desc, int expectedStatus,
                                                 String resourceAdminATId, String groupResourceATSecret,
                                                 String resourceOwnerId) throws Exception {
        LOG.info(desc);
        // create service group by group admin
        HttpHeaders httpHeaders = new HttpHeaders();
        if (StringUtils.isNotBlank(resourceOwnerId)) {
            httpHeaders.add(HTTP_HEADER_KEY_SERVICE_GROUP_OWNER, resourceOwnerId);
        }

        mvc.perform(put(URL_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .contentType(APPLICATION_XML_VALUE)
                        .headers(httpHeaders)
                        .content(SERVICE_GROUP_INPUT_BODY))
                .andExpect(status().isCreated());
        // update service group by owner (if not given then owner is the same as creator)
        mvc.perform(put(URL_PATH)
                        .with(httpBasic(resourceAdminATId, groupResourceATSecret))
                        .contentType(APPLICATION_XML_VALUE)
                        .content(SERVICE_GROUP_INPUT_BODY))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void addResourceWithUTF8() throws Exception {
        String resourceLocation = "/input/ServiceGroupWithUTF8.xml";
        LOG.info(resourceLocation);

        InputStream inputStream = SignatureUtil.class.getResourceAsStream(resourceLocation);
        byte[] data = IOUtils.toByteArray(inputStream);

        mvc.perform(put(URL_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .contentType(APPLICATION_XML_VALUE)
                        .content(data))
                .andExpect(status().isCreated());
        // get service group
        mvc.perform(get(URL_PATH))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousUserCannotCreateResource() throws Exception {
        mvc.perform(put(URL_PATH)
                        .contentType(APPLICATION_XML_VALUE)
                        .content(SERVICE_GROUP_INPUT_BODY))
                .andExpect(status().isUnauthorized());

        mvc.perform(get(URL_PATH))
                .andExpect(status().isNotFound());
    }

    @Test
    void malformedInputReturnsBadRequestNoDomain() throws Exception {
        mvc.perform(put(URL_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .contentType(APPLICATION_XML_VALUE)
                        .content("malformed input XML"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidParticipantSchemeReturnsBadRequestNoDomain() throws Exception {

        String scheme = "length-exceeeeeeds-25chars";
        String urlPath = format("/%s::%s", scheme, PARTICIPANT_ID);

        mvc.perform(put(urlPath)
                        .with(ADMIN_CREDENTIALS)
                        .contentType(APPLICATION_XML_VALUE)
                        .content(getSampleServiceGroupBodyWithScheme(scheme)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creatingResourceUnderBadFormattedDomainReturnsBadRequestNoDomain() throws Exception {
        mvc.perform(put(URL_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .contentType(APPLICATION_XML_VALUE)
                        .header(HTTP_HEADER_KEY_DOMAIN, "not-existing-domain")
                        .content(SERVICE_GROUP_INPUT_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(stringContainsInOrder("FORMAT_ERROR")));
    }

    @Test
    void creatingResourceUnderNotExistingDomainReturnsBadRequestNoDomain() throws Exception {
        mvc.perform(put(URL_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .contentType(APPLICATION_XML_VALUE)
                        .header(HTTP_HEADER_KEY_DOMAIN, "notExistingDomain")
                        .content(SERVICE_GROUP_INPUT_BODY))
                .andExpect(status().isNotFound())
                .andExpect(content().string(stringContainsInOrder("NOT_FOUND")));
    }

    @Test
    void adminCanAssignNewResourceToOtherOwnerNoDomain() throws Exception {
        mvc.perform(put(URL_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .contentType(APPLICATION_XML_VALUE)
                        .header(HTTP_HEADER_KEY_SERVICE_GROUP_OWNER, OTHER_OWNER_NAME)
                        .content(SERVICE_GROUP_INPUT_BODY))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @CsvSource({"Null group,,201",
            "Empty group, '',201",
            "Existing group:,'domain group',201",
            "Existing group 2:,'Third group',201",
            "Not authorized for domain:,'Second group',401",
            "Not authorized for non existing domain:,'Not exists group',404"})
    void createResourceForGroup(String testDesc, String groupName, int httpCode) throws Exception {

        LOG.info(testDesc);
        // create service group by group admin
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HTTP_HEADER_KEY_SERVICE_GROUP_OWNER, OTHER_OWNER_NAME);
        httpHeaders.add(HTTP_HEADER_KEY_DOMAIN, "domain");
        if (groupName != null) {
            httpHeaders.add(HTTP_HEADER_KEY_RESOURCE_GROUP, groupName);
        }

        mvc.perform(put(URL_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .contentType(APPLICATION_XML_VALUE)
                        .headers(httpHeaders)
                        .content(SERVICE_GROUP_INPUT_BODY))
                .andExpect( status().is(httpCode) );
    }

    @ParameterizedTest
    @CsvSource({"Null Visibility,,201",
            "Empty Visibility, '',201",
            "Public Visibility:,'PUBLIC',201",
            "Private Visibility:,'PRIVATE',201",
            "Case insensitive Visibility:,'PRiVaTE',201",
            "Invalid Visibility:,'NotOKValue',400",
            })
    void createResourceWithVisibility(String testDesc, String visibility, int httpCode) throws Exception {

        LOG.info(testDesc);
        // create service group by group admin
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HTTP_HEADER_KEY_SERVICE_GROUP_OWNER, OTHER_OWNER_NAME);
        httpHeaders.add(HTTP_HEADER_KEY_DOMAIN, "domain");
        if (visibility != null) {
            httpHeaders.add(HTTP_HEADER_KEY_RESOURCE_VISIBILITY, visibility);
        }

        mvc.perform(put(URL_PATH)
                        .with(ADMIN_CREDENTIALS)
                        .contentType(APPLICATION_XML_VALUE)
                        .headers(httpHeaders)
                        .content(SERVICE_GROUP_INPUT_BODY))
                .andExpect( status().is(httpCode) );
    }
}
