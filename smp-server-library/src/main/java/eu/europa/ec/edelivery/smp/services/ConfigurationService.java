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

import eu.europa.ec.edelivery.smp.auth.enums.SMPUserAuthenticationTypes;
import eu.europa.ec.edelivery.smp.config.enums.SMPDomainPropertyEnum;
import eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum;
import eu.europa.ec.edelivery.smp.data.dao.ConfigurationDao;
import eu.europa.ec.edelivery.smp.data.ui.auth.SMPAuthority;
import eu.europa.ec.edelivery.smp.data.ui.enums.AlertLevelEnum;
import eu.europa.ec.edelivery.smp.data.ui.enums.AlertSuspensionMomentEnum;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum.*;


@Service
public class ConfigurationService {

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(ConfigurationService.class);

    private final ConfigurationDao configurationDAO;

    public ConfigurationService(ConfigurationDao configurationDAO) {
        this.configurationDAO = configurationDAO;
    }


    public String getDefaultDomainCode(){
        return configurationDAO.getCachedPropertyValue(DEFAULT_DOMAIN);
    }


    public Pattern getParticipantIdentifierSchemeRexExp() {
        return configurationDAO.getCachedPropertyValue(RESOURCE_SCH_VALIDATION_REGEXP);
    }

    public String getParticipantIdentifierSchemeRexExpPattern() {
        return configurationDAO.getCachedProperty(RESOURCE_SCH_VALIDATION_REGEXP);
    }

    public String getParticipantIdentifierSchemeRexExpMessage() {
        return configurationDAO.getCachedPropertyValue(RESOURCE_SCH_REGEXP_MSG);
    }

    public Pattern getPasswordPolicyRexExp() {
        return configurationDAO.getCachedPropertyValue(PASSWORD_POLICY_REGULAR_EXPRESSION);
    }

    public String getPasswordPolicyRexExpPattern() {
        return configurationDAO.getCachedProperty(PASSWORD_POLICY_REGULAR_EXPRESSION);
    }

    public String getPasswordPolicyValidationMessage() {
        return configurationDAO.getCachedProperty(PASSWORD_POLICY_MESSAGE);
    }

    public Integer getPasswordPolicyValidDays() {
        return configurationDAO.getCachedPropertyValue(PASSWORD_POLICY_VALID_DAYS);
    }

    public Integer getPasswordPolicyUIWarningDaysBeforeExpire() {
        return configurationDAO.getCachedPropertyValue(PASSWORD_POLICY_WARNING_DAYS_BEFORE_EXPIRE);
    }

    public Boolean getPasswordPolicyForceChangeIfExpired() {
        return configurationDAO.getCachedPropertyValue(PASSWORD_POLICY_FORCE_CHANGE_EXPIRED);
    }

    public Integer getAccessTokenPolicyValidDays() {
        return configurationDAO.getCachedPropertyValue(ACCESS_TOKEN_POLICY_VALID_DAYS);
    }

    public Integer getLoginMaxAttempts() {
        return configurationDAO.getCachedPropertyValue(USER_MAX_FAILED_ATTEMPTS);
    }

    public Integer getLoginSuspensionTimeInSeconds() {
        return configurationDAO.getCachedPropertyValue(USER_SUSPENSION_TIME);
    }

    public Integer getLoginFailDelayInMilliSeconds() {
        Integer delay = configurationDAO.getCachedPropertyValue(USER_LOGIN_FAIL_DELAY);
        return delay == null ? 1000 : delay;
    }

    public Integer getAccessTokenLoginMaxAttempts() {
        return configurationDAO.getCachedPropertyValue(ACCESS_TOKEN_MAX_FAILED_ATTEMPTS);
    }

    public Integer getAccessTokenLoginSuspensionTimeInSeconds() {
        return configurationDAO.getCachedPropertyValue(ACCESS_TOKEN_SUSPENSION_TIME);
    }

    public Integer getAccessTokenLoginFailDelayInMilliSeconds() {
        Integer delay = configurationDAO.getCachedPropertyValue(ACCESS_TOKEN_FAIL_DELAY);
        return delay == null ? 1000 : delay;
    }

    public Integer getHttpHeaderHstsMaxAge() {
        return configurationDAO.getCachedPropertyValue(HTTP_HSTS_MAX_AGE);
    }

    public String getHttpHeaderContentSecurityPolicy() {
        return configurationDAO.getCachedPropertyValue(HTTP_HEADER_SEC_POLICY);
    }

    public String getHttpProxyHost() {
        return configurationDAO.getCachedProperty(HTTP_PROXY_HOST);
    }

    public String getHttpNoProxyHosts() {
        return configurationDAO.getCachedProperty(HTTP_NO_PROXY_HOSTS);
    }

    public Optional<Integer> getHttpProxyPort() {
        Integer intVal = configurationDAO.getCachedPropertyValue(HTTP_PROXY_PORT);
        return Optional.ofNullable(intVal);
    }

    public java.net.URL getSMLIntegrationUrl() {
        return configurationDAO.getCachedPropertyValue(SML_URL);
    }

    public String getProxyUsername() {
        return configurationDAO.getCachedPropertyValue(HTTP_PROXY_USER);
    }

    public String getProxyCredentialToken() {
        return configurationDAO.getCachedPropertyValue(HTTP_PROXY_PASSWORD);
    }

    public List<String> getCaseSensitiveDocumentScheme() {
        return configurationDAO.getCachedPropertyValue(SUBRESOURCE_CASE_SENSITIVE_SCHEMES);
    }

    public List<String> getCaseSensitiveParticipantScheme() {
        return configurationDAO.getCachedPropertyValue(RESOURCE_CASE_SENSITIVE_SCHEMES);
    }


    public boolean getParticipantSchemeMandatory() {
        // not mandatory by default
        Boolean value = configurationDAO.getCachedPropertyValue(RESOURCE_SCH_MANDATORY);
        return value != null && value;
    }

    public Pattern getParticipantIdentifierTmplSplitRexExp() {
        return configurationDAO.getCachedPropertyValue(RESOURCE_IDENTIFIER_TMPL_SPLIT_REGEXP);
    }

    public Pattern getParticipantIdentifierTmplMatchRexExp() {
        return configurationDAO.getCachedPropertyValue(RESOURCE_IDENTIFIER_TMPL_MATCH_REGEXP);
    }

    public String getParticipantIdentifierTmplConcatenate() {
        return configurationDAO.getCachedPropertyValue(RESOURCE_IDENTIFIER_TMPL_CONCATENATE);
    }

    public String getParticipantIdentifierTmplConcatenateSchemeNull() {
        return configurationDAO.getCachedPropertyValue(RESOURCE_IDENTIFIER_TMPL_CONCATENATE_NULL_SCHEME);
    }

    public boolean isProxyEnabled() {
        String proxyHost = configurationDAO.getCachedProperty(HTTP_PROXY_HOST);
        return !StringUtils.isBlank(proxyHost);
    }

    public boolean isSMLIntegrationEnabled() {
        Boolean value = configurationDAO.getCachedPropertyValue(SML_ENABLED);
        return value != null && value;
    }

    public boolean isUrlContextEnabled() {
        Boolean value = configurationDAO.getCachedPropertyValue(OUTPUT_CONTEXT_PATH);
        // by default is true - return false only in case is declared in configuration
        return value == null || value;
    }

    public boolean isClusterEnabled() {
        Boolean value = configurationDAO.getCachedPropertyValue(SMP_CLUSTER_ENABLED);
        return value != null && value;
    }

    public boolean encodedSlashesAllowedInUrl() {
        Boolean value = configurationDAO.getCachedPropertyValue(ENCODED_SLASHES_ALLOWED_IN_URL);
        // by default is true - return false only in case is declared in configuration
        return value == null || value;
    }

    public String getTargetServerForCredentialValidation() {
        return configurationDAO.getCachedPropertyValue(SMP_ALERT_CREDENTIALS_SERVER);
    }

    public String getSMLIntegrationSMPLogicalAddress() {
        return configurationDAO.getCachedProperty(SML_LOGICAL_ADDRESS);
    }

    public String getSMLIntegrationSMPPhysicalAddress() {
        return configurationDAO.getCachedProperty(SML_PHYSICAL_ADDRESS);
    }

    public boolean forceCRLValidation() {
        Boolean value = configurationDAO.getCachedPropertyValue(CERTIFICATE_CRL_FORCE);
        // by default is not forced -> if missing is false!
        return value != null && value;
    }

    public boolean isExternalTLSAuthenticationWithClientCertHeaderEnabled() {
        Boolean value = configurationDAO.getCachedPropertyValue(SMPPropertyEnum.EXTERNAL_TLS_AUTHENTICATION_CLIENT_CERT_HEADER_ENABLED);
        // by default is not forced -> if missing is false!
        return value != null && value;
    }

    public boolean isExternalTLSAuthenticationWithSSLClientCertHeaderEnabled() {
        Boolean value = configurationDAO.getCachedPropertyValue(SMPPropertyEnum.EXTERNAL_TLS_AUTHENTICATION_CERTIFICATE_HEADER_ENABLED);
        // by default is not forced -> if missing is false!
        return value != null && value;
    }


    public Pattern getCertificateSubjectRegularExpression() {
        return configurationDAO.getCachedPropertyValue(CERTIFICATE_SUBJECT_REGULAR_EXPRESSION);
    }

    public List<String> getAllowedCertificatePolicies() {
        return configurationDAO.getCachedPropertyValue(CERTIFICATE_ALLOWED_CERTIFICATEPOLICY_OIDS);
    }

    public List<String> getAllowedCertificateKeyTypes() {
        return configurationDAO.getCachedPropertyValue(CERTIFICATE_ALLOWED_KEY_TYPES);
    }

    public String getSMLIntegrationServerCertSubjectRegExpPattern() {
        return configurationDAO.getCachedProperty(SML_TLS_SERVER_CERT_SUBJECT_REGEXP);
    }

    public Pattern getSMLIntegrationServerCertSubjectRegExp() {
        return configurationDAO.getCachedPropertyValue(SML_TLS_SERVER_CERT_SUBJECT_REGEXP);
    }

    public boolean useSystemTruststoreForTLS() {
        Boolean value = configurationDAO.getCachedPropertyValue(SML_TLS_TRUSTSTORE_USE_SYSTEM_DEFAULT);
        // by default is not forced
        return value != null && value;
    }

    public boolean smlDisableCNCheck() {
        Boolean value = configurationDAO.getCachedPropertyValue(SML_TLS_DISABLE_CN_CHECK);
        // by default is not forced
        return value != null && value;
    }

    public File getSecurityFolder() {
        return  configurationDAO.getSecurityFolder();
    }

    public File getLocaleFolder() {
        return configurationDAO.getLocaleFolder();
    }

    public File getTruststoreFile() {
        return configurationDAO.getCachedPropertyValue(TRUSTSTORE_FILENAME);
    }

    public String getTruststoreType() {
        return configurationDAO.getCachedPropertyValue(TRUSTSTORE_TYPE);
    }

    public File getKeystoreFile() {
        return configurationDAO.getCachedPropertyValue(KEYSTORE_FILENAME);
    }

    public String getKeystoreType() {
        return configurationDAO.getCachedPropertyValue(KEYSTORE_TYPE);
    }

    public String getTruststoreCredentialToken() {
        return configurationDAO.getCachedPropertyValue(TRUSTSTORE_PASSWORD);
    }

    public String getKeystoreCredentialToken() {
        return configurationDAO.getCachedPropertyValue(KEYSTORE_PASSWORD);
    }

    public boolean getSessionCookieSecure() {
        Boolean value = configurationDAO.getCachedPropertyValue(UI_COOKIE_SESSION_SECURE);
        return value != null && value;
    }

    public Integer getSessionCookieMaxAge() {
        return configurationDAO.getCachedPropertyValue(UI_COOKIE_SESSION_MAX_AGE);
    }

    public String getSessionCookieSameSite() {
        return configurationDAO.getCachedPropertyValue(UI_COOKIE_SESSION_SITE);
    }

    public String getSessionCookiePath() {
        return configurationDAO.getCachedPropertyValue(UI_COOKIE_SESSION_PATH);
    }

    public int getSessionTimeoutForRoles(Collection<? extends GrantedAuthority> authorities) {
        boolean hasAdminRole = false;
        if (authorities != null) {
            hasAdminRole = authorities.stream().anyMatch(grantedAuthority ->
                    StringUtils.equalsIgnoreCase(grantedAuthority.getAuthority(), SMPAuthority.S_AUTHORITY_SYSTEM_ADMIN.getAuthority()));
        }
        LOG.debug("Has admin role [{}]", hasAdminRole);
        return hasAdminRole ? getSessionIdleTimeoutForAdmin(): getSessionIdleTimeoutForUser();
    }

    public Integer getSessionIdleTimeoutForAdmin() {
        return configurationDAO.getCachedPropertyValue(UI_COOKIE_SESSION_IDLE_TIMEOUT_ADMIN);
    }

    public Integer getSessionIdleTimeoutForUser() {
        return configurationDAO.getCachedPropertyValue(UI_COOKIE_SESSION_IDLE_TIMEOUT_USER);
    }

    public boolean isSSOEnabledForUserAuthentication() {
        List<String> userAuthenticationTypes = getUIAuthenticationTypes();
        return userAuthenticationTypes != null && userAuthenticationTypes.contains(SMPUserAuthenticationTypes.SSO.name());
    }

    public String getCasUILabel() {
        return configurationDAO.getCachedPropertyValue(SSO_CAS_UI_LABEL);
    }

    public java.net.URL getCasURL() {
        return configurationDAO.getCachedPropertyValue(SSO_CAS_URL);
    }

    public java.net.URL getCasCallbackUrl() {
        return configurationDAO.getCachedPropertyValue(SSO_CAS_CALLBACK_URL);
    }

    public boolean isCasAutomaticRegistrationEnabledForUserAuthentication() {
        Boolean value = configurationDAO.getCachedPropertyValue(SSO_CAS_AUTOMATIC_REGISTRATION_ENABLED);
        return value != null && value;
    }

    public boolean isCasAutomaticRegistrationConfirmation() {
        Boolean value = configurationDAO.getCachedPropertyValue(SSO_CAS_AUTOMATIC_REGISTRATION_CONFIRMATION);
        return value != null && value;
    }

    public Map<String, String> getCasAutomaticRegistrationDataMapping() {
        return configurationDAO.getCachedPropertyValue(SSO_CAS_AUTOMATIC_REGISTRATION_PROPERTY_MAPPING);
    }

    public String getCasSMPLoginRelativePath() {
        return configurationDAO.getCachedPropertyValue(SSO_CAS_SMP_LOGIN_URI);
    }

    public String getCasURLPathLogin() {
        return configurationDAO.getCachedPropertyValue(SSO_CAS_URL_PATH_LOGIN);
    }

    public String getCasURLTokenValidation() {
        return configurationDAO.getCachedPropertyValue(SSO_CAS_TOKEN_VALIDATION_URL_PATH);
    }

    public URL getCasUserDataURL() {
        URL casUrl = getCasURL();
        if (casUrl == null) {
            LOG.warn("Invalid CAS configuration [{}]. Can not resolve user data URL!", SSO_CAS_URL.getProperty());
            return null;
        }
        String path = configurationDAO.getCachedPropertyValue(SSO_CAS_SMP_USER_DATA_URL_PATH);
        if (StringUtils.isBlank(path)) {
            LOG.warn("Invalid CAS configuration [{}]. Can not resolve user data URL!", SSO_CAS_SMP_USER_DATA_URL_PATH.getProperty());
            return null;
        }
        try {
            return casUrl.toURI().resolve(path).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            LOG.warn("Invalid CAS configuration [{}]. Can not resolve user data URL! Error: [{}]",
                    SSO_CAS_SMP_USER_DATA_URL_PATH.getProperty(),
                    ExceptionUtils.getRootCauseMessage(e));
        }
        return null;
    }


    public Map<String, String> getCasTokenValidationParams() {
        return configurationDAO.getCachedPropertyValue(SSO_CAS_TOKEN_VALIDATION_PARAMS);
    }

    public Map<String, String> getCustomNaptrServicesMap() {
        return configurationDAO.getCachedPropertyValue(SML_CUSTOM_NAPTR_SERVICE_PARAMS);
    }

    public int getManageMaxSMLRecordCount() {
        Integer intVal = configurationDAO.getCachedPropertyValue(SML_MANAGE_MAX_COUNT);
        return intVal == null ? 10000 : intVal;
    }


    public List<String> getCasURLTokenValidationGroups() {
        return configurationDAO.getCachedPropertyValue(SSO_CAS_TOKEN_VALIDATION_GROUPS);
    }

    public List<String> getUIAuthenticationTypes() {
        return configurationDAO.getCachedPropertyValue(UI_AUTHENTICATION_TYPES);
    }

    public List<String> getAutomationAuthenticationTypes() {
        return configurationDAO.getCachedPropertyValue(AUTOMATION_AUTHENTICATION_TYPES);
    }

    //-----------------------
    // before user suspended
    public Boolean getAlertUserLoginFailureEnabled() {
        return configurationDAO.getCachedPropertyValue(ALERT_USER_LOGIN_FAILURE_ENABLED);
    }

    public AlertLevelEnum getAlertUserLoginFailureLevel() {
        String level = configurationDAO.getCachedPropertyValue(ALERT_USER_LOGIN_FAILURE_LEVEL);
        return AlertLevelEnum.valueOf(level);
    }

    //-----------------------
    // user Created
    public Boolean getAlertUserCreatedEnabled() {
        return configurationDAO.getCachedPropertyValue(ALERT_USER_CREATED_ENABLED);
    }

    public AlertLevelEnum getAlertUserCreatedLevel() {
        String level = configurationDAO.getCachedPropertyValue(ALERT_USER_CREATED_LEVEL);
        return AlertLevelEnum.valueOf(level);
    }

    //-----------------------
    // user updated
    public Boolean getAlertUserUpdatedEnabled() {
        return configurationDAO.getCachedPropertyValue(ALERT_USER_UPDATED_ENABLED);
    }

    public AlertLevelEnum getAlertUserUpdatedLevel() {
        String level = configurationDAO.getCachedPropertyValue(ALERT_USER_UPDATED_LEVEL);
        return AlertLevelEnum.valueOf(level);
    }

    //-----------------------
    // user suspended
    public Boolean getAlertUserSuspendedEnabled() {
        return configurationDAO.getCachedPropertyValue(ALERT_USER_SUSPENDED_ENABLED);
    }

    public AlertLevelEnum getAlertUserSuspendedLevel() {
        String level = configurationDAO.getCachedPropertyValue(ALERT_USER_SUSPENDED_LEVEL);
        return AlertLevelEnum.valueOf(level);
    }

    public AlertSuspensionMomentEnum getAlertBeforeUserSuspendedAlertMoment() {
        String moment = configurationDAO.getCachedPropertyValue(ALERT_USER_SUSPENDED_MOMENT);
        return AlertSuspensionMomentEnum.valueOf(moment);
    }

    //-----------------------
    // before password expire
    public Boolean getAlertBeforeExpirePasswordEnabled() {
        return configurationDAO.getCachedPropertyValue(ALERT_PASSWORD_BEFORE_EXPIRATION_ENABLED);
    }

    public Integer getAlertBeforeExpirePasswordPeriod() {
        return configurationDAO.getCachedPropertyValue(ALERT_PASSWORD_BEFORE_EXPIRATION_PERIOD);
    }

    public Integer getAlertBeforeExpirePasswordInterval() {
        return configurationDAO.getCachedPropertyValue(ALERT_PASSWORD_BEFORE_EXPIRATION_INTERVAL);
    }

    public AlertLevelEnum getAlertBeforeExpirePasswordLevel() {
        String level = configurationDAO.getCachedPropertyValue(ALERT_PASSWORD_BEFORE_EXPIRATION_LEVEL);
        return AlertLevelEnum.valueOf(level);
    }

    // expired passwords
    public Boolean getAlertExpiredPasswordEnabled() {
        return configurationDAO.getCachedPropertyValue(ALERT_PASSWORD_EXPIRED_ENABLED);
    }

    public Integer getAlertExpiredPasswordPeriod() {
        return configurationDAO.getCachedPropertyValue(ALERT_PASSWORD_EXPIRED_PERIOD);
    }

    public Integer getAlertExpiredPasswordInterval() {
        return configurationDAO.getCachedPropertyValue(ALERT_PASSWORD_EXPIRED_INTERVAL);
    }

    public AlertLevelEnum getAlertExpiredPasswordLevel() {
        String level = configurationDAO.getCachedPropertyValue(ALERT_PASSWORD_EXPIRED_LEVEL);
        return AlertLevelEnum.valueOf(level);
    }

    //-----------------------
    // before access token expire
    public Boolean getAlertBeforeExpireAccessTokenEnabled() {
        return configurationDAO.getCachedPropertyValue(ALERT_ACCESS_TOKEN_BEFORE_EXPIRATION_ENABLED);
    }

    public Integer getAlertBeforeExpireAccessTokenPeriod() {
        return configurationDAO.getCachedPropertyValue(ALERT_ACCESS_TOKEN_BEFORE_EXPIRATION_PERIOD);
    }

    public Integer getAlertBeforeExpireAccessTokenInterval() {
        return configurationDAO.getCachedPropertyValue(ALERT_ACCESS_TOKEN_BEFORE_EXPIRATION_INTERVAL);
    }

    public AlertLevelEnum getAlertBeforeExpireAccessTokenLevel() {
        String level = configurationDAO.getCachedPropertyValue(ALERT_ACCESS_TOKEN_BEFORE_EXPIRATION_LEVEL);
        return AlertLevelEnum.valueOf(level);
    }

    // expired access token alerts
    public Boolean getAlertExpiredAccessTokenEnabled() {
        return configurationDAO.getCachedPropertyValue(ALERT_ACCESS_TOKEN_EXPIRED_ENABLED);
    }

    public Integer getAlertExpiredAccessTokenPeriod() {
        return configurationDAO.getCachedPropertyValue(ALERT_ACCESS_TOKEN_EXPIRED_PERIOD);
    }

    public Integer getAlertExpiredAccessTokenInterval() {
        return configurationDAO.getCachedPropertyValue(ALERT_ACCESS_TOKEN_EXPIRED_INTERVAL);
    }

    public AlertLevelEnum getAlertExpiredAccessTokenLevel() {
        String level = configurationDAO.getCachedPropertyValue(ALERT_ACCESS_TOKEN_EXPIRED_LEVEL);
        return AlertLevelEnum.valueOf(level);
    }

    //-----------------------
    // before certificate expire
    public Boolean getAlertBeforeExpireCertificateEnabled() {
        return configurationDAO.getCachedPropertyValue(ALERT_CERTIFICATE_BEFORE_EXPIRATION_ENABLED);
    }

    public Integer getAlertBeforeExpireCertificatePeriod() {
        return configurationDAO.getCachedPropertyValue(ALERT_CERTIFICATE_BEFORE_EXPIRATION_PERIOD);
    }

    public Integer getAlertBeforeExpireCertificateInterval() {
        return configurationDAO.getCachedPropertyValue(ALERT_CERTIFICATE_BEFORE_EXPIRATION_INTERVAL);
    }

    public AlertLevelEnum getAlertBeforeExpireCertificateLevel() {
        String level = configurationDAO.getCachedPropertyValue(ALERT_CERTIFICATE_BEFORE_EXPIRATION_LEVEL);
        return AlertLevelEnum.valueOf(level);
    }

    // expired access token alerts
    public Boolean getAlertExpiredCertificateEnabled() {
        return configurationDAO.getCachedPropertyValue(ALERT_CERTIFICATE_EXPIRED_ENABLED);
    }

    public Integer getAlertExpiredCertificatePeriod() {
        return configurationDAO.getCachedPropertyValue(ALERT_CERTIFICATE_EXPIRED_PERIOD);
    }

    public Integer getAlertExpiredCertificateInterval() {
        return configurationDAO.getCachedPropertyValue(ALERT_CERTIFICATE_EXPIRED_INTERVAL);
    }

    public AlertLevelEnum getAlertExpiredCertificateLevel() {
        String level = configurationDAO.getCachedPropertyValue(ALERT_CERTIFICATE_EXPIRED_LEVEL);
        return AlertLevelEnum.valueOf(level);
    }

    public String getSMPInstanceName() {
        return configurationDAO.getCachedPropertyValue(SMP_INSTANCE_NAME);
    }
    // ----
    public java.net.URL getCredentialsResetUrl() {
        return configurationDAO.getCachedPropertyValue(CREDENTIALS_RESET_URL);
    }

    public Integer getCredentialsResetPolicyValidMinutes() {
        return configurationDAO.getCachedPropertyValue(CREDENTIALS_RESET_POLICY_VALID_DAYS);
    }

    public Integer getAlertCredentialsBatchSize() {
        return configurationDAO.getCachedPropertyValue(SMP_ALERT_BATCH_SIZE);
    }

    public String getAlertEmailFrom() {
        return configurationDAO.getCachedPropertyValue(SMP_ALERT_MAIL_FROM);
    }

    public String getDefaultDomainConfiguration(SMPDomainPropertyEnum property) {
        return configurationDAO.getCachedProperty(property.getPropertyEnum());
    }

    public <T>  T getDefaultDomainConfigurationValue(SMPDomainPropertyEnum property) {
        return configurationDAO.getCachedPropertyValue(property.getPropertyEnum());
    }
}
