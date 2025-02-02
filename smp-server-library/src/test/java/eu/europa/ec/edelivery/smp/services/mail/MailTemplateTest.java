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
package eu.europa.ec.edelivery.smp.services.mail;

import eu.europa.ec.edelivery.smp.data.ui.enums.AlertTypeEnum;
import eu.europa.ec.edelivery.smp.services.ConfigurationService;
import eu.europa.ec.edelivery.smp.services.SMPLanguageResourceService;
import eu.europa.ec.edelivery.smp.services.mail.prop.CredentialsExpirationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MailTemplateTest {

    File localeFolder = new File("target/locales");
    ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
    ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    SMPLanguageResourceService smpLanguageResourceService = new SMPLanguageResourceService(configurationService, resourcePatternResolver);

    MailTemplateService testInstance = new MailTemplateService(smpLanguageResourceService);

    @BeforeEach
    void setUp() throws IOException {
        // clean the folder
        if (localeFolder.exists() ) {
            // first delete all files and then empty folders
            Files.walk(localeFolder.toPath())
                    .map( Path::toFile )
                    .sorted( Comparator.comparing( File::isDirectory ) )
                    .forEach( File::delete );
        }
        Mockito.when(configurationService.getLocaleFolder()).thenReturn(localeFolder);
    }

    @Test
    void getMailContent() {

        Map<String, Object> props = new HashMap<>();
        props.put(CredentialsExpirationProperties.ALERT_LEVEL.name(), "alert level");
        props.put(CredentialsExpirationProperties.CREDENTIAL_ID.name(), "credential id");
        props.put(CredentialsExpirationProperties.CREDENTIAL_TYPE.name(), "credential name");
        props.put(CredentialsExpirationProperties.EXPIRATION_DATETIME.name(), "expiration date");
        props.put(CredentialsExpirationProperties.REPORTING_DATETIME.name(), "reporting date");
        props.put(CredentialsExpirationProperties.SERVER_NAME.name(), "server name");

        MailDataModel model = new MailDataModel("en", AlertTypeEnum.CREDENTIAL_EXPIRED, props);

        String result = testInstance.getMailHtmlContent(model);
        assertNotNull(result);
        assertTrue(result.contains("alert level"));
        assertTrue(result.contains("credential id"));
        assertTrue(result.contains("credential name"));
    }
}
