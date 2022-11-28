package eu.europa.ec.edelivery.smp.config.properties;

import eu.europa.ec.edelivery.smp.config.PropertyUpdateListener;
import eu.europa.ec.edelivery.smp.conversion.IdentifierService;
import eu.europa.ec.edelivery.smp.data.ui.enums.SMPPropertyEnum;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.edelivery.smp.data.ui.enums.SMPPropertyEnum.*;

/**
 * Class update mail sender configuration on property update event
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Component
public class SMPIdentifierServicePropertyUpdateListener implements PropertyUpdateListener {
    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(SMPIdentifierServicePropertyUpdateListener.class);

    IdentifierService identifierService;

    public SMPIdentifierServicePropertyUpdateListener(@Lazy IdentifierService identifierService) {
        this.identifierService = identifierService;
    }

    @Override
    public void updateProperties(Map<SMPPropertyEnum, Object> properties) {
        if (identifierService == null) {
            LOG.warn("No IdentifierService found, Skip IdentifierService configuration!");
            return;
        }
        Boolean partcSchemeMandatory = (Boolean) properties.get(PARTC_SCH_MANDATORY);
        List<String> partcCaseSensitiveSchemes = (List<String>) properties.get(CS_PARTICIPANTS);
        List<String> doccCaseSensitiveSchemes = (List<String>) properties.get(CS_DOCUMENTS);
        identifierService.configureParticipantIdentifierFormatter(partcCaseSensitiveSchemes, partcSchemeMandatory);
        identifierService.configureDocumentIdentifierFormatter(doccCaseSensitiveSchemes);


    }

    @Override
    public List<SMPPropertyEnum> handledProperties() {
        return Arrays.asList(
                PARTC_SCH_SPLIT_REGEXP,
                PARTC_SCH_MANDATORY,
                CS_PARTICIPANTS,
                CS_DOCUMENTS
        );
    }
}
