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

import eu.europa.ec.bdmsl.ws.soap.*;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.services.ConfigurationService;
import eu.europa.ec.edelivery.smp.services.ui.UIKeystoreService;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Factory creating CXF client that access BDMSL via SOAP interface.
 * Produced client has already configured all transport and authentication parameters like URL, keystore, proxy etc...
 * <p>
 * Created by gutowpa on 14/12/2017.
 */
@Component
public class SmlClientFactory {

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(SmlClientFactory.class);

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    UIKeystoreService keystoreService;

    @Bean
    @Scope("prototype")
    public IManageParticipantIdentifierWS create() {
        LOG.info("create IManageParticipantIdentifierWS");
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.getClientFactoryBean().getServiceFactory()
                .setWsdlURL(ManageBusinessIdentifierService.class.getResource("/ManageBusinessIdentifierService-1.0.wsdl"));
        factory.setServiceName(ManageBusinessIdentifierService.SERVICE);
        factory.setEndpointName(ManageBusinessIdentifierService.ManageBusinessIdentifierServicePort);
        return factory.create(IManageParticipantIdentifierWS.class);
    }

    @Bean
    @Scope("prototype")
    public IManageServiceMetadataWS createSmp() {
        LOG.info("create IManageServiceMetadataWS");

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.getClientFactoryBean().getServiceFactory()
                .setWsdlURL(ManageServiceMetadataService.class.getResource("/ManageServiceMetadataService-1.0.wsdl"));
        factory.setServiceName(ManageServiceMetadataService.SERVICE);
        factory.setEndpointName(ManageServiceMetadataService.ManageServiceMetadataServicePort);
        return factory.create(IManageServiceMetadataWS.class);
    }

    @Bean
    @Scope("prototype")
    public IBDMSLServiceWS createBDMSLCustomServices() {
        LOG.info("create IBDMSLServiceWS");

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.getClientFactoryBean().getServiceFactory()
                .setWsdlURL(BDMSLService.class.getResource("/BDMSLService-1.0.wsdl"));
        factory.setServiceName(BDMSLService.SERVICE);
        factory.setEndpointName(BDMSLService.BDMSLServicePort);
        return factory.create(IBDMSLServiceWS.class);
    }
}
