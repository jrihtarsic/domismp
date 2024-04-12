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

import eu.europa.ec.edelivery.smp.data.dao.BaseDao;
import eu.europa.ec.edelivery.smp.data.dao.DomainDao;
import eu.europa.ec.edelivery.smp.data.dao.ResourceDao;
import eu.europa.ec.edelivery.smp.data.dao.UserDao;
import eu.europa.ec.edelivery.smp.data.model.doc.DBResource;
import eu.europa.ec.edelivery.smp.data.model.user.DBUser;
import eu.europa.ec.edelivery.smp.data.ui.ServiceGroupSearchRO;
import eu.europa.ec.edelivery.smp.data.ui.ServiceMetadataRO;
import eu.europa.ec.edelivery.smp.data.ui.ServiceResult;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.services.ui.filters.ResourceFilter;
import eu.europa.ec.edelivery.smp.utils.SessionSecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UIResourceSearchService extends UIServiceBase<DBResource, ServiceGroupSearchRO> {
    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(UIResourceSearchService.class);

    @Autowired
    DomainDao domainDao;

    @Autowired
    ResourceDao resourceDao;

    @Autowired
    UserDao userDao;


    @Override
    protected BaseDao<DBResource> getDatabaseDao() {
        return resourceDao;
    }

    /**
     * Method return list of service group entities with service metadata for given search parameters and page.
     *
     * @param page
     * @param pageSize
     * @param sortField
     * @param sortOrder
     * @param filter
     * @return
     */
    @Transactional
    public ServiceResult<ServiceGroupSearchRO> getTableList(int page, int pageSize,
                                                            String sortField,
                                                            String sortOrder, ResourceFilter filter) {

        ServiceResult<ServiceGroupSearchRO> sg = new ServiceResult<>();
        sg.setPage(page < 0 ? 0 : page);
        sg.setPageSize(pageSize);
        DBUser user = SessionSecurityUtils.getSessionUserDetails() != null ? SessionSecurityUtils.getSessionUserDetails().getUser() : null;

        long iCnt = resourceDao.getPublicResourcesSearchCount(user, filter.getIdentifierSchemeLike(), filter.getIdentifierValueLike());
        sg.setCount(iCnt);

        if (iCnt > 0) {
            int iStartIndex = pageSize < 0 ? -1 : page * pageSize;
            if (iStartIndex >= iCnt && page > 0) {
                page = page - 1;
                sg.setPage(page); // go back for a page
                iStartIndex = pageSize < 0 ? -1 : page * pageSize;
            }
            List<DBResource> lst = resourceDao.getPublicResourcesSearch(page, pageSize, user, filter.getIdentifierSchemeLike(), filter.getIdentifierValueLike());
            List<ServiceGroupSearchRO> lstRo = new ArrayList<>();
            for (DBResource resource : lst) {
                ServiceGroupSearchRO serviceGroupRo = convertToRo(resource);
                serviceGroupRo.setIndex(iStartIndex++);
                lstRo.add(serviceGroupRo);
            }
            sg.getServiceEntities().addAll(lstRo);
        }
        return sg;
    }

    /**
     * Convert Database object to Rest object for UI
     *
     * @param resource - database  entity
     * @return ServiceGroupRO
     */
    public ServiceGroupSearchRO convertToRo(DBResource resource) {
        ServiceGroupSearchRO serviceGroupRo = new ServiceGroupSearchRO();

        serviceGroupRo.setId(resource.getId());
        serviceGroupRo.setDomainCode(resource.getDomainResourceDef().getDomain().getDomainCode());
        serviceGroupRo.setResourceDefUrlSegment(resource.getDomainResourceDef().getResourceDef().getUrlSegment());
        serviceGroupRo.setParticipantIdentifier(resource.getIdentifierValue());
        serviceGroupRo.setParticipantScheme(resource.getIdentifierScheme());

        resource.getSubresources().forEach(subresource -> {
            ServiceMetadataRO smdro = new ServiceMetadataRO();
            smdro.setSubresourceDefUrlSegment(subresource.getSubresourceDef().getUrlSegment());
            smdro.setDocumentIdentifier(subresource.getIdentifierValue());
            smdro.setDocumentIdentifierScheme(subresource.getIdentifierScheme());
            serviceGroupRo.getServiceMetadata().add(smdro);
        });

        return serviceGroupRo;
    }
}
