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

import eu.europa.ec.edelivery.smp.data.dao.*;
import eu.europa.ec.edelivery.smp.data.enums.DocumentVersionStatusType;
import eu.europa.ec.edelivery.smp.data.enums.EventSourceType;
import eu.europa.ec.edelivery.smp.data.enums.MembershipRoleType;
import eu.europa.ec.edelivery.smp.data.model.DBDomain;
import eu.europa.ec.edelivery.smp.data.model.DBDomainResourceDef;
import eu.europa.ec.edelivery.smp.data.model.DBGroup;
import eu.europa.ec.edelivery.smp.data.model.doc.DBDocument;
import eu.europa.ec.edelivery.smp.data.model.doc.DBDocumentVersion;
import eu.europa.ec.edelivery.smp.data.model.doc.DBResource;
import eu.europa.ec.edelivery.smp.data.model.doc.DBResourceFilter;
import eu.europa.ec.edelivery.smp.data.model.ext.DBResourceDef;
import eu.europa.ec.edelivery.smp.data.model.user.DBResourceMember;
import eu.europa.ec.edelivery.smp.data.model.user.DBUser;
import eu.europa.ec.edelivery.smp.data.ui.MemberRO;
import eu.europa.ec.edelivery.smp.data.ui.ResourceRO;
import eu.europa.ec.edelivery.smp.data.ui.ServiceResult;
import eu.europa.ec.edelivery.smp.exceptions.ErrorCode;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import eu.europa.ec.edelivery.smp.identifiers.Identifier;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.services.IdentifierService;
import eu.europa.ec.edelivery.smp.services.SMLIntegrationService;
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

import static org.apache.commons.lang3.BooleanUtils.isTrue;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */

@Service
public class UIResourceService {
    private static final String ACTION_RESOURCE_LIST = "GetResourceListForGroup";
    private static final String ACTION_RESOURCE_CREATE = "CreateResourceForGroup";
    private static final String ACTION_RESOURCE_DELETE = "DeleteResourceFromGroup";
    private static final String ACTION_RESOURCE_UPDATE = "UpdateResource";

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(UIResourceService.class);
    public static final String GROUP_DOES_NOT_EXIST = "Group does not exist!";
    public static final String GROUP_DOES_NOT_BELONG_TO_THE_GIVEN_DOMAIN = "Group does not belong to the given domain!";


    private final ResourceDao resourceDao;

    private final GroupDao groupDao;
    private final ResourceMemberDao resourceMemberDao;
    private final UserDao userDao;
    private final DocumentDao documentDao;
    private final ResourceDefDao resourceDefDao;
    private final DomainResourceDefDao domainResourceDefDao;
    private final IdentifierService identifierService;
    private final ConversionService conversionService;
    private final SMLIntegrationService smlIntegrationService;
    private final UIDocumentService uiDocumentService;
    private final DocumentVersionService documentVersionService;


    public UIResourceService(ResourceDao resourceDao,
                             ResourceMemberDao resourceMemberDao,
                             ResourceDefDao resourceDefDao,
                             DocumentDao documentDao,
                             DomainResourceDefDao domainResourceDefDao, UserDao userDao, GroupDao groupDao,
                             IdentifierService identifierService,
                             ConversionService conversionService,
                             SMLIntegrationService smlIntegrationService,
                             UIDocumentService uiDocumentService, DocumentVersionService documentVersionService) {
        this.resourceDao = resourceDao;
        this.resourceMemberDao = resourceMemberDao;
        this.resourceDefDao = resourceDefDao;
        this.documentDao = documentDao;
        this.domainResourceDefDao = domainResourceDefDao;
        this.groupDao = groupDao;
        this.userDao = userDao;
        this.identifierService = identifierService;
        this.conversionService = conversionService;
        this.smlIntegrationService = smlIntegrationService;
        this.uiDocumentService = uiDocumentService;
        this.documentVersionService = documentVersionService;
    }


    @Transactional
    public ServiceResult<ResourceRO> getGroupResources(Long groupId, int page, int pageSize, String filterValue) {

        DBGroup group = groupDao.find(groupId);
        if (group == null) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_RESOURCE_LIST, GROUP_DOES_NOT_EXIST);
        }

        DBResourceFilter filter = DBResourceFilter.createBuilder()
                .group(group)
                .identifierFilter(StringUtils.trimToNull(filterValue))
                .build();

        Long count = resourceDao.getResourcesForFilterCount(filter);

        ServiceResult<ResourceRO> result = new ServiceResult<>();
        result.setPage(page);
        result.setPageSize(pageSize);
        if (count < 1) {
            result.setCount(0L);
            return result;
        }
        result.setCount(count);
        List<DBResource> resources = resourceDao.getResourcesForFilter(page, pageSize, filter);
        List<ResourceRO> resourceROS = resources.stream().map(resource -> conversionService.convert(resource, ResourceRO.class)).collect(Collectors.toList());
        resourceDao.getResourcesForFilter(page, pageSize, filter);
        result.getServiceEntities().addAll(resourceROS);
        return result;
    }


    @Transactional
    public ServiceResult<ResourceRO> getResourcesForUserAndGroup(Long userId, MembershipRoleType role, Long groupId, int page, int pageSize, String filterValue) {

        DBGroup group = groupDao.find(groupId);
        if (group == null) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_RESOURCE_LIST, GROUP_DOES_NOT_EXIST);
        }
        DBUser user = userDao.find(userId);
        if (user == null) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_RESOURCE_LIST, "User does not exist!");
        }

        DBResourceFilter filter = DBResourceFilter.createBuilder()
                .user(user)
                .membershipRoleType(role)
                .group(group)
                .identifierFilter(StringUtils.trimToNull(filterValue))
                .build();

        Long count = resourceDao.getResourcesForFilterCount(filter);

        ServiceResult<ResourceRO> result = new ServiceResult<>();
        result.setPage(page);
        result.setPageSize(pageSize);
        if (count < 1) {
            result.setCount(0L);
            return result;
        }
        result.setCount(count);
        List<DBResource> resources = resourceDao.getResourcesForFilter(page, pageSize, filter);
        List<ResourceRO> resourceROS = resources.stream().map(resource -> conversionService.convert(resource, ResourceRO.class)).collect(Collectors.toList());
        resourceDao.getResourcesForFilter(page, pageSize, filter);
        result.getServiceEntities().addAll(resourceROS);
        return result;
    }

    @Transactional
    public ResourceRO deleteResourceFromGroup(Long resourceId, Long groupId, Long domainId) {
        DBResource resource = resourceDao.find(resourceId);
        if (resource == null) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_RESOURCE_DELETE, "Resource does not exist!");
        }
        if (!Objects.equals(resource.getGroup().getId(), groupId)) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_RESOURCE_DELETE, "Resource does not belong to the group!");
        }
        if (!Objects.equals(resource.getGroup().getDomain().getId(), domainId)) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_RESOURCE_DELETE, GROUP_DOES_NOT_BELONG_TO_THE_GIVEN_DOMAIN);
        }
        DBDomain resourceDomain = resource.getGroup().getDomain();
        if (smlIntegrationService.isSMLIntegrationEnabled() &&
                resourceDomain.isSmlRegistered() && resource.isSmlRegistered()) {
            smlIntegrationService.unregisterParticipant(resource, resourceDomain);
        }

        // remove all documents where resource is used as reference
        documentDao.unlinkDocument(resource.getDocument());

        resourceDao.remove(resource);
        return conversionService.convert(resource, ResourceRO.class);
    }

    @Transactional
    public ResourceRO createResourceForGroup(ResourceRO resourceRO, Long groupId, Long domainId, Long userId) {

        DBGroup group = groupDao.find(groupId);
        if (group == null) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_RESOURCE_CREATE, GROUP_DOES_NOT_EXIST);
        }

        DBDomain domain = group.getDomain();
        if (!Objects.equals(domain.getId(), domainId)) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_RESOURCE_CREATE, GROUP_DOES_NOT_BELONG_TO_THE_GIVEN_DOMAIN);
        }

        Optional<DBResourceDef> optRedef = resourceDefDao.getResourceDefByIdentifier(resourceRO.getResourceTypeIdentifier());
        if (!optRedef.isPresent()) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_RESOURCE_CREATE, "Resource definition [" + resourceRO.getResourceTypeIdentifier() + "] does not exist!");
        }

        Optional<DBDomainResourceDef> optDoredef = domainResourceDefDao.getResourceDefConfigurationForDomainAndResourceDef(group.getDomain(), optRedef.get());
        if (!optDoredef.isPresent()) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_RESOURCE_CREATE, "Resource definition [" + resourceRO.getResourceTypeIdentifier() + "] is not registered for domain!");
        }
        Identifier resourceIdentifier = identifierService.normalizeParticipant(
                domain.getDomainCode(),
                resourceRO.getIdentifierScheme(),
                resourceRO.getIdentifierValue());
        boolean isResourceIdentifierCaseSensitive = identifierService.isResourceIdentifierCaseSensitive(resourceIdentifier, domain.getDomainCode());

        Optional<DBResource> existResource = resourceDao.getResource(resourceIdentifier.getValue(),
                resourceIdentifier.getScheme(),
                optRedef.get(),
                group.getDomain(),
                isResourceIdentifierCaseSensitive);
        if (existResource.isPresent()) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_RESOURCE_CREATE, "Resource [val:" + resourceRO.getIdentifierValue() + " scheme:" + resourceRO.getIdentifierScheme() + "] already exists for domain!");
        }

        DBResource resource = new DBResource();
        resource.setIdentifierScheme(resourceIdentifier.getScheme());
        resource.setIdentifierValue(resourceIdentifier.getValue());
        resource.setVisibility(resourceRO.getVisibility());
        resource.setReviewEnabled(resourceRO.isReviewEnabled());
        resource.setGroup(group);
        resource.setDomainResourceDef(optDoredef.get());
        resource.setReviewEnabled(resourceRO.isReviewEnabled());

        DBDocument document = createDocumentForNewResource(resource);
        resource.setDocument(document);
        resourceDao.persist(resource);
        // create first member as admin user
        DBUser user = userDao.find(userId);
        DBResourceMember dbResourceMember = new DBResourceMember();
        dbResourceMember.setRole(MembershipRoleType.ADMIN);
        dbResourceMember.setResource(resource);
        dbResourceMember.setUser(user);
        resourceMemberDao.persist(dbResourceMember);
        // try to register it to
        DBDomain resourceDomain = resource.getGroup().getDomain();
        if (smlIntegrationService.isSMLIntegrationEnabled() &&
                resourceDomain.isSmlRegistered()) {
            smlIntegrationService.registerParticipant(resource, resourceDomain);
        }

        return conversionService.convert(resource, ResourceRO.class);
    }

    /**
     * Method allows Group admin and Resource admin to change resource visibility and enable/disable review flow.
     *
     * @param resourceRO
     * @param resourceId
     * @param groupId
     * @param domainId
     * @return
     */
    @Transactional
    public ResourceRO updateResourceForGroup(ResourceRO resourceRO, Long resourceId, Long groupId, Long domainId) {

        DBGroup group = groupDao.find(groupId);
        if (group == null) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_RESOURCE_UPDATE, GROUP_DOES_NOT_EXIST);
        }

        if (!Objects.equals(group.getDomain().getId(), domainId)) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_RESOURCE_UPDATE, GROUP_DOES_NOT_BELONG_TO_THE_GIVEN_DOMAIN);
        }

        Optional<DBResourceDef> optRedef = resourceDefDao.getResourceDefByIdentifier(resourceRO.getResourceTypeIdentifier());
        if (!optRedef.isPresent()) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_RESOURCE_UPDATE, "Resource definition [" + resourceRO.getResourceTypeIdentifier() + "] does not exist!");
        }

        Optional<DBDomainResourceDef> optDoredef = domainResourceDefDao.getResourceDefConfigurationForDomainAndResourceDef(group.getDomain(), optRedef.get());
        if (!optDoredef.isPresent()) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, ACTION_RESOURCE_UPDATE, "Resource definition [" + resourceRO.getResourceTypeIdentifier() + "] is not registered for domain!");
        }

        // at the moment only visibility and review enabled
        // can be updated for the resource
        DBResource resource = resourceDao.find(resourceId);
        resource.setVisibility(resourceRO.getVisibility());
        if (resourceRO.isReviewEnabled() != null) {
            boolean newValue = isTrue(resourceRO.isReviewEnabled());
            boolean oldValue = isTrue(resource.isReviewEnabled());
            // update resource review enabled in case if it was null before
            resource.setReviewEnabled(newValue);
            // check if new status is disabled  and changed
            if (oldValue != newValue && !newValue) {
                // update all document versions to non review status
                uiDocumentService.updateToNonReviewStatuses(resource.getDocument());
                // update statuses for all subresources
                resource.getSubresources().forEach(subResource ->
                        uiDocumentService.updateToNonReviewStatuses(subResource.getDocument()));
            }
            resource.setReviewEnabled(isTrue(resourceRO.isReviewEnabled()));
        }
        ResourceRO resourceROResult = conversionService.convert(resource, ResourceRO.class);
        if (StringUtils.isNotBlank(resourceRO.getResourceId())) {
            // return the same encrypted id so the UI can use update old resource
            resourceROResult.setResourceId(resourceRO.getResourceId());
        }
        return resourceROResult;
    }

    @Transactional
    public ServiceResult<MemberRO> getResourceMembers(Long resourceId, Long groupId, int page, int pageSize,
                                                      String filter) {

        validateGroupAndResource(resourceId, groupId, "GetResourceMembers");
        Long count = resourceMemberDao.getResourceMemberCount(resourceId, filter);
        ServiceResult<MemberRO> result = new ServiceResult<>();
        result.setPage(page);
        result.setPageSize(pageSize);
        if (count < 1) {
            result.setCount(0L);
            return result;
        }
        result.setCount(count);
        List<DBResourceMember> memberROS = resourceMemberDao.getResourceMembers(resourceId, page, pageSize, filter);
        List<MemberRO> memberList = memberROS.stream().map(member -> conversionService.convert(member, MemberRO.class)).collect(Collectors.toList());

        result.getServiceEntities().addAll(memberList);
        return result;
    }

    /**
     * Add or update a member to a resource
     *
     * @param resourceId resource id to add member to
     * @param groupId    group id to add member to
     * @param memberRO   member data
     * @param memberId   member id (optional) if null then add member, if not null then update member
     * @return added member RO
     */
    @Transactional
    public MemberRO addUpdateMemberToResource(Long resourceId, Long groupId, MemberRO memberRO, Long memberId) {
        LOG.info("Add member [{}] to resource [{}]", memberRO.getUsername(), resourceId);
        validateGroupAndResource(resourceId, groupId, "AddMemberToResource");

        DBUser user = userDao.findUserByUsername(memberRO.getUsername())
                .orElseThrow(() -> new SMPRuntimeException(ErrorCode.INVALID_REQUEST, "Add/edit membership", "User [" + memberRO.getUsername() + "] does not exists!"));

        DBResourceMember member;
        if (memberId != null) {
            member = resourceMemberDao.find(memberId);
            member.setRole(memberRO.getRoleType());
            member.setHasPermissionToReview(memberRO.getHasPermissionReview());
        } else {
            DBResource resource = resourceDao.find(resourceId);
            if (resourceMemberDao.isUserResourceMember(user, resource)) {
                throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, "Add membership", "User [" + memberRO.getUsername() + "] is already a member!");
            }
            member = resourceMemberDao.addMemberToResource(resource, user,
                    memberRO.getRoleType(),
                    isTrue(memberRO.getHasPermissionReview())
            );
        }
        return conversionService.convert(member, MemberRO.class);
    }

    @Transactional
    public MemberRO deleteMemberFromResource(Long resourceId, Long groupId, Long memberId) {
        LOG.info("Delete member [{}] from resource [{}]", memberId, resourceId);
        validateGroupAndResource(resourceId, groupId, "DeleteMemberFromResource");
        DBResourceMember resourceMember = resourceMemberDao.find(memberId);
        if (resourceMember == null) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, "Membership", "Membership does not exists!");
        }
        if (!Objects.equals(resourceMember.getResource().getId(), resourceId)) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, "Membership", "Membership does not belong to resource!");
        }

        resourceMemberDao.remove(resourceMember);
        return conversionService.convert(resourceMember, MemberRO.class);
    }

    public DBResource validateGroupAndResource(Long resourceId, Long groupId, String action) {
        DBResource resource = resourceDao.find(resourceId);
        if (resource == null) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, action, "Resource does not exists!");
        }
        if (!Objects.equals(groupId, resource.getGroup().getId())) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, action, "Group does not belong to given domain!");
        }
        return resource;
    }

    /**
     * Create document for new resource. Method is called when GroupAdmin creates new resource via
     * UI.
     *
     * @param resource resource to create document for
     * @return created document
     */
    public DBDocument createDocumentForNewResource(DBResource resource) {
        DBResourceDef domainResourceDef = resource.getDomainResourceDef().getResourceDef();
        DBDocument document = new DBDocument();

        document.setMimeType(domainResourceDef.getMimeType());
        document.setName(StringUtils.left(resource.getIdentifierValue(), 255));
        // create first version of the document
        DBDocumentVersion version = documentVersionService.initializeDocumentVersionByGroupAdmin(EventSourceType.UI);
        // The first version is always published.
        version.setStatus(DocumentVersionStatusType.PUBLISHED);
        version.setDocument(document);
        version.setVersion(1);
        document.setCurrentVersion(1);
        // generate document content
        document.addNewDocumentVersion(version);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        uiDocumentService.generateDocumentForResource(resource, baos);
        version.setContent(baos.toByteArray());
        return document;
    }
}
