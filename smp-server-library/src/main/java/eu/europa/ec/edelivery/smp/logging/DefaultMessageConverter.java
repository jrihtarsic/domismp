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
package eu.europa.ec.edelivery.smp.logging;

import eu.europa.ec.edelivery.smp.logging.api.MessageCode;
import eu.europa.ec.edelivery.smp.logging.api.MessageConverter;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;

/**
 * @author Cosmin Baciu (DomibusLogger, Domibus 3.3.)
 * @since 4.1
 */
public class DefaultMessageConverter implements MessageConverter {

    private static final Logger LOG = SMPLoggerFactory.getLogger(DefaultMessageConverter.class);

    @Override
    public String getMessage(Marker marker, MessageCode messageCode, Object... args) {
        String message = null;
        try {
            message = MessageFormatter.arrayFormat(messageCode.getMessage(), args).getMessage();
        } catch (Exception throwable) {
            LOG.debug("Could not format the code [" + messageCode.getCode() + "]: message [" + messageCode.getMessage() + "] and arguments [" + Arrays.asList(args) + "]");
            message = messageCode.getMessage();
        }
        if (marker != null) {
            return "[" + marker + " - " + messageCode.getCode() + "] " + message;
        } else {
            return "[" + messageCode.getCode() + "] " + message;
        }
    }
}
