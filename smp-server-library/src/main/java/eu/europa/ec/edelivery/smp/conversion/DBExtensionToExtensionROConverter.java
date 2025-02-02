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
package eu.europa.ec.edelivery.smp.conversion;

import eu.europa.ec.edelivery.smp.data.model.ext.DBExtension;
import eu.europa.ec.edelivery.smp.data.ui.ExtensionRO;
import eu.europa.ec.edelivery.smp.data.ui.ResourceDefinitionRO;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;


/**
 *
 */
@Component
public class DBExtensionToExtensionROConverter implements Converter<DBExtension, ExtensionRO> {
    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(DBExtensionToExtensionROConverter.class);
    private final ConversionService conversionService;

    public DBExtensionToExtensionROConverter( @Lazy ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public ExtensionRO convert(DBExtension source) {

        ExtensionRO target = new ExtensionRO();
        try {
            BeanUtils.copyProperties(target, source);
            List<ResourceDefinitionRO> resourceDefinitionROList =  source.getResourceDefs().stream().map(resourceDef ->
                    conversionService.convert(resourceDef, ResourceDefinitionRO.class)
            ).collect(Collectors.toList());
            target.getResourceDefinitions().addAll(resourceDefinitionROList);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOG.error("Error occurred while converting DBExtension", e);
            return null;
        }
        return target;
    }

}
