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

import eu.europa.ec.edelivery.smp.auth.SMPUserDetails;
import eu.europa.ec.edelivery.smp.data.dao.*;
import eu.europa.ec.edelivery.smp.data.enums.MembershipRoleType;
import eu.europa.ec.edelivery.smp.data.model.DBDomain;
import eu.europa.ec.edelivery.smp.data.model.DBDomainResourceDef;
import eu.europa.ec.edelivery.smp.data.model.user.DBDomainMember;
import eu.europa.ec.edelivery.smp.data.model.user.DBUser;
import eu.europa.ec.edelivery.smp.data.ui.*;
import eu.europa.ec.edelivery.smp.data.ui.auth.SMPRole;
import eu.europa.ec.edelivery.smp.exceptions.BadRequestException;
import eu.europa.ec.edelivery.smp.exceptions.ErrorBusinessCode;
import eu.europa.ec.edelivery.smp.exceptions.ErrorCode;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.utils.SessionSecurityUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service bean provides only public/edit domain entity data for the Domain.
 * To access most of the data user must have Domain administrator permissions.
 *
 * @author Joze Rihtarsic
 * @since 4.2
 */
@Service
public class UIDomainEditService extends UIServiceBase<DBDomain, DomainPublicRO> {

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(UIDomainEditService.class);
    public static final String DOMAIN_DOES_NOT_EXIST_IN_DATABASE = "Domain does not exist in database!";
    private final DomainDao domainDao;
    private final DomainConfigurationDao domainConfigurationDao;
    private final DomainMemberDao domainMemberDao;
    private final UserDao userDao;
    private final ConversionService conversionService;


    public UIDomainEditService(DomainDao domainDao,
                               DomainConfigurationDao domainConfigurationDao,
                               DomainMemberDao domainMemberDao, ConversionService conversionService, UserDao userDao) {
        this.domainDao = domainDao;
        this.domainConfigurationDao = domainConfigurationDao;
        this.domainMemberDao = domainMemberDao;
        this.conversionService = conversionService;
        this.userDao = userDao;
    }

    @Override
    protected BaseDao<DBDomain> getDatabaseDao() {
        return domainDao;
    }

    /**
     * Method returns Domain resource object list for page.
     *
     * @param page      - page number
     * @param pageSize  - page size
     * @param sortField - sort field
     * @param sortOrder - sort order
     * @param filter    - filter
     * @return ServiceResult<DomainPublicRO> - list of domain resource objects
     */
    @Override
    public ServiceResult<DomainPublicRO> getTableList(int page, int pageSize,
                                                      String sortField,
                                                      String sortOrder, Object filter) {
        LOG.debug("Query for public domain data: page: [{}], page size [{}], sort: [{}], filter: [{}].", page, pageSize, sortField, filter);
        return super.getTableList(page, pageSize, sortField, sortOrder, filter);
    }


    /**
     * Method returns only domains  current users have access to.
     *
     * @param page      - page number
     * @param pageSize  - page size
     * @return ServiceResult<DomainPublicRO> - list of domain resource objects
     */
    public ServiceResult<DomainPublicRO> getUserPermittedDomains(int page, int pageSize) {
        LOG.debug("Query for public domain data: page: [{}], page size [{}].", page, pageSize);
        SMPUserDetails userDetails = SessionSecurityUtils.getSessionUserDetails();
        DBUser user = userDetails!=null?userDetails.getUser():null;

        ServiceResult<DomainPublicRO> result = new ServiceResult<>();
        result.setPage(page);
        result.setPageSize(pageSize);
        Long count = domainDao.getAllDomainsForUserCount(user);
        if (count < 1) {
            result.setCount(0L);
            return result;
        }
        result.setCount(count);
        List<DomainPublicRO> refList = domainDao.getAllDomainsForUser(user, page, pageSize).stream()
                .map(doc -> conversionService.convert(doc, DomainPublicRO.class))
                .collect(Collectors.toList());
        result.getServiceEntities().addAll(refList);
        return result;
    }

    @Transactional
    public List<DomainRO> getAllDomainsForDomainAdminUser(Long userId) {
        List<DBDomain> domains = domainDao.getDomainsByUserIdAndDomainRoles(userId, MembershipRoleType.ADMIN);
        return domains.stream().map(domain -> conversionService.convert(domain, DomainRO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<DomainRO> getAllDomainsForGroupAdminUser(Long userId) {
        List<DBDomain> domains = domainDao.getDomainsByUserIdAndGroupRoles(userId, MembershipRoleType.ADMIN);
        return domains.stream().map(domain -> conversionService.convert(domain, DomainRO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<DomainRO> getAllDomainsForResourceAdminUser(Long userId) {
        List<DBDomain> domains = domainDao.getDomainsByUserIdAndResourceRoles(userId, MembershipRoleType.ADMIN);
        return domains.stream().map(domain -> conversionService.convert(domain, DomainRO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceResult<MemberRO> getDomainMembers(Long domainId, int page, int pageSize,
                                                    String filter) {
        Long count = domainMemberDao.getDomainMemberCount(domainId, filter);
        ServiceResult<MemberRO> result = new ServiceResult<>();
        result.setPage(page);
        result.setPageSize(pageSize);
        if (count < 1) {
            result.setCount(0L);
            return result;
        }
        result.setCount(count);
        List<DBDomainMember> memberROS = domainMemberDao.getDomainMembers(domainId, page, pageSize, filter);
        List<MemberRO> memberList = memberROS.stream().map(member -> conversionService.convert(member, MemberRO.class)).collect(Collectors.toList());

        result.getServiceEntities().addAll(memberList);
        return result;
    }

    @Transactional
    public MemberRO addMemberToDomain(Long domainId, MemberRO memberRO, Long memberId) {
        LOG.info("Add member [{}] to domain [{}]", memberRO.getUsername(), domainId);
        DBUser user = userDao.findUserByUsername(memberRO.getUsername())
                .orElseThrow(() -> new SMPRuntimeException(ErrorCode.INVALID_REQUEST, "Add/edit membership", "User [" + memberRO.getUsername() + "] does not exists!"));

        DBDomainMember domainMember;
        if (memberId != null) {
            domainMember = domainMemberDao.find(memberId);
            domainMember.setRole(memberRO.getRoleType());
        } else {
            DBDomain domain = domainDao.find(domainId);
            if (domainMemberDao.isUserDomainMember(user, domain)) {
                throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, "Add membership", "User [" + memberRO.getUsername() + "] is already a member!");
            }
            domainMember = domainMemberDao.addMemberToDomain(domain, user, memberRO.getRoleType());
        }
        return conversionService.convert(domainMember, MemberRO.class);
    }

    @Transactional
    public MemberRO deleteMemberFromDomain(Long domainId, Long memberId) {
        LOG.info("Delete member [{}] from domain [{}]", memberId, domainId);
        DBDomainMember domainMember = domainMemberDao.find(memberId);
        if (domainMember == null) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, "Membership", "Membership does not exists!");
        }
        if (!Objects.equals(domainMember.getDomain().getId(), domainId)) {
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, "Membership", "Membership does not belong to domain!");
        }

        domainMemberDao.remove(domainMember);
        return conversionService.convert(domainMember, MemberRO.class);
    }

    @Transactional
    public List<ResourceDefinitionRO> getResourceDefDomainList(Long domainId) {
        DBDomain domain = domainDao.find(domainId);
        if (domain == null) {
            LOG.warn("Can not get domain for ID [{}], because it does not exists!", domainId);
            throw new BadRequestException(ErrorBusinessCode.NOT_FOUND, DOMAIN_DOES_NOT_EXIST_IN_DATABASE);
        }

        //filter and validate resources to be removed
        List<DBDomainResourceDef> domainResourceDefs = domain.getDomainResourceDefs();
        return domainResourceDefs.stream().map(DBDomainResourceDef::getResourceDef).map(resourceDef ->
                conversionService.convert(resourceDef, ResourceDefinitionRO.class)).collect(Collectors.toList());
    }

    /**
     * Method returns all Domain properties which are not tagged as system admin only!
     *
     * @param domainId - domain to get properties
     * @return list of domain properties
     */
    public List<DomainPropertyRO> getDomainEditProperties(Long domainId) {
        DBDomain domain = domainDao.find(domainId);
        if (domain == null) {
            throw new BadRequestException(ErrorBusinessCode.NOT_FOUND, DOMAIN_DOES_NOT_EXIST_IN_DATABASE);
        }
        return domainConfigurationDao.getDomainPropertiesForRole(domain, SMPRole.USER).stream()
                .map(property -> conversionService.convert(property, DomainPropertyRO.class))
                .collect(Collectors.toList());
    }

    /**
     * Method updates domain properties which are not system admin only.
     *
     * @param domainId         - domain to update properties
     * @param domainProperties - list of domain properties
     * @return list of updated domain properties
     */
    @Transactional
    public List<DomainPropertyRO> updateDomainEditProperties(Long domainId, List<DomainPropertyRO> domainProperties) {
        DBDomain domain = domainDao.find(domainId);
        if (domain == null) {
            throw new BadRequestException(ErrorBusinessCode.NOT_FOUND, DOMAIN_DOES_NOT_EXIST_IN_DATABASE);
        }
        return domainConfigurationDao.updateDomainPropertiesForRole(domain, domainProperties, SMPRole.USER).stream()
                .map(property -> conversionService.convert(property, DomainPropertyRO.class))
                .collect(Collectors.toList());
    }

    public PropertyValidationRO validateDomainProperty(PropertyRO propertyRO) {
        return domainConfigurationDao.validateDomainProperty(propertyRO);
    }
}
