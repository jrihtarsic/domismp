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
package eu.europa.ec.edelivery.smp.services.ui;

import eu.europa.ec.edelivery.smp.data.ui.DNSQueryRO;
import eu.europa.ec.edelivery.smp.data.ui.DNSQueryRequestRO;
import eu.europa.ec.edelivery.smp.services.spi.SmpIdentifierService;
import eu.europa.ec.edelivery.smp.testutil.TestConstants;
import eu.europa.ec.smp.spi.api.model.ResourceIdentifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Class provide unit tests dynamic discovery tools for UI.
 *
 * @author Joze Rihtarsic
 * @since 5.1
 */
class UIDynamicDiscoveryToolsTest {

    SmpIdentifierService mockIdentifierService = Mockito.mock(SmpIdentifierService.class);
    UIDynamicDiscoveryTools testInstance = new UIDynamicDiscoveryTools(mockIdentifierService);


    /**
     * Test method tries to resolve DNS queries for given
     * participant identifier iso6523-actorid-upis::0007:001:oasis:eusend
     * on domain acc.edelivery.tech.ec.europa.eu.
     * For test to pass the data must be registered in the DNS server and build
     * server must have access to internet!
     * <p>
     * If the test fails, please check the eDelivery DNS server still contains
     * CNAME records
     * B-3c66f725f5d01a2de8c413d100da4bc9.iso6523-actorid-upis.test.acc.edelivery.tech.ec.europa.eu
     * NAPTR records
     * B3THMADDQQOBPZPE7CVM5PGD5UE4XHTPPGZZQRM4OH74P2HOSIBA.iso6523-actorid-upis.test.acc.edelivery.tech.ec.europa.eu
     * domain and resource identifier
     */
    @Test
    void testGetDNSQuerySuccess() {
        DNSQueryRequestRO request = createDNSQueryRequestRO("test.acc.edelivery.tech.ec.europa.eu");

        Mockito.doReturn(new ResourceIdentifier(request.getIdentifierValue(), request.getIdentifierScheme()))
                .when(mockIdentifierService).normalizeResourceIdentifier(Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyString());

        List<DNSQueryRO> result = testInstance.createDnsQueries(request);

        assertEquals(2, result.size());
        assertEquals("B-3c66f725f5d01a2de8c413d100da4bc9.iso6523-actorid-upis.test.acc.edelivery.tech.ec.europa.eu", result.get(0).getDnsQuery());
        assertNotEquals(0, result.get(0).getDnsEntries().size());
        assertEquals("B3THMADDQQOBPZPE7CVM5PGD5UE4XHTPPGZZQRM4OH74P2HOSIBA.iso6523-actorid-upis.test.acc.edelivery.tech.ec.europa.eu", result.get(1).getDnsQuery());
        assertNotEquals(0, result.get(1).getDnsEntries().size());
    }

    @Test
    void testGetDNSQueryDoesNotExists() {

        DNSQueryRequestRO request = createDNSQueryRequestRO("domainNoExists.local");

        Mockito.doReturn(new ResourceIdentifier(request.getIdentifierValue(), request.getIdentifierScheme()))
                .when(mockIdentifierService).normalizeResourceIdentifier(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        List<DNSQueryRO> result = testInstance.createDnsQueries(request);

        assertEquals(2, result.size());
        assertEquals("B-3c66f725f5d01a2de8c413d100da4bc9.iso6523-actorid-upis.domainNoExists.local", result.get(0).getDnsQuery());
        assertEquals(0, result.get(0).getDnsEntries().size());
        assertEquals("B3THMADDQQOBPZPE7CVM5PGD5UE4XHTPPGZZQRM4OH74P2HOSIBA.iso6523-actorid-upis.domainNoExists.local", result.get(1).getDnsQuery());
        assertEquals(0, result.get(1).getDnsEntries().size());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testGetDNSQueryWithNullEmptyDomain(String domain) {
        DNSQueryRequestRO request = createDNSQueryRequestRO(domain);

        Mockito.doReturn(new ResourceIdentifier(request.getIdentifierValue(), request.getIdentifierScheme()))
                .when(mockIdentifierService).normalizeResourceIdentifier(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        List<DNSQueryRO> result = testInstance.createDnsQueries(request);

        assertEquals(2, result.size());
        assertEquals("B-3c66f725f5d01a2de8c413d100da4bc9.iso6523-actorid-upis.", result.get(0).getDnsQuery());
        assertEquals(0, result.get(0).getDnsEntries().size());
        assertEquals("B3THMADDQQOBPZPE7CVM5PGD5UE4XHTPPGZZQRM4OH74P2HOSIBA.iso6523-actorid-upis.", result.get(1).getDnsQuery());
        assertEquals(0, result.get(1).getDnsEntries().size());
    }

    private DNSQueryRequestRO createDNSQueryRequestRO(String topDnsDomain) {
        DNSQueryRequestRO dnsQueryRequestRO = new DNSQueryRequestRO();
        dnsQueryRequestRO.setDomainCode(TestConstants.TEST_DOMAIN_CODE_1);

        dnsQueryRequestRO.setIdentifierScheme("iso6523-actorid-upis");
        dnsQueryRequestRO.setIdentifierValue("0007:001:oasis:eusend");
        dnsQueryRequestRO.setTopDnsDomain(topDnsDomain);
        return dnsQueryRequestRO;
    }
}
