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
package eu.europa.ec.edelivery.smp.ui.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum;
import eu.europa.ec.edelivery.smp.data.dao.ConfigurationDao;
import eu.europa.ec.edelivery.smp.data.dao.DomainDao;
import eu.europa.ec.edelivery.smp.data.ui.DomainRO;
import eu.europa.ec.edelivery.smp.data.ui.ServiceResult;
import eu.europa.ec.edelivery.smp.test.SmpTestWebAppConfig;
import eu.europa.ec.edelivery.smp.test.testutils.MockMvcUtils;
import eu.europa.ec.edelivery.smp.test.testutils.X509CertificateTestUtils;
import eu.europa.ec.edelivery.smp.ui.ResourceConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SmpTestWebAppConfig.class})
@Sql(scripts = {
        "/cleanup-database.sql",
        "/webapp_integration_test_data.sql"})
class DomainResourceIT {
    private static final String PATH = ResourceConstants.CONTEXT_PATH_PUBLIC_DOMAIN;

    @Autowired
    private WebApplicationContext webAppContext;
    @Autowired
    DomainDao domainDao;
    @Autowired
    private ConfigurationDao configurationDao;


    private MockMvc mvc;

    @BeforeEach
    public void setup() throws IOException {
        X509CertificateTestUtils.reloadKeystores();
        configurationDao.setPropertyToDatabase(SMPPropertyEnum.EXTERNAL_TLS_AUTHENTICATION_CLIENT_CERT_HEADER_ENABLED, "true", null);
        configurationDao.reloadPropertiesFromDatabase();
        mvc = MockMvcUtils.initializeMockMvc(webAppContext);
        configurationDao.contextRefreshedEvent();
    }

    @Test
    void geDomainPublicList() throws Exception {

        // given when
        MvcResult result = mvc.perform(get(PATH)
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        //then
        ObjectMapper mapper = new ObjectMapper();
        ServiceResult res = mapper.readValue(result.getResponse().getContentAsString(), ServiceResult.class);


        assertNotNull(res);
        // there are two  public domains in the database but only one has resources
        // see EDELIVERY-13793
        assertEquals(1, res.getServiceEntities().size());
        res.getServiceEntities().forEach(sgMap -> {
            DomainRO sgro = mapper.convertValue(sgMap, DomainRO.class);
            assertNotNull(sgro.getDomainCode());
            assertNotNull(sgro.getSmlSubdomain());
            // for public endpoint all other data must be null!
            assertNull(sgro.getDomainId());
            assertNull(sgro.getSmlSmpId());
            assertNull(sgro.getSignatureKeyAlias());
        });
    }
}
