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
package eu.europa.ec.edelivery.smp.utils;

import eu.europa.ec.edelivery.smp.config.enums.SMPPropertyEnum;
import eu.europa.ec.edelivery.smp.config.enums.SMPPropertyTypeEnum;
import eu.europa.ec.edelivery.smp.exceptions.ErrorCode;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.event.Level;
import org.springframework.scheduling.support.CronExpression;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;

public class PropertyUtils {


    private static final String MASKED_VALUE = "*******";
    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(PropertyUtils.class);
    private static final String REG_EXP_VALUE_SEPARATOR = "\\|";
    private static final String REG_EXP_MAP_SEPARATOR = ":";

    private PropertyUtils() {
        // private constructor
    }

    public static Object parseProperty(SMPPropertyEnum prop, String value, File rootFolder) {
        if (StringUtils.isBlank(value)) {
            // empty/ null value is invalid
            if (prop.isMandatory()) {
                throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Empty mandatory property: " + prop.getProperty());
            }
            return null;
        }
        if (!prop.getValuePattern().matcher(value).find()) {
            LOG.debug("Value [{}] for property [{}] does not match [{}]", value, prop.getProperty(), prop.getValuePattern().pattern());
            throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, prop.getErrorValueMessage());
        }

        SMPPropertyTypeEnum type = prop.getPropertyType();
        return parsePropertyType(type, value, rootFolder);
    }

    public static boolean isValidProperty(SMPPropertyEnum prop, String value, File confFolder) {
        if (StringUtils.isBlank(value)) {
            // empty/ null value is invalid
            return !prop.isMandatory();
        }

        if (!prop.getValuePattern().matcher(value).matches()) {
            LOG.debug("Value [{}] for property [{}] does not match [{}]", value, prop.getProperty(), prop.getValuePattern().pattern());
            throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, prop.getErrorValueMessage());
        }
        SMPPropertyTypeEnum type = prop.getPropertyType();
        return isValidPropertyType(type, value, confFolder);
    }

    public static boolean isValidPropertyType(SMPPropertyTypeEnum type, String value, File confFolder) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        try {
            parsePropertyType(type, value, confFolder);
            return true;
        } catch (SMPRuntimeException ex) {
            LOG.debug("Invalid property value [{}] for type [{}]. Error: [{}]", value, type, ExceptionUtils.getRootCauseMessage(ex));
            return false;
        }
    }

    public static Object parsePropertyType(SMPPropertyTypeEnum type, String value, File rootFolder) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        if (StringUtils.length(value) > 2000) {
            throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Invalid property value! Error: Value to long. Max. allowed size 2000 characters!");
        }

        switch (type) {
            case BOOLEAN:
                if (StringUtils.equalsAnyIgnoreCase(trim(value), "true", "false")) {
                    return Boolean.valueOf(value.trim());
                }
                throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Invalid boolean value: ["
                        + value + "]. Error: Only {true, false} are allowed!");
            case REGEXP:
                try {
                    return Pattern.compile(value);
                } catch (PatternSyntaxException ex) {
                    throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Invalid regular expression: ["
                            + value + "]. Error:" + ExceptionUtils.getRootCauseMessage(ex), ex);
                }
            case INTEGER:
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException ex) {
                    throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Invalid integer: ["
                            + value + "]. Error:" + ExceptionUtils.getRootCauseMessage(ex), ex);
                }
            case LIST_STRING: {
                return Arrays.asList(value.split(REG_EXP_VALUE_SEPARATOR));
            }
            case MAP_STRING: {
                if (!value.contains(":")) {
                    throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Invalid map: ["
                            + value + "]. Error: Map must have at least one key:value entry!");
                }
                return Arrays.asList(value.split(REG_EXP_VALUE_SEPARATOR)).stream().collect(Collectors.toMap(
                        val -> trim(substringBefore(val, REG_EXP_MAP_SEPARATOR)), val -> trim(substringAfter(val, REG_EXP_MAP_SEPARATOR))));
            }
            case PATH: {
                File file = new File(rootFolder, value);
                if (!file.exists() && !file.mkdirs()) {
                    throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Folder: ["
                            + value + "] does not exist, and can not be created!");
                }
                if (!file.isDirectory()) {
                    throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Path: [" + value + "] is not folder!");
                }
                return new File(value);
            }
            // nothing to validate
            case FILENAME:
                File file = new File(rootFolder, value);
                if (!file.exists()) {
                    LOG.warn("File: [{}] does not exist. Full path: [{}].", value, file.getAbsolutePath());
                }
                return file;
            case EMAIL:
                String trimVal = value.trim();
                if (EmailValidator.getInstance().isValid(trimVal)) {
                    return trimVal;
                } else {
                    throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Invalid email address: [" + value + "].");
                }
            case URL:
                try {
                    return new URL(value.trim());
                } catch (MalformedURLException ex) {
                    throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Invalid URL address:  ["
                            + value + "]. Error:" + ExceptionUtils.getRootCauseMessage(ex), ex);
                }
            case STRING:
                return value;
            case CRON_EXPRESSION:
                try {
                    return CronExpression.parse(value);
                } catch (IllegalArgumentException ex) {
                    throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "cron expression:  ["
                            + value + "]. Error:" + ExceptionUtils.getRootCauseMessage(ex), ex);
                }
        }
        return null;
    }


    /**
     * Return true for properties with sensitive data. For example the property value must not be logged
     * or returned via WS!
     *
     * @param property - value to validate if contains sensitive data
     * @return true if data is sensitive, else return false
     */
    public static boolean isSensitiveData(String property) {
        Optional<SMPPropertyEnum> propOpt = SMPPropertyEnum.getByProperty(trim(property));
        if (propOpt.isPresent()) {
            return propOpt.get().isEncrypted() || property.toLowerCase().contains(".password.decrypted");
        }
        LOG.debug("Database property [{}] is not recognized by the SMP. Basic mask rule applied for masking!", property);
        return StringUtils.contains(property.toLowerCase(), "passw");
    }

    /**
     * Method returns 'masked' value for sensitive property data
     *
     * @param property - property name
     * @param value   - property value
     * @return masked value for sensitive properties. Else it returns value!
     */
    public static String getMaskedData(String property, String value) {
        return isSensitiveData(property) ? getMaskedData(value) : value;
    }

    public static String getMaskedData(String value) {
        return isNotBlank(value) ? MASKED_VALUE : "Null/Empty/Blank";
    }

    public static  void printProperties(Properties properties, Level loggingLevel) {
        if (properties != null) {
            LOG.debug("------ Print properties ------");
            properties.entrySet().stream().forEach(e ->
                    printProperty((String) e.getKey(), (String) e.getValue(), loggingLevel)
            );
        }
    }

    public static void printProperty(String key, String value, Level loggingLevel) {
        String logValue = "\t[" + key + "] --> [" + getMaskedData(key, value) + "]";
        switch (loggingLevel) {
            case TRACE:
                LOG.trace(logValue);
                break;
            case DEBUG:
                LOG.debug(logValue);
                break;
            case INFO:
                LOG.info(logValue);
                break;
            case WARN:
                LOG.warn(logValue);
                break;
            case ERROR:
                LOG.error(logValue);
                break;
            default:
                LOG.debug(logValue);
        }
    }
}
