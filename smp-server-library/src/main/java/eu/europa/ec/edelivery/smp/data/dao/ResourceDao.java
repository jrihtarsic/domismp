/*-
 * #START_LICENSE#
 * smp-webapp
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

package eu.europa.ec.edelivery.smp.data.dao;

import eu.europa.ec.edelivery.smp.data.model.DBDomain;
import eu.europa.ec.edelivery.smp.data.model.doc.DBResource;
import eu.europa.ec.edelivery.smp.data.model.doc.DBResourceFilter;
import eu.europa.ec.edelivery.smp.data.model.ext.DBResourceDef;
import eu.europa.ec.edelivery.smp.data.model.user.DBUser;
import eu.europa.ec.edelivery.smp.exceptions.ErrorCode;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

import static eu.europa.ec.edelivery.smp.data.dao.QueryNames.*;


/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Repository
public class ResourceDao extends BaseDao<DBResource> {

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(ResourceDao.class);


    /**
     * The method returns DBResource for the participant identifier, domain, and resource type. If the resource does not exist, it returns an empty Option.
     * If more than one result exist, it returns IllegalStateException caused by database data inconsistency. Only one combination of
     * participant identifier must be registered in database for the domain and the resource type.
     *
     * @param identifierValue  resource identifier value
     * @param identifierSchema resource identifier schema
     * @return DBResource from the database
     */
    public Optional<DBResource> getResource(String identifierValue, String identifierSchema, DBResourceDef resourceDef, DBDomain domain) {
        LOG.debug("Get resource (identifier [{}], scheme [{}])", identifierValue, identifierSchema);
        try {
            TypedQuery<DBResource> query = memEManager.createNamedQuery(QUERY_RESOURCE_BY_IDENTIFIER_RESOURCE_DEF_DOMAIN, DBResource.class);
            query.setParameter(PARAM_DOMAIN_ID, domain.getId());
            query.setParameter(PARAM_RESOURCE_DEF_ID, resourceDef.getId());
            query.setParameter(IDENTIFIER_VALUE, identifierValue);
            query.setParameter(IDENTIFIER_SCHEME, identifierSchema);
            DBResource res = query.getSingleResult();
            return Optional.of(res);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (NonUniqueResultException e) {
            throw new IllegalStateException(ErrorCode.ILLEGAL_STATE_SG_MULTIPLE_ENTRY.getMessage(identifierValue, identifierSchema));
        }
    }


    public Long getResourcesForFilterCount(DBResourceFilter resourceFilter) {
        LOG.debug("Get resources count for filter [{}]", resourceFilter);

        TypedQuery<Long> query = memEManager.createNamedQuery(QUERY_RESOURCE_FILTER_COUNT, Long.class);
        query.setParameter(PARAM_GROUP_ID, resourceFilter.getGroupId());
        query.setParameter(PARAM_DOMAIN_ID, resourceFilter.getDomainId());
        query.setParameter(PARAM_RESOURCE_DEF_ID, resourceFilter.getResourceDefId());
        query.setParameter(PARAM_USER_ID, resourceFilter.getUserId());
        query.setParameter(PARAM_MEMBERSHIP_ROLES, resourceFilter.getMembershipRoleTypes());
        query.setParameter(PARAM_RESOURCE_FILTER, resourceFilter.getIdentifierFilter());
        return query.getSingleResult();
    }

    public List<DBResource> getResourcesForFilter(int iPage, int iPageSize, DBResourceFilter resourceFilter) {
        LOG.debug("Get resources page [{}] and page size [{}] for filter [{}]", iPage, iPageSize, resourceFilter);
        TypedQuery<DBResource> query = memEManager.createNamedQuery(QUERY_RESOURCE_FILTER, DBResource.class);

        if (iPageSize > -1 && iPage > -1) {
            query.setFirstResult(iPage * iPageSize);
        }
        if (iPageSize > 0) {
            query.setMaxResults(iPageSize);
        }

        query.setParameter(PARAM_GROUP_ID, resourceFilter.getGroupId());
        query.setParameter(PARAM_DOMAIN_ID, resourceFilter.getDomainId());
        query.setParameter(PARAM_RESOURCE_DEF_ID, resourceFilter.getResourceDefId());
        query.setParameter(PARAM_USER_ID, resourceFilter.getUserId());
        query.setParameter(PARAM_MEMBERSHIP_ROLES, resourceFilter.getMembershipRoleTypes());
        query.setParameter(PARAM_RESOURCE_FILTER, resourceFilter.getIdentifierFilter());
        return query.getResultList();
    }

    public List<DBResource> getPublicResourcesSearch(int iPage, int iPageSize, DBUser user, String schema, String identifier) {
        LOG.debug("Get resources list for user [{}], search scheme [{}] and search value [{}]", user, schema, identifier);

        TypedQuery<DBResource> query = memEManager.createNamedQuery(QUERY_RESOURCE_ALL_FOR_USER, DBResource.class);
        if (iPageSize > -1 && iPage > -1) {
            query.setFirstResult(iPage * iPageSize);
        }
        if (iPageSize > 0) {
            query.setMaxResults(iPageSize);
        }
        query.setParameter(PARAM_USER_ID, user != null ? user.getId() : null);
        query.setParameter(PARAM_RESOURCE_SCHEME, StringUtils.isBlank(schema) ? null : StringUtils.wrapIfMissing(schema, "%"));
        query.setParameter(PARAM_RESOURCE_IDENTIFIER, StringUtils.isBlank(identifier) ? null : StringUtils.wrapIfMissing(identifier, "%"));

        return query.getResultList();
    }

    public Long getPublicResourcesSearchCount(DBUser user, String schema, String identifier) {
        LOG.debug("Get resources count for user [{}], search scheme [{}] and search value [{}]", user, schema, identifier);
        TypedQuery<Long> query = memEManager.createNamedQuery(QUERY_RESOURCE_ALL_FOR_USER_COUNT, Long.class);

        query.setParameter(PARAM_USER_ID, user != null ? user.getId() : null);
        query.setParameter(PARAM_RESOURCE_SCHEME, StringUtils.isBlank(schema) ? null : StringUtils.wrapIfMissing(schema, "%"));
        query.setParameter(PARAM_RESOURCE_IDENTIFIER, StringUtils.isBlank(identifier) ? null : StringUtils.wrapIfMissing(identifier, "%"));

        return query.getSingleResult();
    }


    /**
     * Method returns ServiceGroup by participant identifier. If there is no service group it returns empty Option.
     * If more than one result returns IllegalStateException caused by database data inconsistency. Only one combination of
     * participant identifier must be in the database.
     *
     * @param participantId participant identifier
     * @param schema        participant identifier schema
     * @return DBResource
     */
    public Optional<DBResource> findServiceGroup(String participantId, String schema) {


        try {
            TypedQuery<DBResource> query = memEManager.createNamedQuery("DBResource.getServiceGroupByIdentifier", DBResource.class);
            query.setParameter("participantIdentifier", participantId);
            query.setParameter("participantScheme", schema);
            DBResource res = query.getSingleResult();
            return Optional.of(res);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (NonUniqueResultException e) {
            throw new IllegalStateException(ErrorCode.ILLEGAL_STATE_SG_MULTIPLE_ENTRY.getMessage(participantId, schema));
        }
    }


    public Long getResourceCountForDomainIdAndResourceDefId(Long domainId, Long resourceDefId) {
        TypedQuery<Long> query = memEManager.createNamedQuery(QUERY_RESOURCES_BY_DOMAIN_ID_RESOURCE_DEF_ID_COUNT, Long.class);
        query.setParameter(PARAM_DOMAIN_ID, domainId);
        query.setParameter(PARAM_RESOURCE_DEF_ID, resourceDefId);
        return query.getSingleResult();
    }

    /**
     * Method removes the resource from DB. Related entities (cascade): sub-resources, Document, Document version,
     * group memberships,
     *
     * @param resource
     */
    @Transactional
    public void remove(DBResource resource) {
        removeById(resource.getId());
    }
}
