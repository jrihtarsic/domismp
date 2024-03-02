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
package eu.europa.ec.smp.spi.handler;

import eu.europa.ec.smp.spi.api.model.ResourceIdentifier;
import eu.europa.ec.smp.spi.exceptions.ResourceException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;


class OasisSMPResource10HandlerTest extends AbstractHandlerTest {
    @Override
    public AbstractOasisSMPHandler getTestInstance() {
        return new OasisSMPResource10Handler(mockSmpDataApi, mockSmpIdentifierServiceApi);
    }

    @Test
    void testGenerateResource() throws ResourceException {

        ResourceIdentifier resourceIdentifier = new ResourceIdentifier("test-identifier", "test-test-test");

        generateResourceAction(resourceIdentifier);
    }

    @Test
    void validateResourceOK() throws ResourceException {
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier("urn:eu:ncpb:utest", "ehealth-actorid-qns");
        // validate
        validateResourceAction("/examples/oasis-smp-1.0/ResourceOK.xml", resourceIdentifier);
    }

    @Test
    void validateResourceDisallowedDocType() {
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier("urn:eu:ncpb:utest", "ehealth-actorid-qns");
        // validate
        ResourceException result = assertThrows(ResourceException.class,
                () -> validateResourceAction("/examples/oasis-smp-1.0/ResourceWithDOCTYPE.xml", resourceIdentifier));
        MatcherAssert.assertThat(result.getMessage(), org.hamcrest.Matchers.containsString("DOCTYPE is disallowed"));
    }

    @Test
    void validateResourceInvalidIdentifier() {
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier("urn:poland:ncpb:InvalidIdentifier", "ehealth-actorid-qns");
        // validate
        ResourceException result = assertThrows(ResourceException.class,
                () -> validateResourceAction("/examples/oasis-smp-1.0/ResourceOK.xml", resourceIdentifier));
        MatcherAssert.assertThat(result.getMessage(), org.hamcrest.Matchers.containsString("Participant identifiers don't match"));
    }

    @Test
    void validateResourceInvalidScheme() {
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier("urn:poland:ncpb:utestt", "ehealth-actorid-qns");
        // validate
        ResourceException result = assertThrows(ResourceException.class,
                () -> validateResourceAction("/examples/oasis-smp-1.0/ResourceInvalidScheme.xml", resourceIdentifier));
        MatcherAssert.assertThat(result.getMessage(), org.hamcrest.Matchers.containsString("SAXParseException"));
    }

    @Test
    void readResourceOK() throws ResourceException {
        String resourceName = "/examples/oasis-smp-1.0/ResourceOK.xml";
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier("urn:eu:ncpb:utest", "ehealth-actorid-qns");

        readResourceAction(resourceName, resourceIdentifier);
    }


    @Test
    void storeResourceOK() throws ResourceException {
        String resourceName = "/examples/oasis-smp-1.0/ResourceOK.xml";
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier("urn:eu:ncpb:utest", "ehealth-actorid-qns");

        storeResourceAction(resourceName, resourceIdentifier);
    }


}
