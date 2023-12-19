/*-
 * #START_LICENSE#
 * oasis-smp-spi
 * %%
 * Copyright (C) 2017 - 2023 European Commission | eDelivery | DomiSMP
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
package eu.europa.ec.smp.spi.def;

import eu.europa.ec.smp.spi.handler.OasisSMPServiceGroup20Handler;
import eu.europa.ec.smp.spi.resource.ResourceDefinitionSpi;
import eu.europa.ec.smp.spi.resource.ResourceHandlerSpi;
import eu.europa.ec.smp.spi.resource.SubresourceDefinitionSpi;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;


/**
 * The SubresourceDefinitionSpi implementation for the Oasis SMP 2.0 ServiceGroup document.
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Component
public class OasisSMPServiceGroup20 implements ResourceDefinitionSpi {


    OasisSMPServiceGroup20Handler serviceGroup20Handler;
    OasisSMPServiceMetadata20 oasisSMPServiceMetadata20;

    public OasisSMPServiceGroup20(OasisSMPServiceGroup20Handler serviceGroup20Handler, OasisSMPServiceMetadata20 oasisSMPServiceMetadata20) {
        this.serviceGroup20Handler = serviceGroup20Handler;
        this.oasisSMPServiceMetadata20 = oasisSMPServiceMetadata20;
    }

    @Override
    public String identifier() {
        return "edelivery-oasis-smp-2.0-servicegroup";
    }

    @Override
    public String defaultUrlSegment() {
        return "oasis-bdxr-smp-2";
    }

    @Override
    public String name() {
        return "Oasis SMP 2.0 ServiceGroup";
    }

    @Override
    public String description() {
        return "Oasis SMP 2.0 Service group resource handler";
    }

    @Override
    public String mimeType() {
        return "text/xml";
    }

    @Override
    public List<SubresourceDefinitionSpi> getSubresourceSpiList() {
        return Collections.singletonList(oasisSMPServiceMetadata20);
    }

    @Override
    public ResourceHandlerSpi getResourceHandler() {
        return serviceGroup20Handler;
    }

    @Override
    public String toString() {
        return "OasisSMPServiceGroup20{" +
                "identifier=" + identifier() +
                "defaultUrlSegment=" + defaultUrlSegment() +
                "name=" + name() +
                "mimeType=" + mimeType() +
                '}';
    }
}
