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
import eu.europa.ec.edelivery.smp.logging.SMPMessageCode;
import eu.europa.ec.edelivery.smp.sml.SmlConnector;
import org.oasis_open.docs.bdxr.ns.smp._2016._05.ParticipantIdentifierType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static eu.europa.ec.edelivery.smp.exceptions.ErrorCode.*;
import static eu.europa.ec.edelivery.smp.logging.SMPMessageCode.*;


/**
 * Service for domain
 *
 * @author Joze Rihtarsic
 * @since 4.1
 */

@Service
public class SMLIntegrationService {

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(SMLIntegrationService.class);

    @Autowired
    private SmlConnector smlConnector;

    @Autowired
    private ServiceGroupDao serviceGroupDao;

    @Autowired
    private DomainDao domainDao;

    @Autowired
    private CaseSensitivityNormalizer caseSensitivityNormalizer;


    /**
     * Method in transaction update domain status and registers domain to SML.
     * If registration fails  - transaction is rolled back
     *
     * @param domain
     */
    @Transactional
    public void registerDomain(DBDomain domain) {
        if (!smlConnector.isSmlIntegrationEnabled()) {
            throw new SMPRuntimeException(CONFIGURATION_ERROR, "SML integration is not enabled!");
        }
        domain.setSmlRegistered(true);
        domainDao.update(domain);
        smlConnector.registerDomain(domain);
    }


    /**
     * Method in transaction update domain status and registers domain to SML.
     * If registration fails  - transaction is rolled back
     *
     * @param domain
     */
    @Transactional
    public void unRegisterDomain(DBDomain domain) {
        if (!smlConnector.isSmlIntegrationEnabled()) {
            throw new SMPRuntimeException(CONFIGURATION_ERROR, "SML integration is not enabled!");
        }

        domain.setSmlRegistered(false);
        domainDao.update(domain);
        smlConnector.unregisterDomain(domain);
    }


    /**
     * Method in transaction update servicegroupDomain status and registers participant to SML.
     * If registration fails  - transaction is rolled back
     *
     * @param participantId     - Participant schema
     * @param participantSchema - Participant schema
     * @param domainCode        - register to domain
     */

    @Transactional
    public void registerParticipant(String participantId, String participantSchema, String domainCode) {
        LOG.businessDebug(BUS_SML_REGISTER_SERVICE_GROUP, participantId, participantSchema, domainCode);
        if (!smlConnector.isSmlIntegrationEnabled()) {
            String msg = "SML integration is not enabled!";
            LOG.businessError(BUS_SML_REGISTER_SERVICE_GROUP_FAILED, participantId, participantSchema, domainCode, msg);
            throw new SMPRuntimeException(CONFIGURATION_ERROR, msg);
        }
        DBServiceGroupDomain serviceGroupDomain = getAndValidateServiceGroupDomain(participantId,
                participantSchema, domainCode, BUS_SML_REGISTER_SERVICE_GROUP_FAILED);

        ParticipantIdentifierType normalizedParticipantId = caseSensitivityNormalizer
                .normalizeParticipantIdentifier(participantSchema,
                        participantId);


        // register only not registered services
        if (!serviceGroupDomain.isSmlRegistered()) {
            // update value
            serviceGroupDomain.setSmlRegistered(true);
            serviceGroupDao.updateServiceGroupDomain(serviceGroupDomain);
            smlConnector.registerInDns(normalizedParticipantId, serviceGroupDomain.getDomain());
            LOG.businessDebug(BUS_SML_REGISTER_SERVICE_GROUP, participantId, participantSchema, domainCode);
        } else {
            LOG.businessWarn(BUS_SML_REGISTER_SERVICE_GROUP_ALREADY_REGISTERED, participantId, participantSchema, domainCode);
        }

    }

    /**
     * Method in transaction update servicegroupDomain status and unregisters participant to SML.
     * Method is meant for unregistering participants which are still in database. If they are delete
     * then this method should not be used.
     * <p>
     * If registration fails  - transaction is rolled back
     *
     * @param participantId     - Participant schema
     * @param participantSchema - Participant schema
     * @param domainCode        - register to domain
     */

    @Transactional
    public void unregisterParticipant(String participantId, String participantSchema, String domainCode) {
        LOG.businessDebug(BUS_SML_UNREGISTER_SERVICE_GROUP, participantId, participantSchema, domainCode);
        if (!smlConnector.isSmlIntegrationEnabled()) {
            String msg = "SML integration is not enabled!";
            LOG.businessError(BUS_SML_UNREGISTER_SERVICE_GROUP_FAILED, participantId, participantSchema, domainCode, msg);
            throw new SMPRuntimeException(CONFIGURATION_ERROR, msg);
        }


        DBServiceGroupDomain serviceGroupDomain = getAndValidateServiceGroupDomain(participantId, participantSchema, domainCode, BUS_SML_UNREGISTER_SERVICE_GROUP_FAILED);

        // unregister only  registered participants
        if (serviceGroupDomain.isSmlRegistered()) {
            // update value
            serviceGroupDomain.setSmlRegistered(false);
            serviceGroupDao.updateServiceGroupDomain(serviceGroupDomain);
            unregisterParticipantFromSML(participantId, participantSchema, serviceGroupDomain.getDomain());
            LOG.businessDebug(BUS_SML_UNREGISTER_SERVICE_GROUP, participantId, participantSchema, domainCode);
        } else {
            LOG.businessWarn(BUS_SML_UNREGISTER_SERVICE_GROUP_ALREADY_REGISTERED, participantId, participantSchema, domainCode);
        }
    }

    /**
     * Method unregisters participant from SML. It does not check if Participant is in database or of is unregistered
     *
     * @param participantId     - Participant schema
     * @param participantSchema - Participant schema
     * @param domain            - register to domain
     */

    public boolean unregisterParticipantFromSML(String participantId, String participantSchema, DBDomain domain) {
        LOG.businessDebug(BUS_SML_UNREGISTER_SERVICE_GROUP, participantId, participantSchema, domain.getDomainCode());

        ParticipantIdentifierType normalizedParticipantId = caseSensitivityNormalizer
                .normalizeParticipantIdentifier(participantSchema,
                        participantId);

        // unregister only registered participants
        return smlConnector.unregisterFromDns(normalizedParticipantId, domain);

    }

    /**
     * Method registers participant to SML. It does not check if Participant is in database or of is already registered
     *
     * @param participantId     - Participant schema
     * @param participantSchema - Participant schema
     * @param domain            - register to domain
     */

    public boolean registerParticipantToSML(String participantId, String participantSchema, DBDomain domain) {
        LOG.businessDebug(BUS_SML_UNREGISTER_SERVICE_GROUP, participantId, participantSchema, domain.getDomainCode());

        ParticipantIdentifierType normalizedParticipantId = caseSensitivityNormalizer
                .normalizeParticipantIdentifier(participantSchema,
                        participantId);

        // unregister only  registered participants
        return smlConnector.registerInDns(normalizedParticipantId, domain);

    }

    private DBServiceGroupDomain getAndValidateServiceGroupDomain(String participantId, String participantSchema, String domainCode, SMPMessageCode messageCode) {
        // retrieve participant (session must be on - lazy loading... )
        Optional<DBServiceGroup> optionalServiceGroup = serviceGroupDao.findServiceGroup(participantId, participantSchema);
        if (!optionalServiceGroup.isPresent()) {
            String msg = "Service group not exists anymore !";
            LOG.businessError(messageCode, participantId, participantId, domainCode, msg);
            throw new SMPRuntimeException(SG_NOT_EXISTS, participantId, participantSchema);
        }
        DBServiceGroup serviceGroup = optionalServiceGroup.get();
        Optional<DBServiceGroupDomain> optionalServiceGroupDomain = serviceGroup.getServiceGroupForDomain(domainCode);
        if (!optionalServiceGroupDomain.isPresent()) {
            String msg = "Service group is not registered for domain on this SMP - register participant on domain first!";
            LOG.businessError(messageCode, participantId, participantId, domainCode, msg);
            throw new SMPRuntimeException(SG_NOT_REGISTRED_FOR_DOMAIN, domainCode, participantId, participantSchema);
        }
        return optionalServiceGroupDomain.get();
    }

    public boolean isSmlIntegrationEnabled() {
        return smlConnector.isSmlIntegrationEnabled();
    }
}
