/*-
 * #START_LICENSE#
 * oasis-smp-spi
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
package eu.europa.ec.smp.spi.handler;

import eu.europa.ec.dynamicdiscovery.core.extension.impl.oasis10.OasisSMP10ServiceGroupReader;
import eu.europa.ec.dynamicdiscovery.core.validator.OasisSmpSchemaValidator;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.exception.XmlInvalidAgainstSchemaException;
import eu.europa.ec.smp.spi.api.SmpDataServiceApi;
import eu.europa.ec.smp.spi.api.SmpIdentifierServiceApi;
import eu.europa.ec.smp.spi.api.model.RequestData;
import eu.europa.ec.smp.spi.api.model.ResourceIdentifier;
import eu.europa.ec.smp.spi.api.model.ResponseData;
import eu.europa.ec.smp.spi.def.OasisSMPSubresource10;
import eu.europa.ec.smp.spi.enums.TransientDocumentPropertyType;
import eu.europa.ec.smp.spi.exceptions.ResourceException;
import gen.eu.europa.ec.ddc.api.smp10.ParticipantIdentifierType;
import gen.eu.europa.ec.ddc.api.smp10.ServiceGroup;
import gen.eu.europa.ec.ddc.api.smp10.ServiceMetadataReferenceCollectionType;
import gen.eu.europa.ec.ddc.api.smp10.ServiceMetadataReferenceType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.europa.ec.smp.spi.enums.TransientDocumentPropertyType.RESOURCE_IDENTIFIER_SCHEME;
import static eu.europa.ec.smp.spi.enums.TransientDocumentPropertyType.RESOURCE_IDENTIFIER_VALUE;
import static eu.europa.ec.smp.spi.exceptions.ResourceException.ErrorCode.*;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trim;

@Component
public class OasisSMPResource10Handler extends AbstractOasisSMPHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OasisSMPResource10Handler.class);

    OasisSMP10ServiceGroupReader reader = new OasisSMP10ServiceGroupReader();

    final SmpDataServiceApi smpDataApi;
    final SmpIdentifierServiceApi smpIdentifierApi;

    final OasisSMP10ServiceGroupReader serviceGroupReader;

    public OasisSMPResource10Handler(SmpDataServiceApi smpDataApi,
                                     SmpIdentifierServiceApi smpIdentifierApi) {
        this.smpDataApi = smpDataApi;
        this.smpIdentifierApi = smpIdentifierApi;
        serviceGroupReader = new OasisSMP10ServiceGroupReader();
    }

    public void generateResource(RequestData resourceData, ResponseData responseData, List<String> fields) throws ResourceException {
        ResourceIdentifier identifier = getResourceIdentifier(resourceData);

        ServiceGroup resource = new ServiceGroup();
        resource.setParticipantIdentifier(new ParticipantIdentifierType());
        resource.getParticipantIdentifier().setValue(RESOURCE_IDENTIFIER_VALUE.getPropertyPlaceholder());
        if (identifier.getScheme()!= null) {
            resource.getParticipantIdentifier().setScheme(RESOURCE_IDENTIFIER_SCHEME.getPropertyPlaceholder());
        }
        resource.setServiceMetadataReferenceCollection(new ServiceMetadataReferenceCollectionType());

        try {
            reader.serializeNative(resource, responseData.getOutputStream(), true);
        } catch (TechnicalException e) {
            throw new ResourceException(PARSE_ERROR, "Can not marshal extension for service group: [" + identifier + "]. Error: " + ExceptionUtils.getRootCauseMessage(e), e);
        }
    }


    @Override
    public void readResource(RequestData resourceData, ResponseData responseData) throws ResourceException {

        ResourceIdentifier identifier = getResourceIdentifier(resourceData);
        if (resourceData.getResourceInputStream() == null) {
            LOG.warn("Empty document input stream for service-group [{}]!", identifier);
            return;
        }

        ServiceGroup resource;
        try {
            resource = reader.parseNative(resourceData.getResourceInputStream());
        } catch (TechnicalException e) {
            throw new ResourceException(PARSE_ERROR, "Can not parse service group xml for identifier: [" + identifier + "]. Error: " + ExceptionUtils.getRootCauseMessage(e), e);
        }
        // get references
        resource.setServiceMetadataReferenceCollection(new ServiceMetadataReferenceCollectionType());
        List<ServiceMetadataReferenceType> referenceTypes = buildReferences(resourceData.getDomainCode(), identifier);
        resource.getServiceMetadataReferenceCollection().getServiceMetadataReferences().addAll(referenceTypes);

        try {
            reader.serializeNative(resource, responseData.getOutputStream(), false);
        } catch (TechnicalException e) {
            throw new ResourceException(PARSE_ERROR, "Can not marshal extension for service group: [" + identifier + "]. Error: " + ExceptionUtils.getRootCauseMessage(e), e);
        }
    }


    private List<ServiceMetadataReferenceType> buildReferences(final String domainCode, ResourceIdentifier resourceIdentifier) throws ResourceException {
        LOG.debug("Build build References identifier [{}].", resourceIdentifier);
        // get subresource identifiers for document type
        List<ResourceIdentifier> subResourceIdentifier = smpDataApi.getSubResourceIdentifiers(resourceIdentifier, OasisSMPSubresource10.RESOURCE_IDENTIFIER);

        List<ServiceMetadataReferenceType> referenceIds = new ArrayList<>();
        for (ResourceIdentifier subresId : subResourceIdentifier) {
            URI url = buildSMPURLForParticipantAndDocumentIdentifier(domainCode, resourceIdentifier, subresId);
            ServiceMetadataReferenceType referenceType = new ServiceMetadataReferenceType();
            referenceType.setHref(url.toString());
            referenceIds.add(referenceType);
        }
        return referenceIds;
    }

    public URI buildSMPURLForParticipantAndDocumentIdentifier(final String domainCode, ResourceIdentifier resourceIdentifier, ResourceIdentifier subresourceIdentifier) throws ResourceException {
        LOG.debug("Build SMP url for participant identifier: [{}] and document identifier [{}].", resourceIdentifier, subresourceIdentifier);

        String pathSegment = smpDataApi.getURIPathSegmentForSubresource(OasisSMPSubresource10.RESOURCE_IDENTIFIER);
        String baseUrl = smpDataApi.getResourceUrl();
        String formattedParticipant = smpIdentifierApi.formatResourceIdentifier(domainCode, resourceIdentifier);
        String formattedDocument = smpIdentifierApi.formatSubresourceIdentifier(domainCode, subresourceIdentifier);

        LOG.debug("Build SMP url from base path [{}], participant identifier: [{}] and document identifier [{}].",
                baseUrl, formattedParticipant, formattedDocument);
        try {
            return new URIBuilder(baseUrl)
                    .appendPathSegments(formattedParticipant)
                    .appendPathSegments(pathSegment)
                    .appendPathSegments(formattedDocument).build();
        } catch (URISyntaxException e) {
            throw new ResourceException(INTERNAL_ERROR, "Can not build SMP document URL path! " + ExceptionUtils.getMessage(e), e);
        }
    }


    @Override
    public void storeResource(RequestData resourceData, ResponseData responseData) throws ResourceException {
        LOG.info("Store resource for identifier [{}].", resourceData.getResourceIdentifier());
        InputStream inputStream = resourceData.getResourceInputStream();
        // reading resource multiple time make sure it can be rest
        if (!inputStream.markSupported()) {
            inputStream = new BufferedInputStream(inputStream);
        }

        inputStream.mark(Integer.MAX_VALUE - 2);
        ServiceGroup resource = validateAndParse(resourceData);

        // ServiceMetadataReferenceCollection must be empty because they are automatically generated
        if (resource.getServiceMetadataReferenceCollection() != null
                && !resource.getServiceMetadataReferenceCollection().getServiceMetadataReferences().isEmpty()) {
            throw new ResourceException(INVALID_PARAMETERS, "ServiceMetadataReferenceCollection must be empty!");
        }
        // back-compatibility issue: set participant to "lowercase" to match it as is saved in the database
        ParticipantIdentifierType orgResourceId = resource.getParticipantIdentifier();
        ResourceIdentifier nrmResourceId = resourceData.getResourceIdentifier();
        boolean isSame = StringUtils.equals(orgResourceId.getValue(), nrmResourceId.getValue())
                && StringUtils.equals(orgResourceId.getScheme(), nrmResourceId.getScheme());

        if (isSame) {
            try {
                inputStream.reset();
                StreamUtils.copy(inputStream, responseData.getOutputStream());
            } catch (IOException e) {
                throw new ResourceException(PARSE_ERROR, "Error occurred while copying the ServiceGroup", e);
            }
        } else {
            LOG.info("Update ServiceGroup identifier before saving. Old: [{}], New: [{}]", orgResourceId, nrmResourceId);
            if (!StringUtils.equalsIgnoreCase(orgResourceId.getValue(), RESOURCE_IDENTIFIER_VALUE.getPropertyPlaceholder())) {
                orgResourceId.setValue(nrmResourceId.getValue());
            }
            if (!StringUtils.equalsIgnoreCase(orgResourceId.getScheme(), RESOURCE_IDENTIFIER_SCHEME.getPropertyPlaceholder())) {
                orgResourceId.setScheme(nrmResourceId.getScheme());
            }
            try {
                // need to save resource because of the update on the resource identifier values
                reader.serializeNative(resource, responseData.getOutputStream(), true);
            } catch (TechnicalException e) {
                throw new ResourceException(PARSE_ERROR, "Error occurred while copying the ServiceGroup", e);
            }
        }
    }

    /**
     * Method validates service group
     *
     * @param resourceData the resource data
     */
    @Override
    public void validateResource(RequestData resourceData) throws ResourceException {
        validateAndParse(resourceData);
    }

    public ServiceGroup validateAndParse(RequestData resourceData) throws ResourceException {
        // get service group identifier
        ResourceIdentifier identifier = getResourceIdentifier(resourceData);
        // validate by schema
        byte[] bytearray;
        try {
            bytearray = readFromInputStream(resourceData.getResourceInputStream());
            OasisSmpSchemaValidator.validateOasisSMP10Schema(bytearray);
        } catch (IOException | XmlInvalidAgainstSchemaException e) {
            String ids = identifier != null ?
                    Stream.of(identifier).map(Object::toString).collect(Collectors.joining(",")) : "";
            throw new ResourceException(INVALID_RESOURCE, "Error occurred while validation Oasis SMP 1.0 ServiceGroup extension: [" + ids + "] with error: " + ExceptionUtils.getRootCauseMessage(e), e);
        }
        // if service group
        ServiceGroup resource;
        try {
            resource = reader.parseNative(new ByteArrayInputStream(bytearray));
        } catch (TechnicalException e) {
            throw new ResourceException(INVALID_RESOURCE, "Error occurred while parsing Oasis SMP 1.0 ServiceGroup with error: " + ExceptionUtils.getRootCauseMessage(e), e);
        }
        final ParticipantIdentifierType participantId = resource.getParticipantIdentifier();
        String participantIdValue = participantId.getValue();
        String participantIdScheme = participantId.getScheme();
        if (equalsIgnoreCase(trim(participantIdValue), RESOURCE_IDENTIFIER_VALUE.getPropertyPlaceholder())) {
            participantIdValue = identifier.getValue();
        }

        if (equalsIgnoreCase(trim(participantIdScheme), RESOURCE_IDENTIFIER_SCHEME.getPropertyPlaceholder())) {
            participantIdScheme = identifier.getScheme();
        }

        ResourceIdentifier xmlResourceIdentifier = smpIdentifierApi.normalizeResourceIdentifier(resourceData.getDomainCode(),
                participantIdValue, participantIdScheme);



        if (!xmlResourceIdentifier.equals(identifier)) {
            // Business identifier must equal path
            throw new ResourceException(INVALID_PARAMETERS,
                    "Participant identifiers don't match between URL parameter [" + identifier + "] and XML body: ['" + xmlResourceIdentifier + "']");
        }


        return resource;
    }
}
