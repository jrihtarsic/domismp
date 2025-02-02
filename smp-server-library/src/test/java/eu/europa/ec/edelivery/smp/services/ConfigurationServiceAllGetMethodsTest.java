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

import eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum;
import eu.europa.ec.edelivery.smp.data.dao.ConfigurationDao;
import eu.europa.ec.edelivery.smp.data.ui.enums.AlertLevelEnum;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import static eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ConfigurationServiceAllGetMethodsTest {
    private static final String TEST_STRING = "TestString";
    private static final List<String> TEST_STRING_LIST = Arrays.asList("TestString1", "TestString2", "TestString3");
    private static final Map<String, String> TEST_MAP = new HashMap<>();
    private static final Pattern TEST_REXEXP = Pattern.compile(".*");
    private static final File TEST_FILE = new File("/tmp/file");
    private static URL TEST_URL;

    static {
        try {
            TEST_URL = new URL("http://test:123/path");
        } catch (Exception e) {
            fail("Fail to generated test data" + ExceptionUtils.getRootCauseMessage(e));
        }
    }

    ConfigurationDao configurationDaoMock = mock(ConfigurationDao.class);
    ConfigurationService testInstance = new ConfigurationService(configurationDaoMock);

    public static Collection<Object[]> data() {
        // set property values for property, set value, method name, value or property, value (true) or property (false)
        return Arrays.asList(new Object[][]{
                {EXTERNAL_TLS_AUTHENTICATION_CLIENT_CERT_HEADER_ENABLED, Boolean.TRUE, "isExternalTLSAuthenticationWithClientCertHeaderEnabled", true},
                {EXTERNAL_TLS_AUTHENTICATION_CERTIFICATE_HEADER_ENABLED, Boolean.TRUE, "isExternalTLSAuthenticationWithSSLClientCertHeaderEnabled", true},
                {OUTPUT_CONTEXT_PATH, Boolean.FALSE, "isUrlContextEnabled", true},
                //{HTTP_FORWARDED_HEADERS_ENABLED, Boolean.TRUE, "", true},
                {HTTP_HSTS_MAX_AGE, 1234, "getHttpHeaderHstsMaxAge", true},
                {HTTP_HEADER_SEC_POLICY, TEST_STRING, "getHttpHeaderContentSecurityPolicy", true},
                {HTTP_NO_PROXY_HOSTS, TEST_STRING, "getHttpNoProxyHosts", false},
                {HTTP_PROXY_HOST, TEST_STRING, "getHttpProxyHost", false},
                {HTTP_PROXY_PASSWORD, TEST_STRING, "getProxyCredentialToken", true},
                {HTTP_PROXY_PORT, 8800, "getHttpProxyPort", true},
                {HTTP_PROXY_USER, TEST_STRING, "getProxyUsername", true},
                {RESOURCE_SCH_VALIDATION_REGEXP, TEST_REXEXP, "getParticipantIdentifierSchemeRexExp", true},
                {RESOURCE_SCH_VALIDATION_REGEXP, TEST_STRING, "getParticipantIdentifierSchemeRexExpPattern", false},
                {RESOURCE_SCH_REGEXP_MSG, TEST_STRING, "getParticipantIdentifierSchemeRexExpMessage", true},
                {RESOURCE_SCH_MANDATORY, Boolean.FALSE, "getParticipantSchemeMandatory", true},
                {RESOURCE_CASE_SENSITIVE_SCHEMES, TEST_STRING_LIST, "getCaseSensitiveParticipantScheme", true},
                {SUBRESOURCE_CASE_SENSITIVE_SCHEMES, TEST_STRING_LIST, "getCaseSensitiveDocumentScheme", true},
                {SML_ENABLED, Boolean.FALSE, "isSMLIntegrationEnabled", true},
                {SML_URL, TEST_URL, "getSMLIntegrationUrl", true},
                {SML_TLS_DISABLE_CN_CHECK, Boolean.FALSE, "smlDisableCNCheck", true},
                {SML_TLS_SERVER_CERT_SUBJECT_REGEXP, TEST_REXEXP, "getSMLIntegrationServerCertSubjectRegExp", true},
                {SML_LOGICAL_ADDRESS, TEST_STRING, "getSMLIntegrationSMPLogicalAddress", false},
                {SML_PHYSICAL_ADDRESS, TEST_STRING, "getSMLIntegrationSMPPhysicalAddress", false},
                {KEYSTORE_PASSWORD, TEST_STRING, "getKeystoreCredentialToken", true},
                {KEYSTORE_FILENAME, TEST_FILE, "getKeystoreFile", true},
                {TRUSTSTORE_PASSWORD, TEST_STRING, "getTruststoreCredentialToken", true},
                {TRUSTSTORE_FILENAME, TEST_FILE, "getTruststoreFile", true},
                {CERTIFICATE_CRL_FORCE, Boolean.FALSE, "forceCRLValidation", true},
                //{ENCRYPTION_FILENAME, TEST_STRING, "", true},
                //{KEYSTORE_PASSWORD_DECRYPTED, TEST_STRING, "", true},
                //{TRUSTSTORE_PASSWORD_DECRYPTED, TEST_STRING, "", true},
                {CERTIFICATE_ALLOWED_CERTIFICATEPOLICY_OIDS, TEST_STRING_LIST, "getAllowedCertificatePolicies", true},
                {CERTIFICATE_SUBJECT_REGULAR_EXPRESSION, TEST_REXEXP, "getCertificateSubjectRegularExpression", true},
                //{SMP_PROPERTY_REFRESH_CRON, TEST_STRING, "", true},
                {UI_COOKIE_SESSION_SECURE, Boolean.FALSE, "getSessionCookieSecure", true},
                {UI_COOKIE_SESSION_MAX_AGE, 1111, "getSessionCookieMaxAge", true},
                {UI_COOKIE_SESSION_SITE, TEST_STRING, "getSessionCookieSameSite", true},
                {UI_COOKIE_SESSION_PATH, TEST_STRING, "getSessionCookiePath", true},
                {UI_COOKIE_SESSION_IDLE_TIMEOUT_ADMIN, 12345, "getSessionIdleTimeoutForAdmin", true},
                {UI_COOKIE_SESSION_IDLE_TIMEOUT_USER, 222, "getSessionIdleTimeoutForUser", true},
                {PASSWORD_POLICY_REGULAR_EXPRESSION, TEST_REXEXP, "getPasswordPolicyRexExp", true},
                {PASSWORD_POLICY_MESSAGE, TEST_STRING, "getPasswordPolicyValidationMessage", false},
                {PASSWORD_POLICY_VALID_DAYS, 2, "getPasswordPolicyValidDays", true},
                {PASSWORD_POLICY_REGULAR_EXPRESSION, TEST_STRING, "getPasswordPolicyRexExpPattern", false},
                {PASSWORD_POLICY_WARNING_DAYS_BEFORE_EXPIRE, 10, "getPasswordPolicyUIWarningDaysBeforeExpire", true},
                {PASSWORD_POLICY_FORCE_CHANGE_EXPIRED, Boolean.TRUE, "getPasswordPolicyForceChangeIfExpired", true},
                {USER_LOGIN_FAIL_DELAY, 1000, "getLoginFailDelayInMilliSeconds", true},
                {ACCESS_TOKEN_FAIL_DELAY, 1000, "getAccessTokenLoginFailDelayInMilliSeconds", true},
                {USER_MAX_FAILED_ATTEMPTS, 55, "getLoginMaxAttempts", true},
                {USER_SUSPENSION_TIME, 3600, "getLoginSuspensionTimeInSeconds", true},
                {ACCESS_TOKEN_POLICY_VALID_DAYS, 1212, "getAccessTokenPolicyValidDays", true},
                {ACCESS_TOKEN_MAX_FAILED_ATTEMPTS, 2323, "getAccessTokenLoginMaxAttempts", true},
                {ACCESS_TOKEN_SUSPENSION_TIME, 22, "getAccessTokenLoginSuspensionTimeInSeconds", true},
                {UI_AUTHENTICATION_TYPES, TEST_STRING_LIST, "getUIAuthenticationTypes", true},
                {AUTOMATION_AUTHENTICATION_TYPES, TEST_STRING_LIST, "getAutomationAuthenticationTypes", true},
                {SSO_CAS_UI_LABEL, TEST_STRING, "getCasUILabel", true},
                {SSO_CAS_URL, TEST_URL, "getCasURL", true},
                {SSO_CAS_URL_PATH_LOGIN, TEST_STRING, "getCasURLPathLogin", true},
                {SSO_CAS_CALLBACK_URL, TEST_URL, "getCasCallbackUrl", true},
                {SSO_CAS_TOKEN_VALIDATION_URL_PATH, TEST_STRING, "getCasURLTokenValidation", true},
                {SSO_CAS_TOKEN_VALIDATION_PARAMS, TEST_MAP, "getCasTokenValidationParams", true},
                {SSO_CAS_TOKEN_VALIDATION_GROUPS, TEST_STRING_LIST, "getCasURLTokenValidationGroups", true},
                {SMP_CLUSTER_ENABLED, Boolean.FALSE, "isClusterEnabled", true},
                {ENCODED_SLASHES_ALLOWED_IN_URL, Boolean.FALSE, "encodedSlashesAllowedInUrl", true},
                {SMP_ALERT_CREDENTIALS_SERVER, TEST_STRING, "getTargetServerForCredentialValidation", true},
                {SML_TLS_SERVER_CERT_SUBJECT_REGEXP, TEST_STRING, "getSMLIntegrationServerCertSubjectRegExpPattern", false},
                {SML_TLS_TRUSTSTORE_USE_SYSTEM_DEFAULT, Boolean.FALSE, "useSystemTruststoreForTLS", true},
                {SSO_CAS_SMP_LOGIN_URI, TEST_STRING, "getCasSMPLoginRelativePath", true},
                {ALERT_USER_LOGIN_FAILURE_ENABLED, Boolean.FALSE, "getAlertUserLoginFailureEnabled", true},
                {ALERT_USER_SUSPENDED_ENABLED, Boolean.FALSE, "getAlertUserSuspendedEnabled", true},
                {ALERT_PASSWORD_BEFORE_EXPIRATION_ENABLED, Boolean.FALSE, "getAlertBeforeExpirePasswordEnabled", true},
                {ALERT_PASSWORD_BEFORE_EXPIRATION_PERIOD, 10, "getAlertBeforeExpirePasswordPeriod", true},
                {ALERT_PASSWORD_BEFORE_EXPIRATION_INTERVAL, 10, "getAlertBeforeExpirePasswordInterval", true},
                {ALERT_PASSWORD_EXPIRED_ENABLED, Boolean.FALSE, "getAlertExpiredPasswordEnabled", true},
                {ALERT_PASSWORD_EXPIRED_PERIOD, 10, "getAlertExpiredPasswordPeriod", true},
                {ALERT_PASSWORD_EXPIRED_INTERVAL, 10, "getAlertExpiredPasswordInterval", true},
                {ALERT_ACCESS_TOKEN_BEFORE_EXPIRATION_ENABLED, Boolean.FALSE, "getAlertBeforeExpireAccessTokenEnabled", true},
                {ALERT_ACCESS_TOKEN_BEFORE_EXPIRATION_PERIOD, 10, "getAlertBeforeExpireAccessTokenPeriod", true},
                {ALERT_ACCESS_TOKEN_BEFORE_EXPIRATION_INTERVAL, 10, "getAlertBeforeExpireAccessTokenInterval", true},
                {ALERT_ACCESS_TOKEN_EXPIRED_ENABLED, Boolean.FALSE, "getAlertExpiredAccessTokenEnabled", true},
                {ALERT_ACCESS_TOKEN_EXPIRED_PERIOD, 10, "getAlertExpiredAccessTokenPeriod", true},
                {ALERT_ACCESS_TOKEN_EXPIRED_INTERVAL, 10, "getAlertExpiredAccessTokenInterval", true},

                {ALERT_CERTIFICATE_BEFORE_EXPIRATION_ENABLED, Boolean.FALSE, "getAlertBeforeExpireCertificateEnabled", true},
                {ALERT_CERTIFICATE_BEFORE_EXPIRATION_PERIOD, 10, "getAlertBeforeExpireCertificatePeriod", true},
                {ALERT_CERTIFICATE_BEFORE_EXPIRATION_INTERVAL, 10, "getAlertBeforeExpireCertificateInterval", true},

                {ALERT_CERTIFICATE_EXPIRED_ENABLED, Boolean.FALSE, "getAlertExpiredCertificateEnabled", true},
                {ALERT_CERTIFICATE_EXPIRED_PERIOD, 10, "getAlertExpiredCertificatePeriod", true},
                {ALERT_CERTIFICATE_EXPIRED_INTERVAL, 10, "getAlertExpiredCertificateInterval", true},
                {SMP_ALERT_BATCH_SIZE, 10, "getAlertCredentialsBatchSize", true},
                {SMP_ALERT_MAIL_FROM, TEST_STRING, "getAlertEmailFrom", true},

                {ALERT_USER_SUSPENDED_LEVEL, AlertLevelEnum.HIGH, "getAlertUserSuspendedLevel", true},
                {ALERT_USER_LOGIN_FAILURE_LEVEL, AlertLevelEnum.HIGH, "getAlertUserLoginFailureLevel", true},
                {ALERT_PASSWORD_BEFORE_EXPIRATION_LEVEL, AlertLevelEnum.HIGH, "getAlertBeforeExpirePasswordLevel", true},
                {ALERT_ACCESS_TOKEN_BEFORE_EXPIRATION_LEVEL, AlertLevelEnum.HIGH, "getAlertBeforeExpireAccessTokenLevel", true},
                {ALERT_ACCESS_TOKEN_EXPIRED_LEVEL, AlertLevelEnum.HIGH, "getAlertExpiredAccessTokenLevel", true},
                {ALERT_CERTIFICATE_BEFORE_EXPIRATION_LEVEL, AlertLevelEnum.HIGH, "getAlertBeforeExpireCertificateLevel", true},
                {ALERT_CERTIFICATE_EXPIRED_LEVEL, AlertLevelEnum.HIGH, "getAlertExpiredCertificateLevel", true},
        });
    }

    @ParameterizedTest
    @MethodSource("data")
    void testProperty(SMPPropertyEnum property, Object value, String methodName, boolean fromValue) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        if (fromValue) {
            doReturn(value instanceof AlertLevelEnum ? value.toString() : value).when(configurationDaoMock).getCachedPropertyValue(property);
        } else {
            doReturn(value).when(configurationDaoMock).getCachedProperty(property);
        }
        Object result = MethodUtils.invokeExactMethod(testInstance, methodName);
        if (result instanceof Optional) {
            assertEquals(value, ((Optional<?>) result).get());
        } else {
            assertEquals(value, result);
        }
    }
}
