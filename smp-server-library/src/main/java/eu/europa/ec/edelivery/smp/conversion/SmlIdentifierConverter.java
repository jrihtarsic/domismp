/*-
 * #START_LICENSE#
 * smp-webapp
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

package eu.europa.ec.edelivery.smp.conversion;

import ec.services.wsdl.bdmsl.data._1.ParticipantsType;
import ec.services.wsdl.bdmsl.data._1.SMPAdvancedServiceForParticipantType;
import eu.europa.ec.edelivery.smp.identifiers.Identifier;
import org.busdox.servicemetadata.locator._1.ServiceMetadataPublisherServiceForParticipantType;
import org.busdox.transport.identifiers._1.ParticipantIdentifierType;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by gutowpa on 18/12/2017.
 */
public class SmlIdentifierConverter {

    public static ServiceMetadataPublisherServiceForParticipantType toBusdoxParticipantId(Identifier participantId, String smpId) {
        validate(participantId, smpId);

        ParticipantIdentifierType parId = toParticipantIdentifierType(participantId);

        ServiceMetadataPublisherServiceForParticipantType busdoxIdentifier = new ServiceMetadataPublisherServiceForParticipantType();
        busdoxIdentifier.setServiceMetadataPublisherID(smpId);
        busdoxIdentifier.setParticipantIdentifier(parId);
        return busdoxIdentifier;
    }

    public static ParticipantsType toParticipantsType(Identifier participantId, String smpId) {
        validate(participantId, smpId);

        ParticipantIdentifierType parId = toParticipantIdentifierType(participantId);

        ParticipantsType participantsType = new ParticipantsType();
        participantsType.setParticipantIdentifier(parId);
        participantsType.setServiceMetadataPublisherID(smpId);
        return participantsType;
    }


    public static SMPAdvancedServiceForParticipantType toBDMSLAdvancedParticipantId(Identifier participantId, String smpId, String serviceMetadata) {
        validate(participantId, smpId);

        ServiceMetadataPublisherServiceForParticipantType busdoxIdentifier = toBusdoxParticipantId(participantId, smpId);

        SMPAdvancedServiceForParticipantType bdmslRequest = new SMPAdvancedServiceForParticipantType();
        bdmslRequest.setServiceName(serviceMetadata);
        bdmslRequest.setCreateParticipantIdentifier(busdoxIdentifier);

        return bdmslRequest;
    }

    protected static void validate(Identifier participantId, String smpId) {
        if (isBlank(smpId)) {
            throw new IllegalStateException("SMP ID is null or empty");
        }
        if (participantId == null || isBlank(participantId.getValue())) {
            throw new IllegalStateException("Participant Scheme or Id is null or empty");
        }
    }

    private static ParticipantIdentifierType toParticipantIdentifierType(Identifier participantId) {
        ParticipantIdentifierType parId = new ParticipantIdentifierType();
        parId.setScheme(participantId.getScheme());
        parId.setValue(participantId.getValue());
        return parId;
    }
}
