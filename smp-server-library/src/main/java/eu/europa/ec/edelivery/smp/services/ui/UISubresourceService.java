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

import eu.europa.ec.edelivery.smp.data.dao.DocumentDao;
import eu.europa.ec.edelivery.smp.data.dao.ResourceDao;
import eu.europa.ec.edelivery.smp.data.dao.SubresourceDao;
import eu.europa.ec.edelivery.smp.data.dao.SubresourceDefDao;
import eu.europa.ec.edelivery.smp.data.enums.DocumentVersionStatusType;
import eu.europa.ec.edelivery.smp.data.enums.EventSourceType;
import eu.europa.ec.edelivery.smp.data.model.DBDomain;
import eu.europa.ec.edelivery.smp.data.model.doc.DBDocument;
import eu.europa.ec.edelivery.smp.data.model.doc.DBDocumentVersion;
import eu.europa.ec.edelivery.smp.data.model.doc.DBResource;
import eu.europa.ec.edelivery.smp.data.model.doc.DBSubresource;
import eu.europa.ec.edelivery.smp.data.model.ext.DBSubresourceDef;
import eu.europa.ec.edelivery.smp.data.ui.SubresourceRO;
import eu.europa.ec.edelivery.smp.exceptions.ErrorCode;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import eu.europa.ec.edelivery.smp.identifiers.Identifier;
import eu.europa.ec.edelivery.smp.services.IdentifierService;
import eu.europa.ec.edelivery.smp.services.resource.DocumentVersionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */

@Service
public class UISubresourceService {

    private static final String ACTION_SUBRESOURCE_CREATE = "CreateSubresourceForResource";
    private static final String ACTION_SUBRESOURCE_DELETE = "DeleteSubresourceFromResource";

    private final SubresourceDao subresourceDao;
    private final ResourceDao resourceDao;
    private final DocumentDao documentDao;
    private final SubresourceDefDao subresourceDefDao;
    private final IdentifierService identifierService;
    private final DocumentVersionService documentVersionService;
    private final UIDocumentService uiDocumentService;
    private final ConversionService conversionService;

    public UISubresourceService(SubresourceDao subresourceDao, ResourceDao resourceDao, SubresourceDefDao subresourceDefDao, IdentifierService identifierService,
                                DocumentDao documentDao,
                                ConversionService conversionService,
                                DocumentVersionService documentVersionService,
                                UIDocumentService uiDocumentService
    ) {
        this.subresourceDao = subresourceDao;
        this.resourceDao = resourceDao;
        this.documentDao = documentDao;
        this.subresourceDefDao = subresourceDefDao;
        this.identifierService = identifierService;
        this.conversionService = conversionService;
        this.documentVersionService = documentVersionService;
        this.uiDocumentService = uiDocumentService;
    }


    @Transactional
    public List<SubresourceRO> getSubResourcesForResource(Long resourceId) {
        List<DBSubresource> list = this.subresourceDao.getSubResourcesForResourceId(resourceId);
        return list.stream().map(subresource -> conversionService.convert(subresource, SubresourceRO.class)).collect(Collectors.toList());
    }

    @Transactional
    public SubresourceRO deleteSubresourceFromResource(Long subResourceId, Long resourceId) {
        DBResource resource = resourceDao.find(resourceId);
        if (resource == null) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_SUBRESOURCE_DELETE, "Resource does not exist!");
        }
        DBSubresource subresource = subresourceDao.find(subResourceId);
        if (subresource == null) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_SUBRESOURCE_DELETE, "Subresource does not exist!");
        }
        if (!Objects.equals(subresource.getResource().getId(), resourceId)) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_SUBRESOURCE_DELETE, "Subresource does not belong to the resource!");
        }
        resource.getSubresources().remove(subresource);
        documentDao.unlinkDocument(subresource.getDocument());
        subresourceDao.remove(subresource);
        return conversionService.convert(subresource, SubresourceRO.class);
    }

    @Transactional
    public SubresourceRO createSubresourceForResource(SubresourceRO subResourceRO, Long resourceId) {

        DBResource resParent = resourceDao.find(resourceId);
        if (resParent == null) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_SUBRESOURCE_CREATE, "Resource does not exist!");
        }

        Optional<DBSubresourceDef> optRedef = subresourceDefDao.getSubresourceDefByIdentifier(subResourceRO.getSubresourceTypeIdentifier());
        if (!optRedef.isPresent()) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_SUBRESOURCE_CREATE, "Subresource definition [" + subResourceRO.getSubresourceTypeIdentifier() + "] does not exist!");
        }
        DBDomain domain = resParent.getDomainResourceDef().getDomain();

        Identifier docId = identifierService.normalizeDocument(domain.getDomainCode(), subResourceRO.getIdentifierScheme(),
                subResourceRO.getIdentifierValue());
        Optional<DBSubresource> exists = subresourceDao.getSubResourcesForResource(docId, resParent);
        if (exists.isPresent()) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_SUBRESOURCE_CREATE, "Subresource definition [val:" + docId.getValue() + " scheme:" + docId.getScheme() + "] already exists for the resource!");
        }

        DBSubresource subresource = new DBSubresource();
        subresource.setIdentifierScheme(docId.getScheme());
        subresource.setIdentifierValue(docId.getValue());
        subresource.setResource(resParent);
        subresource.setSubresourceDef(optRedef.get());
        DBDocument document = createDocumentForSubresourceDef(optRedef.get(), subresource, resParent);
        subresource.setDocument(document);
        subresourceDao.persist(subresource);
        // create first member as admin user
        return conversionService.convert(subresource, SubresourceRO.class);
    }

    public DBDocument createDocumentForSubresourceDef(DBSubresourceDef subresourceDef, DBSubresource subresource, DBResource resource) {
        DBDocument document = new DBDocument();
        document.setCurrentVersion(1);
        document.setMimeType(subresourceDef.getMimeType());
        document.setName(StringUtils.left(subresource.getIdentifierValue(), 255));


        // create first version of the document
        DBDocumentVersion version = documentVersionService.initializeDocumentVersionByGroupAdmin(EventSourceType.UI);

        version.setStatus(DocumentVersionStatusType.PUBLISHED);
        version.setDocument(document);
        version.setVersion(1);
        // generate document content
        document.addNewDocumentVersion(version);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        uiDocumentService.generateDocumentForSubresource(resource, subresource, baos);
        version.setContent(baos.toByteArray());
        return document;
    }
}
