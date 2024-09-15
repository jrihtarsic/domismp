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
package eu.europa.ec.edelivery.smp.services.resource;


import eu.europa.ec.edelivery.smp.data.dao.DocumentDao;
import eu.europa.ec.edelivery.smp.data.dao.ResourceDao;
import eu.europa.ec.edelivery.smp.data.dao.SubresourceDao;
import eu.europa.ec.edelivery.smp.data.enums.DocumentVersionEventType;
import eu.europa.ec.edelivery.smp.data.enums.DocumentVersionStatusType;
import eu.europa.ec.edelivery.smp.data.enums.EventSourceType;
import eu.europa.ec.edelivery.smp.data.model.doc.DBDocument;
import eu.europa.ec.edelivery.smp.data.model.doc.DBDocumentVersion;
import eu.europa.ec.edelivery.smp.data.model.doc.DBResource;
import eu.europa.ec.edelivery.smp.data.model.doc.DBSubresource;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.smp.spi.enums.TransientDocumentPropertyType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static eu.europa.ec.smp.spi.enums.TransientDocumentPropertyType.*;

/**
 * The class handles the resource action as creating, updating and reading the resources.
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Service
public class ResourceStorage {
    protected static final SMPLogger LOG = SMPLoggerFactory.getLogger(ResourceStorage.class);
    final DocumentDao documentDao;
    final ResourceDao resourceDao;
    final SubresourceDao subresourceDao;
    private final DocumentVersionService documentVersionService;

    public ResourceStorage(DocumentDao documentDao, ResourceDao resourceDao, SubresourceDao subresourceDao, DocumentVersionService documentVersionService) {
        this.documentDao = documentDao;
        this.resourceDao = resourceDao;
        this.subresourceDao = subresourceDao;
        this.documentVersionService = documentVersionService;
    }

    public byte[] getDocumentContentForResource(DBResource dbResource) {
        LOG.debug("getDocumentContentForResource: [{}]", dbResource);
        Optional<DBDocumentVersion> documentVersion = this.documentDao.getCurrentDocumentVersionForResource(dbResource);
        return documentVersion.isPresent() ? documentVersion.get().getContent() : null;
    }

    public byte[] getDocumentContentForSubresource(DBSubresource subresource) {
        LOG.debug("getDocumentContentForSubresource: [{}]", subresource);
        Optional<DBDocumentVersion> documentVersion = documentDao.getCurrentDocumentVersionForSubresource(subresource);

        return documentVersion.isPresent() ? documentVersion.get().getContent() : null;
    }


    @Transactional
    public Map<String, Object> getResourceProperties(DBResource resource) {

        DBDocument document = documentDao.getDocumentForResource(resource).orElseGet(null);
        if (document == null) {
            LOG.debug("Document not found for resource [{}]", resource);
            return Collections.emptyMap();
        }

        Map<String, Object> documentProperties = new HashMap<>();
        documentProperties.put(TransientDocumentPropertyType.RESOURCE_IDENTIFIER_VALUE.getPropertyName(), resource.getIdentifierValue());
        if (resource.getIdentifierScheme() != null) {
            documentProperties.put(TransientDocumentPropertyType.RESOURCE_IDENTIFIER_SCHEME.getPropertyName(), resource.getIdentifierScheme());
        }
        // add document properties
        documentProperties.putAll(getDocumentProperties(document));
        return documentProperties;
    }

    /**
     * Method returns the 'transient' and custom document properties as map. If
     * document is null, empty map is returned.
     *
     * @param document document
     * @return document properties the key, value map
     */
    private Map<String, Object> getDocumentProperties(DBDocument document) {
        if (document == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> documentProperties = new HashMap<>();
        documentProperties.put(DOCUMENT_NAME.getPropertyName(), document.getName());
        documentProperties.put(DOCUMENT_MIMETYPE.getPropertyName(), document.getMimeType());
        documentProperties.put(DOCUMENT_VERSION.getPropertyName(), document.getCurrentVersion());

        document.getDocumentProperties().forEach(property -> {
            documentProperties.put(property.getProperty(), property.getValue());
        });
        return documentProperties;
    }

    @Transactional
    public Map<String, Object> getSubresourceProperties(DBResource resource, DBSubresource subresource) {

        Map<String, Object> documentProperties = new HashMap<>();
        DBDocument document = documentDao.getDocumentForSubresource(subresource).orElseGet(null);
        if (document == null) {
            LOG.debug("Document not found for subresource [{}]", resource);
            return Collections.emptyMap();
        }
        // add identifiers
        documentProperties.put(TransientDocumentPropertyType.RESOURCE_IDENTIFIER_VALUE.getPropertyName(), resource.getIdentifierValue());
        if (resource.getIdentifierScheme() != null) {
            documentProperties.put(TransientDocumentPropertyType.RESOURCE_IDENTIFIER_SCHEME.getPropertyName(), resource.getIdentifierScheme());
        }
        documentProperties.put(SUBRESOURCE_IDENTIFIER_VALUE.getPropertyName(), subresource.getIdentifierValue());
        if (subresource.getIdentifierScheme() != null) {
            documentProperties.put(SUBRESOURCE_IDENTIFIER_SCHEME.getPropertyName(), subresource.getIdentifierScheme());
        }

        // add document properties
        documentProperties.putAll(getDocumentProperties(document));
        return documentProperties;
    }


    @Transactional
    public DBResource addDocumentVersionForResource(DBResource resource, DBDocumentVersion version) {
        LOG.debug("addDocumentVersionForResource: [{}]", resource);
        if (resource.getId() == null && resource.getDocument() == null) {
            resource.setDocument(new DBDocument());
        }
        DBResource managedResource = resource.getId() != null ? resourceDao.find(resource.getId()) : resourceDao.merge(resource);
        DBDocument document = managedResource.getDocument();

            // if document is not and alread have published version, retire it
        document.getDocumentVersions().stream().filter(v -> v.getStatus() == DocumentVersionStatusType.PUBLISHED)
                .forEach(documentVersion -> documentVersionService.retireDocumentVersion(documentVersion, EventSourceType.REST_API, null));


        managedResource.getDocument().addNewDocumentVersion(version);
        return managedResource;
    }

    @Transactional
    public DBSubresource addDocumentVersionForSubresource(DBSubresource subresource, DBDocumentVersion version) {
        LOG.debug("addDocumentVersionForSubresource: [{}]", subresource);
        if (subresource.getId() == null && subresource.getDocument() == null) {
            subresource.setDocument(new DBDocument());
        }
        DBSubresource managedResource = subresource.getId() != null ? subresourceDao.find(subresource.getId()) : subresourceDao.merge(subresource);
        DBDocument document = managedResource.getDocument();
        // retire existing published version
        document.getDocumentVersions().stream()
                .filter(v -> v.getStatus() == DocumentVersionStatusType.PUBLISHED)
                .forEach(v -> {v.setStatus(DocumentVersionStatusType.RETIRED);
                    v.getDocumentVersionEvents().add(documentVersionService.createDocumentVersionEvent(DocumentVersionEventType.RETIRE, EventSourceType.REST_API, null));});
        managedResource.getDocument().addNewDocumentVersion(version);
        return managedResource;
    }

    @Transactional
    public void deleteResource(DBResource resource) {
        LOG.debug("deleteResource: [{}]", resource);
        resourceDao.remove(resource);
    }

    public void deleteSubresource(DBSubresource subresource) {
        LOG.debug("deleteSubresource: [{}]", subresource);
        subresourceDao.remove(subresource);
    }

}
