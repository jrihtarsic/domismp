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

import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.services.SMPLanguageResourceService;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import static eu.europa.ec.edelivery.smp.services.SMPLanguageResourceService.LANGUAGE_FILENAME_UI_PREFIX;
import static eu.europa.ec.edelivery.smp.services.SMPLanguageResourceService.LANGUAGE_RESOURCE_UI_DEFAULT;
import static eu.europa.ec.edelivery.smp.ui.ResourceConstants.CONTEXT_PATH_PUBLIC_LOCALE;

/**
 * Provides support for returning locale files required by the ngx-translation Angular library.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.1
 */
@RestController
@RequestMapping(value = CONTEXT_PATH_PUBLIC_LOCALE)
public class LocaleController {
    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(LocaleController.class);

    private final SMPLanguageResourceService smpLocaleService;

    public LocaleController(SMPLanguageResourceService smpLocaleService) {
        this.smpLocaleService = smpLocaleService;
    }

    @GetMapping(value = "/{code}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Resource getLanguage(@PathVariable("code") String code) {
        LOG.debug("Requesting locale file for code: [{}]", code);
        return smpLocaleService.getTranslationResourceFile(LANGUAGE_FILENAME_UI_PREFIX, code, LANGUAGE_RESOURCE_UI_DEFAULT);
    }
}
