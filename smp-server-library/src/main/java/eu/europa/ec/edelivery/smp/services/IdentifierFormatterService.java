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
package eu.europa.ec.edelivery.smp.services;


import eu.europa.ec.dynamicdiscovery.enums.DNSLookupFormatType;
import eu.europa.ec.dynamicdiscovery.model.identifiers.types.EBCorePartyIdFormatterType;
import eu.europa.ec.dynamicdiscovery.model.identifiers.types.TemplateFormatterType;
import eu.europa.ec.edelivery.smp.config.enums.SMPDomainPropertyEnum;
import eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum;
import eu.europa.ec.edelivery.smp.config.enums.SMPPropertyTypeEnum;
import eu.europa.ec.edelivery.smp.data.dao.DomainConfigurationDao;
import eu.europa.ec.edelivery.smp.data.dao.DomainDao;
import eu.europa.ec.edelivery.smp.data.model.DBDomain;
import eu.europa.ec.edelivery.smp.data.model.DBDomainConfiguration;
import eu.europa.ec.edelivery.smp.exceptions.ErrorCode;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import eu.europa.ec.edelivery.smp.identifiers.IdentifierFormatter;
import eu.europa.ec.edelivery.smp.utils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Spring bean  provides Identifier formatters for the domain.
 *
 * @since 5.1
 */
@Component
public class IdentifierFormatterService {

    public static final String CACHE_NAME_DOMAIN_RESOURCE_IDENTIFIER_FORMATTER = "domain-resource-identifier-formatter";
    public static final String CACHE_NAME_DOMAIN_SUBRESOURCE_IDENTIFIER_FORMATTER = "domain-subresource-identifier-formatter";

    private static final Logger LOG = LoggerFactory.getLogger(IdentifierFormatterService.class);

    private final DomainDao domainDao;
    private final DomainConfigurationDao domainConfigurationDao;
    private final ConfigurationService configurationService;

    public IdentifierFormatterService(DomainDao domainDao,
                                      DomainConfigurationDao domainConfigurationDao,
                                      ConfigurationService configurationService) {
        this.domainDao = domainDao;
        this.domainConfigurationDao = domainConfigurationDao;
        this.configurationService = configurationService;
    }

    /**
     * Method returns participant identifier formatter for given domain. If the
     * domain code is empty, default resource identifier formatter is returned.
     *
     * @param domainCode domain code to get IdentifierFormatter
     * @return IdentifierFormatter for given domain code or default resource identifier formatter.
     * @throws SMPRuntimeException if domain is not found
     */
    @Cacheable(CACHE_NAME_DOMAIN_RESOURCE_IDENTIFIER_FORMATTER)
    public IdentifierFormatter getResourceIdentifierFormatter(String domainCode) {

        if (StringUtils.isBlank(domainCode)) {
            LOG.warn("Domain code is empty. Using default resource identifier formatter!");
            return getDefaultResourceIdentifierFormatter();
        }
        DBDomain domain = domainDao.getDomainByCode(domainCode)
                .orElseThrow(() -> new SMPRuntimeException(ErrorCode.DOMAIN_NOT_EXISTS, domainCode));

        IdentifierFormatter.Builder builder = IdentifierFormatter.Builder
                .create()
                .addFormatterTypes(new EBCorePartyIdFormatterType());

        // template for formating the identifier
        List<DBDomainConfiguration> listDomainConf = domainConfigurationDao.getDomainConfiguration(domain);
        TemplateFormatterType templateFormatterType = createTemplateFormatterType(listDomainConf, domainCode);
        if (templateFormatterType != null) {
            builder.addFormatterTypes(templateFormatterType);
        }
        builder.addFormatterTypes(new EBCorePartyIdFormatterType());

        IdentifierFormatter identifierFormatter = builder.build();
        identifierFormatter.setCaseSensitiveSchemas(getDomainConfigurationValue(listDomainConf, SMPDomainPropertyEnum.RESOURCE_CASE_SENSITIVE_SCHEMES));
        Boolean mandatory = getDomainConfigurationValue(listDomainConf, SMPDomainPropertyEnum.RESOURCE_SCH_MANDATORY);
        identifierFormatter.setSchemeMandatory(Boolean.TRUE.equals(mandatory));
        identifierFormatter.setSchemeValidationPattern(getDomainConfigurationValue(listDomainConf, SMPDomainPropertyEnum.RESOURCE_SCH_VALIDATION_REGEXP));

        return identifierFormatter;
    }

    /**
     * Method returns default resource identifier formatter using the system configuration.
     *
     * @return default resource identifier formatter
     */
    private IdentifierFormatter getDefaultResourceIdentifierFormatter() {
        IdentifierFormatter.Builder builder = IdentifierFormatter.Builder
                .create()
                .addFormatterTypes(new EBCorePartyIdFormatterType());

        Pattern matchRegExp = configurationService.getParticipantIdentifierTmplMatchRexExp();
        Pattern splitRegExp = configurationService.getParticipantIdentifierTmplSplitRexExp();
        String formatTemplate = configurationService.getParticipantIdentifierTmplConcatenate();
        String formatNullTemplate = configurationService.getParticipantIdentifierTmplConcatenateSchemeNull();

        TemplateFormatterType templateFormatterType = createTemplateFormatterType(matchRegExp, splitRegExp, formatTemplate, formatNullTemplate, "system-default");
        if (templateFormatterType != null) {
            builder.addFormatterTypes(templateFormatterType);
        }

        IdentifierFormatter identifierFormatter = builder.build();
        identifierFormatter.setCaseSensitiveSchemas(configurationService.getCaseSensitiveParticipantScheme());
        identifierFormatter.setSchemeMandatory(configurationService.getParticipantSchemeMandatory());
        identifierFormatter.setSchemeValidationPattern(configurationService.getParticipantIdentifierSchemeRexExp());

        return identifierFormatter;
    }

    TemplateFormatterType createTemplateFormatterType(List<DBDomainConfiguration> listDomainConf,  String domainCode) {
        // template for formating the identifier
        Pattern matchRegExp = getDomainConfigurationValue(listDomainConf, SMPDomainPropertyEnum.RESOURCE_IDENTIFIER_TMPL_MATCH_REGEXP);
        Pattern splitRegExp = getDomainConfigurationValue(listDomainConf, SMPDomainPropertyEnum.RESOURCE_IDENTIFIER_TMPL_SPLIT_REGEXP);
        String formatTemplate = getDomainConfigurationValue(listDomainConf, SMPDomainPropertyEnum.RESOURCE_IDENTIFIER_TMPL_CONCATENATE);
        String formatNullTemplate = getDomainConfigurationValue(listDomainConf, SMPDomainPropertyEnum.RESOURCE_IDENTIFIER_TMPL_CONCATENATE_NULL_SCHEME);

        return createTemplateFormatterType(matchRegExp, splitRegExp, formatTemplate, formatNullTemplate, domainCode);
    }

    TemplateFormatterType createTemplateFormatterType( Pattern matchRegExp, Pattern splitRegExp, String formatTemplate, String formatNullTemplate, String domainCode) {
        // template for formating the identifier
        if (matchRegExp == null ||  splitRegExp == null) {
            LOG.info("TemplateFormatterType for domain [{}] not be created. One of the required parameters is empty: " +
                            "matchRegExp: [{}], splitRegExp: [{}], formatTemplate: [{}],formatNullTemplate: [{}]",
                    domainCode, matchRegExp, splitRegExp, formatTemplate, formatNullTemplate);
            return null;
        }
        return new TemplateFormatterType(matchRegExp, formatTemplate, formatNullTemplate, splitRegExp, DNSLookupFormatType.ALL_IN_HASH);
    }

    /**
     * Method returns participant identifier formatter for given domain
     *
     * @param domainCode domain code to get IdentifierFormatter
     */
    @Cacheable(CACHE_NAME_DOMAIN_SUBRESOURCE_IDENTIFIER_FORMATTER)
    public IdentifierFormatter getSubresourceIdentifierFormatter(String domainCode) {

        if (StringUtils.isBlank(domainCode)) {
            throw new SMPRuntimeException(ErrorCode.DOMAIN_NOT_EXISTS, domainCode);
        }
        DBDomain domain = domainDao.getDomainByCode(domainCode)
                .orElseThrow(() -> new SMPRuntimeException(ErrorCode.DOMAIN_NOT_EXISTS, domainCode));
        List<DBDomainConfiguration> listDomainConf = domainConfigurationDao.getDomainConfiguration(domain);
        IdentifierFormatter identifierFormatter = IdentifierFormatter.Builder
                .create()
                .build();

        identifierFormatter.setCaseSensitiveSchemas(getDomainConfigurationValue(listDomainConf,
                SMPDomainPropertyEnum.SUBRESOURCE_CASE_SENSITIVE_SCHEMES));
        return identifierFormatter;
    }

    /**
     * Method returns parsed value for  property on given domain. If property is not found or use system default,
     * system default value is returned.
     *
     * @param domain   domain to get configuration value
     * @param property domain property type
     * @param <T>      type of returned value
     * @return parsed value for property
     */
    public <T> T getDomainConfigurationValue(List<DBDomainConfiguration> domain, SMPDomainPropertyEnum property) {

        DBDomainConfiguration domainConfiguration = domain.stream()
                .filter(dc -> dc.getProperty().equals(property.getProperty()))
                .findFirst()
                .orElse(null);

        if (domainConfiguration == null || domainConfiguration.isUseSystemDefault()) {
            LOG.debug("Domain configuration value for property [{}] not found or use system default. Using system default value!", property);
            return configurationService.getDefaultDomainConfigurationValue(property);
        }

        String value = domainConfiguration.getValue();
        SMPPropertyEnum sysPropType = getSmpPropertyEnum(property);
        return (T) PropertyUtils.parseProperty(sysPropType, value, null);

    }

    private static SMPPropertyEnum getSmpPropertyEnum(SMPDomainPropertyEnum property) {
        SMPPropertyEnum sysPropType = property.getPropertyEnum();
        if (sysPropType.isEncrypted()) {
            throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Encrypted domain Properties are not supported!. Can not parse   ["
                    + property + "]!");
        }
        if (sysPropType.getPropertyType() == SMPPropertyTypeEnum.PATH ||
                sysPropType.getPropertyType() == SMPPropertyTypeEnum.FILENAME) {
            throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Path or filename domain properties are not supported!. Can not parse   ["
                    + property + "]!");
        }
        return sysPropType;
    }
}
