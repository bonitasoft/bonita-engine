/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.core.login;

import static org.bonitasoft.engine.authentication.AuthenticationConstants.BASIC_USERNAME;
import static org.bonitasoft.engine.identity.model.builder.impl.SUserUpdateBuilderImpl.updateBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.authentication.AuthenticationException;
import org.bonitasoft.engine.authentication.GenericAuthenticationService;
import org.bonitasoft.engine.authorization.PermissionsBuilder;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.SUserUpdateException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.session.SSessionException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Matthieu Chaffotte
 * @author Anthony Birembaut
 */
@Service
public class SecuredLoginServiceImpl implements LoginService {

    private static final Logger log = LoggerFactory.getLogger(SecuredLoginServiceImpl.class);
    private final GenericAuthenticationService authenticationService;
    private final SessionService sessionService;
    private final IdentityService identityService;
    private final TechnicalUser technicalUser;
    private final ProfileService profileService;
    private final PermissionsBuilder permissionsBuilder;

    public SecuredLoginServiceImpl(final GenericAuthenticationService authenticationService,
            final SessionService sessionService,
            final IdentityService identityService,
            TechnicalUser technicalUser, ProfileService profileService, PermissionsBuilder permissionsBuilder) {
        this.authenticationService = authenticationService;
        this.sessionService = sessionService;
        this.identityService = identityService;
        this.technicalUser = technicalUser;
        this.profileService = profileService;
        this.permissionsBuilder = permissionsBuilder;
    }

    @Override
    public SSession login(final Map<String, Serializable> credentials) throws SLoginException, SUserNotFoundException {
        debugLog("Logging in");
        checkNull(credentials);
        Long tenantId = extractTenant(credentials);
        if (isTechnicalUser(credentials)) {
            debugLog("Authenticated as technical user");
            return createSession(tenantId, extractUserName(credentials), -1L, true);
        }
        String userName = verifyCredentials(credentials);
        checkIsNotBlank(userName);
        debugLog("Authenticated as regular user");
        SUser user = getUser(userName);
        checkIsEnabled(user);
        SSession session = createSession(tenantId, userName, user.getId(), false);
        updateLastConnectionDate(user);
        return session;
    }

    private void updateLastConnectionDate(SUser user) throws SLoginException {
        try {
            identityService.updateUser(user, updateBuilder().updateLastConnection(System.currentTimeMillis()).done());
        } catch (SUserUpdateException e) {
            throw new SLoginException(e);
        }
    }

    private long extractTenant(Map<String, Serializable> credentials) {
        return NumberUtils.toLong(String.valueOf(credentials.get(AuthenticationConstants.BASIC_TENANT_ID)), -1);
    }

    private void checkNull(Map<String, Serializable> credentials) throws SLoginException {
        if (credentials == null) {
            throw new SLoginException("invalid credentials, map is null");
        }
    }

    private SSession createSession(Long tenantId, String userName, long id, boolean isTechnicalUser)
            throws SLoginException {
        try {
            List<SProfile> profilesOfUser = profileService.getProfilesOfUser(id);
            List<String> profiles = profilesOfUser.stream().map(SProfile::getName).collect(Collectors.toList());
            Set<String> permissions = permissionsBuilder.getPermissions(isTechnicalUser, profiles, userName);
            return sessionService.createSession(tenantId, id, userName, isTechnicalUser, profiles, permissions);
        } catch (SSessionException | SBonitaReadException e) {
            throw new SLoginException(e);
        }
    }

    private void checkIsEnabled(SUser user) throws SLoginException {
        if (!user.isEnabled()) {
            throw new SLoginException("Unable to login : the user is disable.");
        }
    }

    private void checkIsNotBlank(String userName) throws SLoginException {
        if (StringUtils.isBlank(userName)) {
            debugLog("Authentication failed");
            // now we are sure authentication Failed
            throw new SLoginException("User name or password is not valid!");
        }
    }

    private SUser getUser(String userName) throws SUserNotFoundException {
        try {
            return identityService.getUserByUserName(userName);
        } catch (SUserNotFoundException e) {
            debugLog("Unable to find user with username " + userName + " in database.");
            throw e;
        }
    }

    private String verifyCredentials(Map<String, Serializable> credentials) throws SLoginException {
        try {
            return authenticationService.checkUserCredentials(credentials);
        } catch (AuthenticationException e) {
            debugLog("Unable to authenticate user with username " + credentials.get(BASIC_USERNAME));
            throw new SLoginException(e);
        }
    }

    private boolean isTechnicalUser(Map<String, Serializable> credentials) {
        return technicalUser.getUserName().equals(extractUserName(credentials))
                && technicalUser.getPassword()
                        .equals(String.valueOf(credentials.get(AuthenticationConstants.BASIC_PASSWORD)));
    }

    private String extractUserName(Map<String, Serializable> credentials) {
        if (credentials.containsKey(BASIC_USERNAME) && credentials.get(BASIC_USERNAME) != null) {
            return String.valueOf(credentials.get(BASIC_USERNAME));
        }
        return null;
    }

    @Override
    public void logout(final long sessionId) throws SSessionNotFoundException {
        sessionService.deleteSession(sessionId);
    }

    @Override
    public boolean isValid(final long sessionId) {
        try {
            return sessionService.isValid(sessionId);
        } catch (final SSessionNotFoundException e) {
            return false;
        }
    }

    private void debugLog(String message) {
        log.debug(message);
    }
}
