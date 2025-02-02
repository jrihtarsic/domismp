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

package eu.europa.ec.edelivery.smp.controllers;

import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.logging.SMPMessageCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.helpers.IOUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;


@RestController
@RequestMapping("/")
@Order(HIGHEST_PRECEDENCE)
public class RootController {

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(RootController.class);

    /**
     * redirect if / to index.html.
     *
     * @param model
     * @return
     */
    @GetMapping(value = {"/", "web/index.html"})
    public ModelAndView redirectOldIndexPath(ModelMap model) {
        return new ModelAndView("redirect:/index.html", model);
    }

    @GetMapping(produces = {MediaType.TEXT_HTML_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/ico", "image/x-ico"
    },
            value = {"/index.html", "/favicon.png", "/favicon.ico"})
    public byte[] getStaticResources(HttpServletRequest httpReq, HttpServletResponse httpRes) throws IOException {
        String host = getRemoteHost(httpReq);
        LOG.businessInfo(SMPMessageCode.BUS_HTTP_GET_END_STATIC_CONTENT, host, httpReq.getPathInfo());
        String value = httpReq.getPathInfo();

        if (StringUtils.isBlank(value)) {
            httpRes.setContentType(MediaType.TEXT_HTML_VALUE);
            return IOUtils.readBytesFromStream(RootController.class.getResourceAsStream("/html/index.html"));
        }

        if (value.endsWith("favicon.png")) {
            httpRes.setContentType(MediaType.IMAGE_PNG_VALUE);
            return IOUtils.readBytesFromStream(RootController.class.getResourceAsStream("/html/favicon.png"));
        } else if (value.endsWith("favicon.ico")) {
            httpRes.setContentType("image/x-ico");
            return IOUtils.readBytesFromStream(RootController.class.getResourceAsStream("/html/favicon.ico"));
        }

        httpRes.setContentType(MediaType.TEXT_HTML_VALUE);
        return IOUtils.readBytesFromStream(RootController.class.getResourceAsStream("/html/index.html"));

    }

    /**
     * redirect angular pages to index.html
     * solve the 404 error on refresh
     *
     * @param model
     * @return
     */
    //@GetMapping(value={"/ui","/ui/edit","/ui/search","/ui/search","/ui/domain","/ui/user"})
    @GetMapping(value = {"/ui"})
    public ModelAndView redirectWithUsingRedirectPrefix(ModelMap model) {
        return new ModelAndView("redirect:/ui/index.html", model);
    }

    public String getRemoteHost(HttpServletRequest httpReq) {
        String host = httpReq.getHeader("X-Forwarded-For");
        return StringUtils.isBlank(host) ? httpReq.getRemoteHost() : host;
    }
}
