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

package eu.europa.ec.edelivery.smp.sml;

import ec.services.wsdl.bdmsl.data._1.ExistsParticipantResponseType;
import ec.services.wsdl.bdmsl.data._1.ParticipantsType;
import ec.services.wsdl.bdmsl.data._1.SMPAdvancedServiceForParticipantType;
import eu.europa.ec.bdmsl.ws.soap.*;
import eu.europa.ec.dynamicdiscovery.exception.MalformedIdentifierException;
import eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum;
import eu.europa.ec.edelivery.smp.services.IdentifierService;
import eu.europa.ec.edelivery.smp.data.model.DBDomain;
import eu.europa.ec.edelivery.smp.data.ui.CertificateRO;
import eu.europa.ec.edelivery.smp.exceptions.ErrorCode;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import eu.europa.ec.edelivery.smp.identifiers.Identifier;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.services.ConfigurationService;
import eu.europa.ec.edelivery.smp.services.ui.UIKeystoreService;
import eu.europa.ec.edelivery.smp.services.ui.UITruststoreService;
import eu.europa.ec.edelivery.smp.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.CertificateConstraintsType;
import org.apache.cxf.configuration.security.CombinatorType;
import org.apache.cxf.configuration.security.DNConstraintsType;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.ProxyServerType;
import org.busdox.servicemetadata.locator._1.PublisherEndpointType;
import org.busdox.servicemetadata.locator._1.ServiceMetadataPublisherServiceForParticipantType;
import org.busdox.servicemetadata.locator._1.ServiceMetadataPublisherServiceType;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static eu.europa.ec.edelivery.smp.conversion.SmlIdentifierConverter.*;
import static eu.europa.ec.edelivery.smp.exceptions.SMLErrorMessages.*;

/**
 * Component responsible for building SOAP request and calling BDMSL.
 * It knows if SML integration is turned ON and only then makes a CREATE or DELETE participant call.
 * <p>
 * Created by gutowpa on 22/12/2017.
 */
@Component
public class SmlConnector implements ApplicationContextAware {

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(SmlConnector.class);

    private static final String SERVICE_METADATA_CONTEXT = "manageservicemetadata";
    private static final String IDENTIFIER_VALUE_CONTEXT = "manageparticipantidentifier";
    private static final String BDMSL_CUSTOM_SERVICES_CONTEXT = "bdmslservice";
    private static final String CLIENT_CERT_HEADER_KEY = "Client-Cert";


    private final ConfigurationService configurationService;
    private final UIKeystoreService keystoreService;
    private final UITruststoreService truststoreService;
    private final IdentifierService identifierService;
    private ApplicationContext ctx;

    public SmlConnector(ConfigurationService configurationService, UIKeystoreService keystoreService, UITruststoreService truststoreService, IdentifierService identifierService) {
        this.configurationService = configurationService;
        this.keystoreService = keystoreService;
        this.truststoreService = truststoreService;
        this.identifierService = identifierService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }

    /**
     * Register a new participant in the SML. If the integration with SML is disabled
     * or the Domain is not registered, it returns {@code false}.
     * @param scheme the participant identifier scheme
     * @param identifier  the participant identifier value
     * @param domain the domain entity to which the participant must be registered
     * @param customNaptrService the custom NAPTR service to be used with registration the naptr record
     * @return {@code true} if the participant is registered; otherwise, {@code false}
     * @throws SMPRuntimeException if an error occurs during the registration process
     */
    public boolean registerInDns(String scheme, String identifier, DBDomain domain, String customNaptrService) {
        if (!configurationService.isSMLIntegrationEnabled()) {
            return false;
        }

        Identifier normalizedParticipantId = identifierService.normalizeParticipant(domain.getDomainCode(), scheme, identifier);
        String normalizedParticipantString = identifierService.formatParticipant(domain.getDomainCode(), normalizedParticipantId);
        if (!domain.isSmlRegistered()) {
            LOG.info("Participant {} is not registered to SML because domain {} is not registered!",
                    normalizedParticipantString, domain.getDomainCode());
            return false;
        }

        LOG.debug("Registering new Participant: {} to domain: {}.", normalizedParticipantString, domain.getDomainCode());
        try {
            if (StringUtils.isBlank(customNaptrService)) {
                createRegularDNSRecord(normalizedParticipantId, domain);
                LOG.info("Set regular DNS record for Participant: [{}] and domain: [{}].", normalizedParticipantId, domain.getDomainCode());
            } else {
                createCustomServiceNaptrDNSRecord(normalizedParticipantId, domain, customNaptrService);
                LOG.info("Set custom naptr service [{}] DNS record for Participant: [{}] and domain: [{}].", customNaptrService, normalizedParticipantId, domain.getDomainCode());
            }
            return true;
        } catch (BadRequestFault | NotFoundFault e) {
            return processSMLErrorMessage(e, normalizedParticipantId);
        } catch (InternalErrorFault | UnauthorizedFault e) {
            LOG.error(e.getClass().getName() + e.getMessage(), e);
            throw new SMPRuntimeException(ErrorCode.SML_INTEGRATION_EXCEPTION, e, ExceptionUtils.getRootCauseMessage(e));
        }
    }

    /**
     * Checks whether the participant identified by the provided ID exists or not. In case the integration with SML is
     * disabled, it returns {@code false}.
     *
     * @param scheme the participant scheme
     * @param identifier the participant scheme
     * @param domain                  the domain entity
     * @return {@code true} if the participant exists; otherwise, {@code false} (also when SML integration is disabled).
     */
    public boolean participantExists(String scheme, String identifier, DBDomain domain) {
        if (!configurationService.isSMLIntegrationEnabled()) {
            return false;
        }
        Identifier normalizedParticipantId;
        String normalizedParticipantString;
        try {
            normalizedParticipantId = identifierService.normalizeParticipant(domain.getDomainCode(), scheme, identifier);
            normalizedParticipantString = identifierService.formatParticipant(domain.getDomainCode(), normalizedParticipantId);
        } catch (MalformedIdentifierException e) {
            LOG.error("Invalid participant identifier: [{}].", e.getMessage());
            throw new SMPRuntimeException(ErrorCode.SML_INTEGRATION_EXCEPTION, e, ExceptionUtils.getRootCauseMessage(e));

        }
        if (!domain.isSmlRegistered()) {
            LOG.info("Cannot check if Participant {} exists when domain {} is not registered!",
                    normalizedParticipantString, domain.getDomainCode());
            return false;
        }

        LOG.debug("Checking if Participant: {} exists in domain: {}.", normalizedParticipantString, domain.getDomainCode());
        try {
            ParticipantsType smlRequest = toParticipantsType(normalizedParticipantId, domain.getSmlSmpId());
            ExistsParticipantResponseType existsParticipantResponseType = getBDMSLWSClient(domain).existsParticipantIdentifier(smlRequest);
            return existsParticipantResponseType.isExist();
        } catch (BadRequestFault | NotFoundFault e) {
            return processSMLErrorMessage(e, normalizedParticipantId);
        } catch (Exception e) {
            LOG.error(e.getClass().getName() + e.getMessage(), e);
            throw new SMPRuntimeException(ErrorCode.SML_INTEGRATION_EXCEPTION, e, ExceptionUtils.getRootCauseMessage(e));
        }
    }

    protected void createRegularDNSRecord(Identifier normalizedParticipantId, DBDomain domain) throws UnauthorizedFault, BadRequestFault, NotFoundFault, InternalErrorFault {
        LOG.debug("Set regular DNS record for Participant: [{}] and domain: [{}].", normalizedParticipantId, domain.getDomainCode());
        ServiceMetadataPublisherServiceForParticipantType smlRequest = toBusdoxParticipantId(normalizedParticipantId, domain.getSmlSmpId());
        getParticipantWSClient(domain).create(smlRequest);
    }

    protected void createCustomServiceNaptrDNSRecord(Identifier normalizedParticipantId, DBDomain domain, String customNaptrService) throws UnauthorizedFault, BadRequestFault, NotFoundFault, InternalErrorFault {
        LOG.debug("Set custom naptr service [{}] DNS record for Participant: [{}] and domain: [{}].", customNaptrService, normalizedParticipantId, domain.getDomainCode());
        SMPAdvancedServiceForParticipantType smlRequest = toBDMSLAdvancedParticipantId(normalizedParticipantId, domain.getSmlSmpId(), customNaptrService);
        getBDMSLWSClient(domain).createParticipantIdentifier(smlRequest);
    }

    protected boolean processSMLErrorMessage(Exception e, Identifier participantIdentifierType) {
        if (!isOkMessage(participantIdentifierType, e.getMessage())) {
            LOG.error(e.getMessage(), e);
            throw new SMPRuntimeException(ErrorCode.SML_INTEGRATION_EXCEPTION, e, ExceptionUtils.getRootCauseMessage(e));
        }
        LOG.warn(e.getMessage(), e);
        return true;
    }

    /**
     * Ignore messages if already exists
     *
     * @param patId
     * @param errorMessage
     * @return
     */
    protected boolean isOkMessage(Identifier patId, String errorMessage) {
        if (errorMessage == null) {
            return false;
        }
        String exp = String.format(ERR_PARTICIPANT_ALREADY_EXISTS, patId.getValue(), patId.getScheme());
        String exp2 = ERR_PARTICIPANT_NOT_EXISTS;
        return errorMessage.startsWith(exp) || errorMessage.startsWith(exp2);
    }


    /**
     * @param domain
     * @return
     */
    public boolean registerDomain(DBDomain domain) {
        if (!configurationService.isSMLIntegrationEnabled()) {
            return false;
        }
        String smlSmpId = domain.getSmlSmpId();
        LOG.info("Registering new Domain to SML: (smpCode {} smp-smp-id {}) ", domain.getDomainCode(), smlSmpId);
        try {
            ServiceMetadataPublisherServiceType smlSmpRequest = getServiceMetadataPublisherServiceType(smlSmpId);
            getSMPManagerWSClient(domain).create(smlSmpRequest);
        } catch (BadRequestFault e) {
            processSMLErrorMessage(e, domain);
        } catch (Exception e) {
            LOG.error(e.getClass().getName() + e.getMessage(), e);
            throw new SMPRuntimeException(ErrorCode.SML_INTEGRATION_EXCEPTION, e, ExceptionUtils.getRootCauseMessage(e));
        }
        // if not error is thrown - the registration is done OK.
        return true;
    }

    /**
     * Checks whether a domain is valid or not. In case the integration with SML is disabled, it returns {@code false}.
     *
     * @param domain the domain entity
     * @return {@code true} if the domain exists and is valid; otherwise, {@code false} (also when SML integration is disabled).
     */
    public boolean isDomainValid(DBDomain domain) {
        if (!configurationService.isSMLIntegrationEnabled()) {
            return false;
        }
        String smlSmpId = domain.getSmlSmpId();
        LOG.info("Validating Domain to SML: (smpCode {} smp-smp-id {}) ", domain.getDomainCode(), smlSmpId);
        try {
            ServiceMetadataPublisherServiceType smlSmpRequest = getServiceMetadataPublisherServiceType(smlSmpId);
            getSMPManagerWSClient(domain).read(smlSmpRequest);
        } catch (BadRequestFault | NotFoundFault e) {
            processSMLErrorMessage(e, domain);
        } catch (Exception e) {
            LOG.error(e.getClass().getName() + e.getMessage(), e);
            throw new SMPRuntimeException(ErrorCode.SML_INTEGRATION_EXCEPTION, e, ExceptionUtils.getRootCauseMessage(e));
        }
        // if not error is thrown - the domain exists and is valid
        return true;
    }

    private ServiceMetadataPublisherServiceType getServiceMetadataPublisherServiceType(String smlSmpId) {
        String smpLogicalAddress = configurationService.getSMLIntegrationSMPLogicalAddress();
        String smpPhysicalAddress = configurationService.getSMLIntegrationSMPPhysicalAddress();

        ServiceMetadataPublisherServiceType smlSmpRequest = new ServiceMetadataPublisherServiceType();
        smlSmpRequest.setPublisherEndpoint(new PublisherEndpointType());
        smlSmpRequest.getPublisherEndpoint().setLogicalAddress(smpLogicalAddress);
        smlSmpRequest.getPublisherEndpoint().setPhysicalAddress(smpPhysicalAddress);
        smlSmpRequest.setServiceMetadataPublisherID(smlSmpId);

        return smlSmpRequest;
    }

    private void processSMLErrorMessage(Exception e, DBDomain domain) {
        if (!isOkMessage(domain, e.getMessage())) {
            LOG.error(e.getMessage(), e);
            throw new SMPRuntimeException(ErrorCode.SML_INTEGRATION_EXCEPTION, e, ExceptionUtils.getRootCauseMessage(e));
        }
        LOG.warn(e.getMessage(), e);
    }

    /**
     * Ignore messages if already exists
     *
     * @param domain
     * @param errorMessage
     * @return
     */
    protected boolean isOkMessage(DBDomain domain, String errorMessage) {
        LOG.info("Validate SML error message for domain {} {}", errorMessage, domain.getDomainCode());
        if (errorMessage == null) {
            return false;
        }
        String exp = String.format(ERR_DOMAIN_ALREADY_EXISTS, domain.getSmlSmpId());
        String exp2 = String.format(ERR_DOMAIN_NOT_EXISTS, domain.getSmlSmpId());
        return errorMessage.startsWith(exp) || errorMessage.startsWith(exp2);
    }


    /**
     * Unregister a participant from the SML. If the integration with SML is disabled or the Domain is not registered, it returns {@code false}.
     * @param scheme the participant identifier scheme
     * @param identifier the participant identifier value
     * @param domain the domain entity from which the participant must be unregistered
     * @return {@code true} if the participant is unregistered; otherwise, {@code false}
     */
    public boolean unregisterFromDns(String scheme, String identifier, DBDomain domain) {
        if (!configurationService.isSMLIntegrationEnabled()) {
            return false;
        }
        Identifier normalizedParticipantId = identifierService.normalizeParticipant(domain.getDomainCode(), scheme, identifier);
        String normalizedParticipantString = identifierService.formatParticipant(domain.getDomainCode(), normalizedParticipantId);
        if (!domain.isSmlRegistered()) {
            LOG.info("Participant {} is not unregistered from SML because domain {} is not registered!",
                    normalizedParticipantString, domain.getDomainCode());
            return false;
        }

        LOG.debug("Removing Participant: {} from domain: {}.", normalizedParticipantString, domain.getDomainCode());
        try {
            ServiceMetadataPublisherServiceForParticipantType smlRequest = toBusdoxParticipantId(normalizedParticipantId, domain.getSmlSmpId());
            getParticipantWSClient(domain).delete(smlRequest);
            LOG.info("Participant: {} removed domain: {}.", normalizedParticipantString, domain.getDomainCode());
            return true;
        } catch (BadRequestFault | NotFoundFault e) {
            return processSMLErrorMessage(e, normalizedParticipantId);
        } catch (Exception e) {
            LOG.error(e.getClass().getName() + e.getMessage(), e);
            throw new SMPRuntimeException(ErrorCode.SML_INTEGRATION_EXCEPTION, e, ExceptionUtils.getRootCauseMessage(e));
        }
    }

    public void unregisterDomain(DBDomain domain) {
        if (!configurationService.isSMLIntegrationEnabled()) {
            return;
        }
        LOG.info("Removing SMP id (Domain) from BDMSL: {} ", domain.getDomainCode());
        try {
            getSMPManagerWSClient(domain).delete(domain.getSmlSmpId());
        } catch (BadRequestFault | NotFoundFault e) {
            processSMLErrorMessage(e, domain);
        } catch (Exception e) {
            LOG.error(e.getClass().getName() + e.getMessage(), e);
            throw new SMPRuntimeException(ErrorCode.SML_INTEGRATION_EXCEPTION, e, ExceptionUtils.getRootCauseMessage(e));
        }
    }

    private IManageParticipantIdentifierWS getParticipantWSClient(DBDomain domain) {

        IManageParticipantIdentifierWS iManageServiceMetadataWS = ctx.getBean(IManageParticipantIdentifierWS.class);
        // configure connection
        configureClient(IDENTIFIER_VALUE_CONTEXT, iManageServiceMetadataWS, domain);

        return iManageServiceMetadataWS;
    }

    private IBDMSLServiceWS getBDMSLWSClient(DBDomain domain) {

        IBDMSLServiceWS bdmslServiceWS = ctx.getBean(IBDMSLServiceWS.class);
        // configure connection
        configureClient(BDMSL_CUSTOM_SERVICES_CONTEXT, bdmslServiceWS, domain);

        return bdmslServiceWS;
    }

    private IManageServiceMetadataWS getSMPManagerWSClient(DBDomain domain) {
        IManageServiceMetadataWS iManageServiceMetadataWS = ctx.getBean(IManageServiceMetadataWS.class);
        // configure value connection
        configureClient(SERVICE_METADATA_CONTEXT, iManageServiceMetadataWS, domain);

        return iManageServiceMetadataWS;
    }

    protected String getSmlClientKeyAliasForDomain(DBDomain domain) {
        String alias = domain.getSmlClientKeyAlias();
        if (!domain.isSmlClientCertAuth() && StringUtils.isBlank(alias)) {
            List<CertificateRO> list = keystoreService.getKeystoreEntriesList();
            // if there is only one certificate than choose the one
            if (list.size() == 1) {
                alias = list.get(0).getAlias();
            }
        }
        return alias;
    }

    public void configureClient(String serviceEndpoint, Object smlPort, DBDomain domain) {
        String clientKeyAlias = getSmlClientKeyAliasForDomain(domain);
        boolean clientCertAuthentication = domain.isSmlClientCertAuth();
        Client client = ClientProxy.getClient(smlPort);
        URL url = configurationService.getSMLIntegrationUrl();
        if (url == null) {
            throw new IllegalArgumentException("Empty or null SML url. Check the configuration and set property: " + SMPPropertyEnum.SML_URL.getProperty());
        }
        URL urlSMPManagment;
        try {
            urlSMPManagment = new URL(StringUtils.appendIfMissing(url.toString(), "/") + serviceEndpoint);

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed SML URL: " + url, e);
        }

        boolean useTLS = urlSMPManagment.getProtocol().equalsIgnoreCase("https");
        Map<String, Object> requestContext = ((BindingProvider) smlPort).getRequestContext();
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, urlSMPManagment.toString());

        CertificateRO certificateRO = getClientCertificate(clientKeyAlias);

        if (!clientCertAuthentication && !useTLS) {
            LOG.warn("SML integration is wrongly configured. Uses 2-way-SSL HTTPS but URL is not HTTPS! Url: [{}].", urlSMPManagment);
        }

        HTTPConduit httpConduit = (HTTPConduit) client.getConduit();

        configureClientAuthentication(httpConduit, requestContext,
                certificateRO,
                clientCertAuthentication, useTLS);
        configureFaultHandling(requestContext);
        configureProxy(httpConduit, urlSMPManagment);
        configurePayloadLogging(client);
    }

    public CertificateRO getClientCertificate(String alias) {
        List<CertificateRO> list = keystoreService.getKeystoreEntriesList();
        if (StringUtils.isBlank(alias)) {
            // legacy behaviour - if only one certificate then there is no need to define it
            if (list.size() == 1) {
                return list.get(0);
            }
            throw new IllegalStateException("Invalid integration configuration. Missing Client cert configuration!");
        }

        return list.stream().filter(cert -> StringUtils.equalsIgnoreCase(alias, cert.getAlias()))
                .findFirst().orElseThrow(() -> new IllegalStateException("Invalid integration configuration. Missing Client cert configuration!"));

    }

    public void configureClientAuthentication(HTTPConduit httpConduit, Map<String, Object> requestContext, CertificateRO certificateRO, boolean clientCertAuthentication, boolean useTLS) {
        LOG.info("Connect to SML (smlClientAuthentication: [{}] use Client-CertHeader: [{}])", certificateRO, clientCertAuthentication);

        if (certificateRO == null) {
            throw new IllegalStateException("SML integration is wrongly configured, at least one authentication option is required: 2-way-SSL or Client-Cert header");
        }

        // set truststore...
        TLSClientParameters tlsParams = new TLSClientParameters();
        tlsParams.setUseHttpsURLConnectionDefaultSslSocketFactory(false);
        tlsParams.setUseHttpsURLConnectionDefaultHostnameVerifier(false);
        tlsParams.setCertConstraints(createCertConstraint(configurationService.getSMLIntegrationServerCertSubjectRegExpPattern()));
        tlsParams.setDisableCNCheck(configurationService.smlDisableCNCheck());
        if (!configurationService.useSystemTruststoreForTLS()) {
            /**
             * Sets the TrustManagers associated with this endpoint.
             * This parameter may be set to null for system default behavior.
             */
            LOG.debug("Set SMP truststore managers for the TLS certificate verification.");
            tlsParams.setTrustManagers(truststoreService.getTrustManagers());
        }

        if (!clientCertAuthentication) {
            LOG.debug("SML X509 certificate authentication with alias  [{}].", certificateRO.getAlias());
            tlsParams.setCertAlias(certificateRO.getAlias());
            tlsParams.setKeyManagers(keystoreService.getKeyManagers());
        } else {
            LOG.debug("User Client cert header to authenticate to SML {}.", certificateRO.getClientCertHeader());
            Map<String, List<String>> customHeaders = new HashMap<>();
            customHeaders.put(CLIENT_CERT_HEADER_KEY, Collections.singletonList(certificateRO.getClientCertHeader()));
            requestContext.put(MessageContext.HTTP_REQUEST_HEADERS, customHeaders);
        }
        if (useTLS) {
            LOG.debug("Set SMP TLS client parameters.");
            httpConduit.setTlsClientParameters(tlsParams);
        }
    }


    public CertificateConstraintsType createCertConstraint(String regExp) {
        if (StringUtils.isBlank(regExp)) {
            return null;
        }

        CertificateConstraintsType ct = new CertificateConstraintsType();
        DNConstraintsType dnConstraintsType = new DNConstraintsType();
        dnConstraintsType.setCombinator(CombinatorType.fromValue("ALL"));
        dnConstraintsType.getRegularExpression().add(regExp);
        ct.setSubjectDNConstraints(dnConstraintsType);

        return ct;
    }

    private void configureFaultHandling(Map<String, Object> requestContext) {
        // CXF by default stops processing response in a few cases, ie.: when server returned HTTP 400 (SOAP Fault)
        // We want to finish processing such messages - otherwise we would not be able to log fault's reason.
        requestContext.put(HTTPConduit.NO_IO_EXCEPTIONS, true);
        requestContext.put(HTTPConduit.PROCESS_FAULT_ON_HTTP_400, true);
    }

    private void configurePayloadLogging(Client client) {
        client.getBus().setFeatures(Collections.singletonList(new LoggingFeature()));
    }

    private void configureProxy(HTTPConduit httpConduit, URL targetUrl) {

        if (!configurationService.isProxyEnabled()) {
            return;
        }

        String noProxyHosts = configurationService.getHttpNoProxyHosts();
        if (HttpUtils.doesTargetMatchNonProxy(targetUrl.getHost(), configurationService.getHttpNoProxyHosts())) {
            LOG.info("Target host {} is match noProxy hosts {}!", targetUrl.getHost(), noProxyHosts);
            return;
        }
        String proxyServer = configurationService.getHttpProxyHost();
        Optional<Integer> proxyPort = configurationService.getHttpProxyPort();
        String proxyUser = configurationService.getProxyUsername();
        String proxyPassword = configurationService.getProxyCredentialToken();


        LOG.info("Configuring proxy for BDMSL integration client: {}:{}@{}:{}", proxyUser, "******", proxyServer, proxyPort.isPresent() ? proxyPort.get() : "");
        httpConduit.getClient().setProxyServerType(ProxyServerType.HTTP);
        httpConduit.getClient().setProxyServer(proxyServer);
        proxyPort.ifPresent(port -> httpConduit.getClient().setProxyServerPort(port));

        if (!StringUtils.isBlank(proxyUser)) {
            ProxyAuthorizationPolicy proxyAuth = new ProxyAuthorizationPolicy();
            proxyAuth.setAuthorizationType("Basic");
            LOG.debug("Set proxy authentication {}", proxyUser);
            proxyAuth.setUserName(proxyUser);
            proxyAuth.setPassword(proxyPassword);
            httpConduit.setProxyAuthorization(proxyAuth);
        }

    }

}
