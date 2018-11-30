package eu.europa.ec.edelivery.smp.ui;


import eu.europa.ec.edelivery.smp.auth.SMPAuthenticationService;
import eu.europa.ec.edelivery.smp.auth.SMPAuthenticationToken;
import eu.europa.ec.edelivery.smp.auth.SMPAuthority;
import eu.europa.ec.edelivery.smp.auth.SMPAuthorizationService;
import eu.europa.ec.edelivery.smp.data.ui.ErrorRO;
import eu.europa.ec.edelivery.smp.data.ui.LoginRO;
import eu.europa.ec.edelivery.smp.data.ui.UserRO;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Sebastian-Ion TINCU
 * @since 4.0
 */
@RestController
@RequestMapping(value = "/ui/rest/security")
public class AuthenticationResource {

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(AuthenticationResource.class);

    @Autowired
    protected SMPAuthenticationService authenticationService;

    @Autowired
    protected SMPAuthorizationService authorizationService;

    @Autowired
    private ConversionService conversionService;

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ExceptionHandler({AuthenticationException.class})
    public ErrorRO handleException(Exception ex) {
        LOG.error(ex.getMessage(), ex);
        return new ErrorRO(ex.getMessage());
    }

    @RequestMapping(value = "authentication", method = RequestMethod.POST)
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public UserRO authenticate(@RequestBody LoginRO loginRO, HttpServletResponse response) {
        LOG.debug("Authenticating user [{}]", loginRO.getUsername());
        SMPAuthenticationToken authentication = (SMPAuthenticationToken) authenticationService.authenticate(loginRO.getUsername(), loginRO.getPassword());
        UserRO userRO = conversionService.convert(authentication.getUser(), UserRO.class);
        return authorizationService.sanitize(userRO);
    }

    @RequestMapping(value = "authentication", method = RequestMethod.DELETE)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            LOG.debug("Cannot perform logout: no user is authenticated");
            return;
        }

        LOG.info("Logging out user [{}]", auth.getName());
        new CookieClearingLogoutHandler("JSESSIONID", "XSRF-TOKEN").logout(request, response, null);
        LOG.info("Cleared cookies");
        new SecurityContextLogoutHandler().logout(request, response, auth);
        LOG.info("Logged out");
    }

    @RequestMapping(value = "user", method = RequestMethod.GET)
    @Secured({SMPAuthority.S_AUTHORITY_TOKEN_SYSTEM_ADMIN, SMPAuthority.S_AUTHORITY_TOKEN_SMP_ADMIN, SMPAuthority.S_AUTHORITY_TOKEN_SERVICE_GROUP_ADMIN})
    public UserRO getUser() {
        UserRO user = new UserRO();

        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LOG.debug("get user: {}", username);

        user.setUsername(username);
        return user;
    }

}