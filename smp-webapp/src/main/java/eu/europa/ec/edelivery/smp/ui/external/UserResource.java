package eu.europa.ec.edelivery.smp.ui.external;

import eu.europa.ec.edelivery.smp.auth.SMPAuthenticationToken;
import eu.europa.ec.edelivery.smp.auth.SMPAuthorizationService;
import eu.europa.ec.edelivery.smp.data.model.DBUser;
import eu.europa.ec.edelivery.smp.data.ui.AccessTokenRO;
import eu.europa.ec.edelivery.smp.data.ui.CertificateRO;
import eu.europa.ec.edelivery.smp.data.ui.PasswordChangeRO;
import eu.europa.ec.edelivery.smp.data.ui.UserRO;
import eu.europa.ec.edelivery.smp.error.exceptions.SMPResponseStatusException;
import eu.europa.ec.edelivery.smp.exceptions.ErrorBusinessCode;
import eu.europa.ec.edelivery.smp.exceptions.ErrorCode;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.services.ui.UITruststoreService;
import eu.europa.ec.edelivery.smp.services.ui.UIUserService;
import eu.europa.ec.edelivery.smp.utils.SessionSecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.util.Arrays;

import static eu.europa.ec.edelivery.smp.ui.ResourceConstants.CONTEXT_PATH_PUBLIC_USER;

/**
 * @author Joze Rihtarsic
 * @since 4.1
 */
@RestController
@RequestMapping(value = CONTEXT_PATH_PUBLIC_USER)
public class UserResource {

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(UserResource.class);

    @Autowired
    private UIUserService uiUserService;
    @Autowired
    protected SMPAuthorizationService authorizationService;


    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#userId)")
    @PostMapping(value = "/{user-id}/generate-access-token", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public AccessTokenRO generateAccessToken(@PathVariable("user-id") String userId, @RequestBody String password) {
        Long entityId = decryptEntityId(userId);
        LOG.info("Generated access token for user:[{}] with id:[{}] ", userId, entityId);

        return uiUserService.generateAccessTokenForUser(entityId, password);
    }

    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#userId)")
    @PutMapping(path = "/{user-id}/change-password", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public boolean changePassword(@PathVariable("user-id") String userId, @RequestBody PasswordChangeRO newPassword) {
        Long entityId = decryptEntityId(userId);
        LOG.info("Validating the password of the currently logged in user:[{}] with id:[{}] ", userId, entityId);
        return uiUserService.updateUserPassword(entityId, newPassword.getCurrentPassword(), newPassword.getNewPassword());
    }
    /**
     * Update the details of the currently logged in user (e.g. update the role, the credentials or add certificate details).
     *
     * @param userId   the identifier of the user being updated; it must match the currently logged in user's identifier
     * @param user the updated details
     * @throws org.springframework.security.access.AccessDeniedException when trying to update the details of another user, different than the one being currently logged in
     */
    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#userId)")
    @PutMapping(path = "/{user-id}")
    public UserRO updateCurrentUser(@PathVariable("user-id") String userId, @RequestBody UserRO user) {
        LOG.info("Update current user: {}", user);
        Long entityId = decryptEntityId(userId);
        // Update the user and mark the password as changed at this very instant of time
        uiUserService.updateUserdata(entityId, user);

        DBUser updatedUser = uiUserService.findUser(entityId);
        UserRO userRO = uiUserService.convertToRo(updatedUser);

        return authorizationService.sanitize(userRO);
    }

    public Long decryptEntityId(String userId){
        try{
            return SessionSecurityUtils.decryptEntityId(userId);
        } catch (RuntimeException runtimeException) {
            LOG.error("Error occurred while decryption entityId ["+userId+"]!", runtimeException );
            throw new SMPRuntimeException(ErrorCode.INVALID_REQUEST, "UserId", "Invalid userId!");
        }
    }
}
