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

package eu.europa.ec.smp.spi.validation;


import eu.europa.ec.smp.spi.api.SmpIdentifierServiceApi;
import eu.europa.ec.smp.spi.api.model.ResourceIdentifier;
import eu.europa.ec.smp.spi.exceptions.ResourceException;
import gen.eu.europa.ec.ddc.api.smp20.ServiceMetadata;
import gen.eu.europa.ec.ddc.api.smp20.aggregate.Endpoint;
import gen.eu.europa.ec.ddc.api.smp20.aggregate.ProcessMetadata;
import gen.eu.europa.ec.ddc.api.smp20.basic.ParticipantID;
import gen.eu.europa.ec.ddc.api.smp20.basic.ServiceID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static eu.europa.ec.smp.spi.enums.TransientDocumentPropertyType.*;
import static eu.europa.ec.smp.spi.enums.TransientDocumentPropertyType.SUBRESOURCE_IDENTIFIER_SCHEME;
import static eu.europa.ec.smp.spi.exceptions.ResourceException.ErrorCode.INVALID_PARAMETERS;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trim;


/**
 * Simple Subresource validator
 */
@Component
public class Subresource20Validator {

    private static final Logger LOG = LoggerFactory.getLogger(Subresource20Validator.class);

    final SmpIdentifierServiceApi smpIdentifierApi;

    public Subresource20Validator(SmpIdentifierServiceApi smpIdentifierApi) {
        this.smpIdentifierApi = smpIdentifierApi;
    }

    public void validate(final String domainCode,
            ResourceIdentifier participantIdentifierFromUrl,
                         ResourceIdentifier documentIdentifierFromUrl,
                         ServiceMetadata subresource
    ) throws ResourceException {
        LOG.debug("Validate service metadata for participant [{}], document [{}]", participantIdentifierFromUrl, documentIdentifierFromUrl);

        final ParticipantID participantId = subresource.getParticipantID();
        final ServiceID documentId = subresource.getServiceID();

        String participantIdValue = participantId.getValue();
        String participantIdScheme = participantId.getSchemeID();
        String documentIdValue = documentId.getValue();
        String documentIdScheme = documentId.getSchemeID();

        if (equalsIgnoreCase(trim(participantIdValue), RESOURCE_IDENTIFIER_VALUE.getPropertyPlaceholder())) {
            participantIdValue = participantIdentifierFromUrl.getValue();
        }

        if (equalsIgnoreCase(trim(participantIdScheme), RESOURCE_IDENTIFIER_SCHEME.getPropertyPlaceholder())) {
            participantIdScheme = participantIdentifierFromUrl.getScheme();
        }

        if (equalsIgnoreCase(trim(documentIdValue), SUBRESOURCE_IDENTIFIER_VALUE.getPropertyPlaceholder())) {
            documentIdValue = documentIdentifierFromUrl.getValue();
        }

        if (equalsIgnoreCase(trim(documentIdScheme), SUBRESOURCE_IDENTIFIER_SCHEME.getPropertyPlaceholder())) {
            documentIdScheme = documentIdentifierFromUrl.getScheme();
        }

        ResourceIdentifier xmlResourceIdentifier = smpIdentifierApi.normalizeResourceIdentifier(
                domainCode,
                participantIdValue,
                participantIdScheme);
        ResourceIdentifier xmlSubresourceIdentifier = smpIdentifierApi.normalizeSubresourceIdentifier(
                domainCode,
                documentIdValue,
                documentIdScheme);


        if (!xmlResourceIdentifier.equals(participantIdentifierFromUrl)) {
            // Business identifier must equal path
            throw new ResourceException(INVALID_PARAMETERS, "Participant identifiers don't match between URL parameter [" + participantIdentifierFromUrl + "] and XML body: [" + xmlResourceIdentifier + "]");
        }

        if (!xmlSubresourceIdentifier.equals(documentIdentifierFromUrl)) {
            // Business identifier must equal path
            throw new ResourceException(INVALID_PARAMETERS, "Document identifiers don't match between URL parameter [" + documentIdentifierFromUrl + "] and XML body: [" + xmlSubresourceIdentifier + "]");
        }

        List<ProcessMetadata> processMetadata = subresource.getProcessMetadatas();
        validateProcesses(processMetadata);
    }


    private void validateProcesses(List<ProcessMetadata> processMetadata) throws ResourceException {
        LOG.debug("Validate service metadata processes!");

        if (processMetadata.isEmpty()) {
            LOG.debug("No processes found!");
            return;
        }

        for (ProcessMetadata process : processMetadata) {
            validateProcess(process);
        }
    }

    private void validateProcess(ProcessMetadata process) throws ResourceException {
        LOG.debug("Validate process found!");
        List<Endpoint> serviceEndpoints = process.getEndpoints();
        if (serviceEndpoints == null) {
            LOG.warn("No endpoint for the process!");
            return;
        }

        Set<String> transportProfiles = new HashSet<>();
        for (Endpoint endpoint : serviceEndpoints) {
            if (endpoint.getTransportProfileID() == null || StringUtils.isBlank(endpoint.getTransportProfileID().getValue())) {
                throw new ResourceException(INVALID_PARAMETERS, "Empty Transport Profile!");
            }
            String profileId = endpoint.getTransportProfileID().getValue();

            if (!transportProfiles.add(profileId)) {
                throw new ResourceException(INVALID_PARAMETERS, "Duplicated Transport Profile: " + profileId);
            }

            OffsetDateTime activationDate = endpoint.getActivationDate() != null ? endpoint.getActivationDate().getValue() : null;
            OffsetDateTime expirationDate = endpoint.getExpirationDate() != null ? endpoint.getExpirationDate().getValue() : null;
            LOG.debug("Validate validity for the process with activation date [{}] and expiration date [{}]!", activationDate, expirationDate);

            if (activationDate != null && expirationDate != null && activationDate.isAfter(expirationDate)) {
                throw new ResourceException(INVALID_PARAMETERS, "[OUT_OF_RANGE] Expiration date is before Activation date");
            }

            if (expirationDate != null && expirationDate.isBefore(OffsetDateTime.now())) {
                throw new ResourceException(INVALID_PARAMETERS, "[OUT_OF_RANGE] Expiration date has passed");
            }
        }
    }
}
