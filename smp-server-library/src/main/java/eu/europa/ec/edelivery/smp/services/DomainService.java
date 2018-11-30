package eu.europa.ec.edelivery.smp.services;


import eu.europa.ec.edelivery.smp.conversion.CaseSensitivityNormalizer;
import eu.europa.ec.edelivery.smp.data.dao.DomainDao;
import eu.europa.ec.edelivery.smp.data.dao.ServiceGroupDao;
import eu.europa.ec.edelivery.smp.data.model.DBDomain;
import eu.europa.ec.edelivery.smp.data.model.DBServiceGroup;
import eu.europa.ec.edelivery.smp.data.model.DBServiceGroupDomain;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.services.ui.filters.ServiceGroupFilter;
import eu.europa.ec.edelivery.smp.sml.SmlConnector;
import org.apache.commons.lang3.StringUtils;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ParticipantIdentifierType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static eu.europa.ec.edelivery.smp.exceptions.ErrorCode.*;
import static eu.europa.ec.edelivery.smp.logging.SMPMessageCode.BUS_SML_REGISTER_SERVICE_GROUP;
import static eu.europa.ec.edelivery.smp.logging.SMPMessageCode.BUS_SML_REGISTER_SERVICE_GROUP_ALREADY_REGISTERED;
import static eu.europa.ec.edelivery.smp.logging.SMPMessageCode.BUS_SML_REGISTER_SERVICE_GROUP_FAILED;


/**
 * Service for domain
 * @author Joze Rihtarsic
 * @since 4.1
 */

@Service
public class DomainService {

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(DomainService.class);

    public static final Pattern DOMAIN_ID_PATTERN = Pattern.compile("[a-zA-Z0-9]{1,50}");

    @Autowired
    private SMLIntegrationService smlIntegrationService;

    @Autowired
    private ServiceGroupDao serviceGroupDao;

    @Autowired
    private DomainDao domainDao;



    /**
     * Method checks if domain is in right format. Domain must contains only alphanomeric chars and it shoud
     * not be longer than 50 chars.
     *
     * @param domain
     * @return
     */
    @NotNull
    public DBDomain getDomain(final String domain) {
        if (StringUtils.isBlank(domain)) {
            Optional<DBDomain> res = domainDao.getTheOnlyDomain();
            if (!res.isPresent()) {
                throw new SMPRuntimeException(MISSING_DOMAIN);
            }
            return res.get();
        }
        // else test if domain is ok.
        if (!DOMAIN_ID_PATTERN.matcher(domain).matches()) {
            throw new SMPRuntimeException(INVALID_DOMAIN_CODE, domain, DOMAIN_ID_PATTERN);
        }
        // get domain by code
        Optional<DBDomain> domEntity = domainDao.getDomainByCode(domain);
        if (!domEntity.isPresent()) {
            throw new SMPRuntimeException(DOMAIN_NOT_EXISTS, domain);
        }
        return domEntity.get();
    }

    /**
     * If domain is not yet registered and sml integration is on. Than it tries to register domain and  all participants
     * on that domain. If integration is off it return an configuration exception.
     *
     * Method is not in transaction - but sub-methods are. if registering domain or particular serviceGroup succeed
     * then the database flag (SML_REGISTERED) is turned on ( if method fails
     * while execution the SML_REGISTERED reflect the real status in SML). Running the method again updates only
     * serviceGroup which are not yet registered.
     *
     *
     * @param domain
     */

    public void registerDomainAndParticipants(DBDomain domain){
        LOG.info("Start registerDomainAndParticipants for domain:" + domain.getDomainCode());
        smlIntegrationService.registerDomain(domain);

        // get all participant for domain and register them
        ServiceGroupFilter serviceGroupFilter = new ServiceGroupFilter();
        serviceGroupFilter.setDomain(domain);

        // register all service groups
        List<DBServiceGroup> serviceGroupList = serviceGroupDao.getServiceGroupList(-1, -1, null, null, serviceGroupFilter);
        for (DBServiceGroup sg: serviceGroupList){
            smlIntegrationService.registerParticipant(sg.getParticipantIdentifier(), sg.getParticipantScheme(), domain.getDomainCode());
        }
    }

    public void unregisterDomainAndParticipantsFromSml(DBDomain domain){

        // get all participant for domain and register them
        ServiceGroupFilter serviceGroupFilter = new ServiceGroupFilter();
        serviceGroupFilter.setDomain(domain);

        // register all service groups
        List<DBServiceGroup> serviceGroupList = serviceGroupDao.getServiceGroupList(-1, -1, null, null, serviceGroupFilter);
        LOG.info("Unregister participants (count: {}) for domain: {}: ", serviceGroupList.size(), domain.getDomainCode());
        for (DBServiceGroup sg: serviceGroupList){
            smlIntegrationService.unregisterParticipant(sg.getParticipantIdentifier(), sg.getParticipantScheme(), domain.getDomainCode());
        }

        smlIntegrationService.unRegisterDomain(domain);
    }

}
