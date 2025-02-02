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
package eu.europa.ec.edelivery.smp.config.properties;

import eu.europa.ec.edelivery.smp.config.PropertyUpdateListener;
import eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.*;

import static eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum.*;

/**
 * Class update mail sender configuration on property update event
 *
 * @author Joze Rihtarsic
 * @since 4.2
 */
@Component
public class SMPMailPropertyUpdateListener implements PropertyUpdateListener {
    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(SMPMailPropertyUpdateListener.class);

    JavaMailSenderImpl mailSender;

    public SMPMailPropertyUpdateListener(Optional<JavaMailSenderImpl> mailSender) {
        this.mailSender = mailSender.isPresent()?mailSender.get():null;
    }

    @Override
    public void updateProperties(Map<SMPPropertyEnum, Object> properties) {
        if (mailSender == null) {
            LOG.warn("No mail sender bean: JavaMailSenderImpl configured!");
            return;
        }
        String host = (String) properties.get(MAIL_SERVER_HOST);
        Integer port = (Integer) properties.get(MAIL_SERVER_PORT);
        String protocol = (String) properties.get(MAIL_SERVER_PROTOCOL);
        String username = (String) properties.get(MAIL_SERVER_USERNAME);
        String password = (String) properties.get(MAIL_SERVER_PASSWORD);
        Map<String, String> mailPropObj = (Map<String, String>) properties.get(MAIL_SERVER_PROPERTIES);

        if (StringUtils.isNotBlank(host)) {
            LOG.debug("Update mail sender  property host: [{}]", host);
            mailSender.setHost(host);
        }
        if (port != null) {
            LOG.debug("Update mail sender  property port: [{}]", port);
            mailSender.setPort(port);
        }
        if (StringUtils.isNotBlank(protocol)) {
            LOG.debug("Update mail sender  property protocol: [{}]", protocol);
            mailSender.setProtocol(protocol);
        }

        if (StringUtils.isNotBlank(username)) {
            LOG.debug("Update mail sender property username: [{}]", username);
            mailSender.setUsername(username);
        }

        if (StringUtils.isNotBlank(password)) {
            LOG.debug("Update mail sender property credentials: ****]");
            mailSender.setPassword(password);
        }

        if (mailPropObj != null) {
            Properties mailProp = new Properties();
            mailProp.putAll(mailPropObj);
            LOG.debug("Update mail sender mail properties: [{}]", mailProp);
            mailSender.setJavaMailProperties(mailProp);
        }
    }

    @Override
    public List<SMPPropertyEnum> handledProperties() {
        return Arrays.asList(MAIL_SERVER_HOST,
                MAIL_SERVER_PORT,
                MAIL_SERVER_PROTOCOL,
                MAIL_SERVER_USERNAME,
                MAIL_SERVER_PASSWORD,
                MAIL_SERVER_PROPERTIES);
    }
}
