package eu.europa.ec.edelivery.smp.conversion;

import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registrar allowing registration of custom {@link org.springframework.core.convert.converter.Converter converters} for
 * the back-end domain model and autowiring them with the {@link org.springframework.core.convert.ConversionService} and
 * possibly other Spring beans (by first autowiring them into this registrar).
 *
 * @author Sebastian-Ion TINCU
 */
@Component
public class ConvertersRegistrar {

    private final SMPLogger logger = SMPLoggerFactory.getLogger(ConvertersRegistrar.class);

    @Autowired
    private ConfigurableConversionService conversionRegistry;

    @Autowired
    public void registerCustomConverters(List<Converter<?,?>> converters) {
        for (Converter<?, ?> converter : converters) {
            conversionRegistry.addConverter(converter);
        }

        logger.info("Finished registering custom converters: {}", converters);
    }
}
