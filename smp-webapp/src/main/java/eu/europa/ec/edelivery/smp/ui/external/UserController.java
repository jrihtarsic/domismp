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
package eu.europa.ec.edelivery.smp.ui.external;

import eu.europa.ec.edelivery.smp.auth.SMPAuthenticationService;
import eu.europa.ec.edelivery.smp.auth.SMPAuthorizationService;
import eu.europa.ec.edelivery.smp.data.enums.ApplicationRoleType;
import eu.europa.ec.edelivery.smp.data.enums.CredentialTargetType;
import eu.europa.ec.edelivery.smp.data.enums.CredentialType;
import eu.europa.ec.edelivery.smp.data.model.user.DBUser;
import eu.europa.ec.edelivery.smp.data.ui.*;
import eu.europa.ec.edelivery.smp.filter.Filter;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.services.ui.UIAlertService;
import eu.europa.ec.edelivery.smp.services.ui.UIUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static eu.europa.ec.edelivery.smp.ui.ResourceConstants.*;
import static eu.europa.ec.edelivery.smp.utils.SessionSecurityUtils.decryptEntityId;

/**
 * @author Joze Rihtarsic
 * @since 4.1
 */
@RestController
@RequestMapping(path = CONTEXT_PATH_PUBLIC_USER)
public class UserController {

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(UserController.class);
    private final UIUserService uiUserService;
    private final UIAlertService uiAlertService;
    private final SMPAuthorizationService authorizationService;
    private final SMPAuthenticationService authenticationService;

    public UserController(UIUserService uiUserService, SMPAuthorizationService authorizationService, SMPAuthenticationService authenticationService, UIAlertService uiAlertService) {
        this.uiUserService = uiUserService;
        this.authorizationService = authorizationService;
        this.authenticationService = authenticationService;
        this.uiAlertService = uiAlertService;
    }

    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#userId)")
    @PutMapping(path = "/{user-id}/change-password", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public boolean changePassword(@PathVariable(PATH_PARAM_ENC_USER_ID) String userId, @RequestBody PasswordChangeRO newPassword, HttpServletRequest request, HttpServletResponse response) {
        Long entityId = decryptEntityId(userId);
        LOG.info("Validating the password of the currently logged in user:[{}] with id:[{}] ", userId, entityId);
        // when user changing password the current password must be verified even if cas authenticated
        DBUser result = uiUserService.updateUserPassword(entityId, entityId, newPassword.getCurrentPassword(), newPassword.getNewPassword());
        if (result != null) {
            LOG.info("Password successfully changed. Logout the user, to be able to login with the new password!");
            authenticationService.logout(request, response);
        }
        return result != null;
    }

    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#userId)")
    @GetMapping(path = "/{user-id}/search", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public List<SearchUserRO> lookupUsers(@PathVariable(PATH_PARAM_ENC_USER_ID) String userId,
                                          @RequestParam(value = PARAM_PAGINATION_FILTER, defaultValue = "", required = false) @Filter String filter) {
        Long entityId = decryptEntityId(userId);
        LOG.info("Validating the password of the currently logged in user:[{}] with id:[{}] ", userId, entityId);

        //  return first 10 results
        return uiUserService.searchUsers(0, 10, filter).getServiceEntities();
    }

    /**
     * Update the details of the currently logged-in user (e.g. update the role, the credentials or add certificate details).
     *
     * @param userId the identifier of the user being updated; it must match the currently logged-in user's identifier
     * @param user   the updated details
     * @throws org.springframework.security.access.AccessDeniedException when trying to update the details of another user, different than the one being currently logged in
     */
    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#userId)")
    @PutMapping(path = "/{user-id}")
    public UserRO updateCurrentUserProfile(@PathVariable(PATH_PARAM_ENC_USER_ID) String userId, @RequestBody UserRO user) {
        LOG.info("Update current user: {}", user);
        Long entityId = decryptEntityId(userId);
        // Update the user and mark the password as changed at this very instant of time
        uiUserService.updateUserProfile(entityId, user);
        // refresh user from DB
        UserRO userRO = uiUserService.getUserById(entityId);
        // return clean user to UI
        return authorizationService.getUpdatedUserData(userRO, userRO.getAuthorities());
    }

    /**
     * Update the details of the currently logged in user (e.g. update the role, the credentials or add certificate details).
     *
     * @param userId the identifier of the user being updated; it must match the currently logged in user's identifier
     * @throws org.springframework.security.access.AccessDeniedException if user is not logged in
     */
    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#userId)")
    @GetMapping(path = "/{user-id}/navigation-tree")
    public NavigationTreeNodeRO getUserNavigationTree(@PathVariable(PATH_PARAM_ENC_USER_ID) String userId) {
        LOG.info("get User Navigation tree for user ID: {}", userId);
        Long entityId = decryptEntityId(userId);
        DBUser user = uiUserService.findUser(entityId);
        NavigationTreeNodeRO home = new NavigationTreeNodeRO("home", "navigation.label.home", "home", "");
        home.addChild(createPublicNavigationTreeNode());
        // create administration nodes for domains, groups and resources
        NavigationTreeNodeRO adminNodes = createEditNavigationTreeNode();
        if (!adminNodes.getChildren().isEmpty()) {
            home.addChild(adminNodes);
        }
        if (user.getApplicationRole() == ApplicationRoleType.SYSTEM_ADMIN) {
            home.addChild(createSystemAdminNavigationTreeNode());
        }
        home.addChild(createUserProfileNavigationTreeNode());
        return home;
    }

    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#userId)")
    @GetMapping(path = "/{user-id}/username-credential-status")
    public CredentialRO getUsernameCredentialStatus(@PathVariable(PATH_PARAM_ENC_USER_ID) String userId) {
        LOG.debug("Get user credential status for user: [{}]", userId);
        Long entityId = decryptEntityId(userId);
        // Update the user and mark the password as changed at this very instant of time
        List<CredentialRO> credentialROList = uiUserService.getUserCredentials(entityId,
                CredentialType.USERNAME_PASSWORD, CredentialTargetType.UI);

        return credentialROList.isEmpty() ? null : credentialROList.get(0);
    }

    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#encUserId)")
    @GetMapping(path = "/{user-id}/access-token-credentials")
    public List<CredentialRO> getAccessTokenCredentials(@PathVariable(PATH_PARAM_ENC_USER_ID) String encUserId) {
        LOG.debug("Get access token credential status for user:: [{}]", encUserId);
        Long userId = decryptEntityId(encUserId);
        // Update the user and mark the password as changed at this very instant of time
        return uiUserService.getUserCredentials(userId,
                CredentialType.ACCESS_TOKEN, CredentialTargetType.REST_API);
    }

    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#encUserId)")
    @DeleteMapping(path = "/{user-id}/access-token-credential/{credential-id}")
    public CredentialRO deleteAccessTokenCredentials(@PathVariable(PATH_PARAM_ENC_USER_ID) String encUserId,
                                                     @PathVariable("credential-id") String encAccessTokenId) {
        LOG.debug("Delete User [{}] access token credential: [{}]", encUserId, encAccessTokenId);
        Long userId = decryptEntityId(encUserId);
        Long accessTokenId = decryptEntityId(encAccessTokenId);

        // delete user credential
        CredentialRO result = uiUserService.deleteUserCredentials(userId,
                accessTokenId, CredentialType.ACCESS_TOKEN, CredentialTargetType.REST_API);
        // set the same encrypted id so that UI can locate and update it
        result.setCredentialId(encAccessTokenId);
        return result;
    }

    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#encUserId)")
    @PostMapping(path = "/{user-id}/access-token-credential/{credential-id}")
    public CredentialRO updateAccessTokenCredentials(@PathVariable(PATH_PARAM_ENC_USER_ID) String encUserId,
                                                     @PathVariable("credential-id") String encAccessTokenId,
                                                     @RequestBody CredentialRO credentialRO) {
        LOG.debug("Update User [{}] access token credential: [{}]", encUserId, encAccessTokenId);
        Long userId = decryptEntityId(encUserId);
        Long accessTokenId = decryptEntityId(encAccessTokenId);

        // delete user credential
        CredentialRO result = uiUserService.updateUserCredentials(userId,
                accessTokenId,
                CredentialType.ACCESS_TOKEN,
                CredentialTargetType.REST_API,
                credentialRO);
        // set the same encrypted credential id so that UI can remove it from the list
        result.setCredentialId(encAccessTokenId);
        return result;
    }

    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#encUserId)")
    @PutMapping(path = "/{user-id}/access-token-credential/{credential-id}")
    public AccessTokenRO generateAccessTokenCredential(@PathVariable(PATH_PARAM_ENC_USER_ID) String encUserId,
                                                       @PathVariable("credential-id") String encAccessTokenId,
                                                       @RequestBody CredentialRO credentialRO) {
        LOG.debug("Generate User [{}] access token credential: [{}]", encUserId, encAccessTokenId);
        Long userId = decryptEntityId(encUserId);
        return uiUserService.createAccessTokenForUser(userId, credentialRO);
    }

    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#encUserId)")
    @GetMapping(path = "/{user-id}/certificate-credentials")
    public List<CredentialRO> getCertificateCredentials(@PathVariable(PATH_PARAM_ENC_USER_ID) String encUserId) {
        LOG.debug("get User credential status: [{}]", encUserId);
        Long userId = decryptEntityId(encUserId);
        // Update the user and mark the password as changed at this very instant of time
        return uiUserService.getUserCredentials(userId,
                CredentialType.CERTIFICATE, CredentialTargetType.REST_API);
    }

    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#encUserId)")
    @DeleteMapping(path = "/{user-id}/certificate-credential/{credential-id}")
    public CredentialRO deleteCertificateCredential(@PathVariable(PATH_PARAM_ENC_USER_ID) String encUserId,
                                                    @PathVariable("credential-id") String encCredentialId) {
        LOG.debug("Delete User [{}] access certificate credential: [{}]", encUserId, encCredentialId);
        Long userId = decryptEntityId(encUserId);
        Long credentialId = decryptEntityId(encCredentialId);
        // delete user credential
        CredentialRO result = uiUserService.deleteUserCredentials(userId,
                credentialId, CredentialType.CERTIFICATE, CredentialTargetType.REST_API);
        // set the same encrypted credential id so that UI can remove it from the list
        result.setCredentialId(encCredentialId);
        return result;
    }

    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#encUserId)")
    @PostMapping(path = "/{user-id}/certificate-credential/{credential-id}")
    public CredentialRO updateCertificateCredential(@PathVariable(PATH_PARAM_ENC_USER_ID) String encUserId,
                                                    @PathVariable("credential-id") String encCredentialId,
                                                    @RequestBody CredentialRO credentialRO) {
        LOG.debug("Update User [{}] certificate credential: [{}]", encUserId, encCredentialId);
        Long userId = decryptEntityId(encUserId);
        Long credentialId = decryptEntityId(encCredentialId);
        // delete user credential
        CredentialRO result = uiUserService.updateUserCredentials(userId,
                credentialId,
                CredentialType.CERTIFICATE,
                CredentialTargetType.REST_API,
                credentialRO);
        // set the same encrypted credential id so that UI can update it
        result.setCredentialId(encCredentialId);
        return result;
    }

    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#encUserId)")
    @GetMapping(path = "/{user-id}/certificate-credential/{credential-id}")
    public CredentialRO getCertificateCredential(@PathVariable(PATH_PARAM_ENC_USER_ID) String encUserId,
                                                 @PathVariable("credential-id") String encCredentialId) {
        LOG.debug("get User [{}] certificate credential: [{}]", encUserId, encCredentialId);
        Long userId = decryptEntityId(encUserId);
        Long credentialId = decryptEntityId(encCredentialId);
        return uiUserService.getUserCertificateCredential(userId, credentialId);
    }

    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#encUserId)")
    @PutMapping(path = "/{user-id}/certificate-credential/{credential-id}")
    public CredentialRO storeCertificateCredential(@PathVariable(PATH_PARAM_ENC_USER_ID) String encUserId,
                                                   @PathVariable("credential-id") String credentialId,
                                                   @RequestBody CredentialRO credentialRO) {
        LOG.debug("Store credential for user [{}] certificate credential: [{}]", encUserId, credentialId);
        Long userId = decryptEntityId(encUserId);
        return uiUserService.storeCertificateCredentialForUser(userId, credentialRO);
    }

    /**
     * Method returns Users list of alerts. To access the list user must be logged in.
     * <p>
     *
     * @param encUserId - encrypted user id (from session) - used for authorization check
     * @param page      - page number (0..n)
     * @param pageSize  - page size (0..n) - number of results on page/max number of returned results.
     * @param orderBy   - order by field
     * @param orderType - order type (asc, desc)
     * @return ServiceResult<AlertRO> - list of alerts
     */
    @PreAuthorize("@smpAuthorizationService.isCurrentlyLoggedIn(#encUserId)")
    @GetMapping(path = "/{user-id}/alert", produces = {MimeTypeUtils.APPLICATION_JSON_VALUE})
    public ServiceResult<AlertRO> getUserAlertList(
            @PathVariable("user-id") String encUserId,
            @RequestParam(value = PARAM_PAGINATION_PAGE, defaultValue = "0") int page,
            @RequestParam(value = PARAM_PAGINATION_PAGE_SIZE, defaultValue = "10") int pageSize,
            @RequestParam(value = PARAM_PAGINATION_ORDER_BY, defaultValue = "id", required = false) String orderBy,
            @RequestParam(value = PARAM_PAGINATION_ORDER_TYPE, defaultValue = "desc", required = false) String orderType
    ) {
        LOG.info("Search for page: {}, page size: {}", page, pageSize);
        UserRO loggedUserData = authorizationService.getLoggedUserData();
        // set filter to current user
        AlertRO filter = new AlertRO();
        filter.setUsername(loggedUserData.getUsername());
        // return the user alert list
        return uiAlertService.getTableList(page, pageSize, orderBy, orderType, filter);
    }


    protected NavigationTreeNodeRO createPublicNavigationTreeNode() {
            NavigationTreeNodeRO node = new NavigationTreeNodeRO("search-tools", "navigation.label.search", "search", "public");
        node.addChild(new NavigationTreeNodeRO("search-resources", "navigation.label.search.resources", "find_in_page", "search-resource", "navigation.tooltip.search.resources"));
        node.addChild(new NavigationTreeNodeRO("dns-tools", "navigation.label.search.dns.tools", "dns", "dns-tools" , "navigation.tooltip.search.dns.tools"));
        return node;
    }

    protected NavigationTreeNodeRO createUserProfileNavigationTreeNode() {
        NavigationTreeNodeRO node = new NavigationTreeNodeRO("user-data", "navigation.label.user.settings", "account_circle", "user-settings");
        node.addChild(new NavigationTreeNodeRO("user-data-profile", "navigation.label.user.settings.profile", "account_circle", "user-profile"));
        node.addChild(new NavigationTreeNodeRO("user-data-access-token", "navigation.label.user.settings.access.tokens", "key", "user-access-token"));
        node.addChild(new NavigationTreeNodeRO("user-data-certificates", "navigation.label.user.settings.certificates", "article", "user-certificate"));
        node.addChild(new NavigationTreeNodeRO("user-data-alert", "navigation.label.user.settings.alerts", "notifications", "user-alert"));
        return node;
    }

    protected NavigationTreeNodeRO createSystemAdminNavigationTreeNode() {
        NavigationTreeNodeRO node = new NavigationTreeNodeRO("system-settings", "navigation.label.system.settings", "admin_panel_settings", "system-settings");
        node.addChild(new NavigationTreeNodeRO("system-admin-user", "navigation.label.system.settings.users", "people", "user"));
        node.addChild(new NavigationTreeNodeRO("system-admin-domain", "navigation.label.system.settings.domains", "domain", "domain"));
        node.addChild(new NavigationTreeNodeRO("system-admin-keystore", "navigation.label.system.settings.keystores", "key", "keystore"));
        node.addChild(new NavigationTreeNodeRO("system-admin-truststore", "navigation.label.system.settings.truststores", "article", "truststore"));
        node.addChild(new NavigationTreeNodeRO("system-admin-extension", "navigation.label.system.settings.extensions", "extension", "extension"));
        node.addChild(new NavigationTreeNodeRO("system-admin-properties", "navigation.label.system.settings.properties", "settings", "properties"));
        // node.addChild(new NavigationTreeNodeRO("system-admin-authentication", "navigation.label.system.settings.authentication", "shield", "authentication"));
        node.addChild(new NavigationTreeNodeRO("system-admin-alert", "navigation.label.system.settings.alerts", "notifications", "alert"));
        return node;
    }

    protected NavigationTreeNodeRO createEditNavigationTreeNode() {
        NavigationTreeNodeRO node = new NavigationTreeNodeRO("edit", "navigation.label.edit", "tune", "edit");
        node.addChild(new NavigationTreeNodeRO("edit-domain", "navigation.label.edit.domains", "account_circle", "edit-domain"));
        node.addChild(new NavigationTreeNodeRO("edit-group", "navigation.label.edit.groups", "group", "edit-group"));
        node.addChild(new NavigationTreeNodeRO("edit-resource", "navigation.label.edit.resources", "article", "edit-resource"));
        node.addChild(new NavigationTreeNodeRO("review-tasks", "navigation.label.review.tasks", "task", "review-tasks"));
        return node;
    }
}
