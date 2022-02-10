package eu.europa.ec.edelivery.smp.data.ui.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public enum SMPPropertyEnum {
    BLUE_COAT_ENABLED ("authentication.blueCoat.enabled","false","Authentication with Blue Coat means that all HTTP requests " +
            "having 'Client-Cert' header will be authenticated as username placed in the header.Never expose SMP to the WEB " +
            "without properly configured reverse-proxy and active blue coat.", false, false,false, SMPPropertyTypeEnum.BOOLEAN),

    OUTPUT_CONTEXT_PATH ("contextPath.output","true","This property controls pattern of URLs produced by SMP in GET ServiceGroup responses." , true, false,true, SMPPropertyTypeEnum.BOOLEAN),
    HTTP_FORWARDED_HEADERS_ENABLED ("smp.http.forwarded.headers.enabled","false","Use (value true) or remove (value false) forwarded headers! There are security considerations for forwarded headers since an application cannot know if the headers were added by a proxy, as intended, or by a malicious client." , false, false,false, SMPPropertyTypeEnum.BOOLEAN),

    HTTP_PROXY_HOST("smp.proxy.host", "", "The http proxy host", false,false,false, SMPPropertyTypeEnum.STRING),
    HTTP_NO_PROXY_HOSTS("smp.noproxy.hosts", "localhost|127.0.0.1", "list of nor proxy hosts. Ex.: localhost|127.0.0.1", false,false,false, SMPPropertyTypeEnum.STRING),
    HTTP_PROXY_PASSWORD("smp.proxy.password", "", "Base64 encrypted password for Proxy.", false, true,false, SMPPropertyTypeEnum.STRING),
    HTTP_PROXY_PORT("smp.proxy.port", "80", "The http proxy port", false, false,false, SMPPropertyTypeEnum.INTEGER),
    HTTP_PROXY_USER("smp.proxy.user", "", "The proxy user", false, false,false, SMPPropertyTypeEnum.STRING),

    PARTC_SCH_REGEXP ("identifiersBehaviour.ParticipantIdentifierScheme.validationRegex","^((?!^.{26})([a-z0-9]+-[a-z0-9]+-[a-z0-9]+)|urn:oasis:names:tc:ebcore:partyid-type:(iso6523|unregistered)(:.+)?$)","Participant Identifier Schema of each PUT ServiceGroup request is validated against this schema.", false, false,false, SMPPropertyTypeEnum.REGEXP),
    PARTC_SCH_REGEXP_MSG ("identifiersBehaviour.ParticipantIdentifierScheme.validationRegexMessage",
            "Participant scheme must start with:urn:oasis:names:tc:ebcore:partyid-type:(iso6523:|unregistered:) OR must be up to 25 characters long with form [domain]-[identifierArea]-[identifierType] (ex.: 'busdox-actorid-upis') and may only contain the following characters: [a-z0-9].", "Error message for UI",false, false,false, SMPPropertyTypeEnum.STRING),

    CS_PARTICIPANTS("identifiersBehaviour.caseSensitive.ParticipantIdentifierSchemes","sensitive-participant-sc1|sensitive-participant-sc2","Specifies schemes of participant identifiers that must be considered CASE-SENSITIVE.", false, false,false, SMPPropertyTypeEnum.LIST_STRING),
    CS_DOCUMENTS("identifiersBehaviour.caseSensitive.DocumentIdentifierSchemes","casesensitive-doc-scheme1|casesensitive-doc-scheme2","Specifies schemes of document identifiers that must be considered CASE-SENSITIVE.", false, false,false, SMPPropertyTypeEnum.LIST_STRING),

    SML_ENABLED("bdmsl.integration.enabled","false","BDMSL (SML) integration ON/OFF switch", false, false,false, SMPPropertyTypeEnum.BOOLEAN),
    SML_PARTICIPANT_MULTIDOMAIN("bdmsl.participant.multidomain.enabled","false","Set to true if SML support participant on multidomain", false, false,true, SMPPropertyTypeEnum.BOOLEAN),
    SML_URL("bdmsl.integration.url","http://localhost:8080/edelivery-sml","BDMSL (SML) endpoint", false, false,false, SMPPropertyTypeEnum.URL),
    SML_TLS_DISABLE_CN_CHECK("bdmsl.integration.tls.disableCNCheck","false","If SML Url is HTTPs - Disable CN check if needed.", false, false,false, SMPPropertyTypeEnum.BOOLEAN),
    SML_TLS_SERVER_CERT_SUBJECT_REGEXP("bdmsl.integration.tls.serverSubjectRegex",".*","Regular expression for server TLS certificate subject verification  CertEx. .*CN=acc.edelivery.tech.ec.europa.eu.*.", false, false,false, SMPPropertyTypeEnum.REGEXP),

    SML_LOGICAL_ADDRESS("bdmsl.integration.logical.address","http://localhost:8080/smp/","Logical SMP endpoint which will be registered on SML when registering new domain", false, false,false, SMPPropertyTypeEnum.URL),
    SML_PHYSICAL_ADDRESS("bdmsl.integration.physical.address","0.0.0.0","Physical SMP endpoint which will be registered on SML when registering new domain.", false, false,false, SMPPropertyTypeEnum.STRING),

    KEYSTORE_PASSWORD("smp.keystore.password","","Encrypted keystore (and keys) password ", false, true,false, SMPPropertyTypeEnum.STRING),
    KEYSTORE_FILENAME("smp.keystore.filename","smp-keystore.jks","Keystore filename ", true, false,false, SMPPropertyTypeEnum.FILENAME),
    TRUSTSTORE_PASSWORD("smp.truststore.password","","Encrypted truststore password ", false, true,false, SMPPropertyTypeEnum.STRING),
    TRUSTSTORE_FILENAME("smp.truststore.filename","","Truststore filename ", false, false,false, SMPPropertyTypeEnum.FILENAME),
    CERTIFICATE_CRL_FORCE("smp.certificate.crl.force","false","If false then if CRL is not reachable ignore CRL validation", false, false,false, SMPPropertyTypeEnum.BOOLEAN),

    CONFIGURATION_DIR("configuration.dir","smp","Path to the folder containing all the configuration files (keystore and encryption key)", true, false,true, SMPPropertyTypeEnum.PATH),
    ENCRYPTION_FILENAME("encryption.key.filename","encryptionPrivateKey.private","Key filename to encrypt passwords", false, false,true, SMPPropertyTypeEnum.FILENAME),
    KEYSTORE_PASSWORD_DECRYPTED("smp.keystore.password.decrypted","","Only for backup purposes when  password is automatically created. Store password somewhere save and delete this entry!", false, false,false, SMPPropertyTypeEnum.STRING),
    TRUSTSTORE_PASSWORD_DECRYPTED("smp.truststore.password.decrypted","","Only for backup purposes when  password is automatically created. Store password somewhere save and delete this entry!", false, false,false, SMPPropertyTypeEnum.STRING),

    SML_KEYSTORE_PASSWORD("bdmsl.integration.keystore.password","","Deprecated", false, false,false, SMPPropertyTypeEnum.STRING),
    SML_KEYSTORE_PATH("bdmsl.integration.keystore.path","","Deprecated", false, false,false, SMPPropertyTypeEnum.STRING),
    SIGNATURE_KEYSTORE_PASSWORD("xmldsig.keystore.password","","Deprecated", false, false,false, SMPPropertyTypeEnum.STRING),
    SIGNATURE_KEYSTORE_PATH("xmldsig.keystore.classpath","","Deprecated", false, false,false, SMPPropertyTypeEnum.STRING),
    SML_PROXY_HOST("bdmsl.integration.proxy.server","","Deprecated", false, false,false, SMPPropertyTypeEnum.STRING),
    SML_PROXY_PORT("bdmsl.integration.proxy.port","","Deprecated", false, false,false, SMPPropertyTypeEnum.INTEGER),
    SML_PROXY_USER("bdmsl.integration.proxy.user","","Deprecated", false, false,false, SMPPropertyTypeEnum.STRING),
    SML_PROXY_PASSWORD("bdmsl.integration.proxy.password","","Deprecated", false, false,false, SMPPropertyTypeEnum.STRING),

    SMP_PROPERTY_REFRESH_CRON("smp.property.refresh.cronJobExpression","0 48 */1 * * *","Property refresh cron expression (def 12 minutes to each hour). Property change is refreshed at restart!", false, false,true, SMPPropertyTypeEnum.STRING),
    // UI COOKIE configuration
    UI_COOKIE_SESSION_SECURE("smp.ui.session.secure","false","Cookie is only sent to the server when a request is made with the https: scheme (except on localhost), and therefore is more resistant to man-in-the-middle attacks.", false, false,false, SMPPropertyTypeEnum.BOOLEAN),
    UI_COOKIE_SESSION_MAX_AGE("smp.ui.session.max-age","","Number of seconds until the cookie expires. A zero or negative number will expire the cookie immediately. Empty value will not set parameter", false, false,false, SMPPropertyTypeEnum.INTEGER),
    UI_COOKIE_SESSION_SITE("smp.ui.session.strict","Lax","Controls whether a cookie is sent with cross-origin requests, providing some protection against cross-site request forgery attacks. Possible values are: Strict, None, Lax. (Cookies with SameSite=None require a secure context/HTTPS)!!)", false, false,false, SMPPropertyTypeEnum.STRING),
    UI_COOKIE_SESSION_PATH("smp.ui.session.path","","A path that must exist in the requested URL, or the browser won't send the Cookie header.  Null/Empty value sets the authentication requests context by default.  The forward slash (/) character is interpreted as a directory separator, and subdirectories will be matched as well: for Path=/docs, /docs, /docs/Web/, and /docs/Web/HTTP will all match", false, false,false, SMPPropertyTypeEnum.STRING),

    UI_COOKIE_SESSION_IDLE_TIMEOUT_ADMIN("smp.ui.session.idle_timeout.admin","300","Specifies the time, in seconds, between client requests before the SMP will invalidate session for ADMIN users (System)!", false, false,false, SMPPropertyTypeEnum.INTEGER),
    UI_COOKIE_SESSION_IDLE_TIMEOUT_USER("smp.ui.session.idle_timeout.user","1800","Specifies the time, in seconds, between client requests before the SMP will invalidate session for users (Service group, SMP Admin)", false, false,false, SMPPropertyTypeEnum.INTEGER),
    // SSO configuration
    SSO_CAS_ENABLED("smp.sso.cas.enabled","false","Enable/disable CAS authentication.", false, false,true, SMPPropertyTypeEnum.BOOLEAN),
    SSO_CAS_UI_LABEL("smp.sso.cas.ui.label","EU Login","The SSO service provider label.", false, false,true, SMPPropertyTypeEnum.STRING),
    SSO_CAS_URL("smp.sso.cas.url","http://localhost:8080/cas/","The SSO CAS URL enpoint", false, false,true, SMPPropertyTypeEnum.URL),
    SSO_CAS_URLPATH_LOGIN("smp.sso.cas.urlpath.login","login","The CAS URL path for login. Complete URL is composed from parameters: ${smp.sso.cas.url}/${smp.sso.cas.urlpath.login}.", false, false,true, SMPPropertyTypeEnum.STRING),
    SSO_CAS_CALLBACK_URL("smp.sso.cas.callback.url","http://localhost:8080/smp/ui/rest/security/cas","The URL is the callback URL belonging to the local SMP Security System. If using RP make sure it target SMP path '/ui/rest/security/cas'", false, false,true, SMPPropertyTypeEnum.URL),
    SSO_CAS_TOKEN_VALIDATION_URLPATH("smp.sso.cas.token.validation.urlpath","http://localhost:8080/cas/","The CAS URL path for login. Complete URL is composed from parameters: ${smp.sso.cas.url}/${smp.sso.cas.urlpath.token.validation}.", false, false,true, SMPPropertyTypeEnum.STRING),
    SSO_CAS_TOKEN_VALIDATION_PARAMS("smp.sso.cas.token.validation.params","acceptStrengths:BASIC,CLIENT_CERT|assuranceLevel:TOP","The CAS token validation key:value properties separated with '|'.Ex: 'acceptStrengths:BASIC,CLIENT_CERT|assuranceLevel:TOP'", false, false,true, SMPPropertyTypeEnum.MAP_STRING),
    SSO_CAS_TOKEN_VALIDATION_GROUPS("smp.sso.cas.token.validation.groups","DIGIT_SMP|DIGIT_ADMIN","'|' separated CAS groups user must belong to.", false, false,true, SMPPropertyTypeEnum.LIST_STRING),
    ;


    String property;
    String defValue;
    String desc;

    boolean isEncrypted;
    boolean isMandatory;
    boolean restartNeeded;
    SMPPropertyTypeEnum propertyType;

    SMPPropertyEnum(String property, String defValue, String desc, boolean isMandatory, boolean isEncrypted, boolean restartNeeded, SMPPropertyTypeEnum propertyType) {
        this.property = property;
        this.defValue = defValue;
        this.desc = desc;
        this.isEncrypted=isEncrypted;
        this.isMandatory=isMandatory;
        this.restartNeeded=restartNeeded;
        this.propertyType=propertyType;
    }

    public String getProperty() {
        return property;
    }

    public String getDefValue() {
        return defValue;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public SMPPropertyTypeEnum getPropertyType() {
        return propertyType;
    }

    public static Optional<SMPPropertyEnum> getByProperty(String key) {
        String keyTrim = StringUtils.trimToNull(key);
        if (keyTrim == null) {
            return Optional.empty();
        }
        return Arrays.asList(values()).stream().filter(val -> val.getProperty().equalsIgnoreCase(keyTrim)).findAny();
    }
}


