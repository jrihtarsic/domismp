/*
 * Copyright 2017 European Commission | CEF eDelivery
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 * or file: LICENCE-EUPL-v1.1.pdf
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package eu.europa.ec.edelivery.smp.config;

import eu.europa.ec.cipa.smp.server.util.SignatureFilter;
import eu.europa.ec.edelivery.smp.error.ErrorMappingControllerAdvice;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.*;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * Created by gutowpa on 11/07/2017.
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {
        "eu.europa.ec.edelivery.smp.controllers",
        "eu.europa.ec.edelivery.smp.validation"})
@Import({GlobalMethodSecurityConfig.class, ErrorMappingControllerAdvice.class})
public class SmpWebAppConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.setOrder(HIGHEST_PRECEDENCE)
                .addResourceHandler("/index.html", "/favicon-16x16.png")
                .addResourceLocations("/web/");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        //Default value (true) would break @PathVariable Identifiers containing dot character "."
        configurer.setUseSuffixPatternMatch(false);
    }

}
