/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.authentication.AuthenticationException;
import org.bonitasoft.engine.authentication.GenericAuthenticationService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.SUserUpdateException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.session.SSessionException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Matthieu Chaffotte
 * @author Anthony Birembaut
 */
public class SecuredLoginServiceImpl implements LoginService {

    private final GenericAuthenticationService authenticationService;
    private final SessionService sessionService;
    private final IdentityService identityService;
    private final TechnicalLoggerService logger;
    private final TechnicalUser technicalUser;
    private final ProfileService profileService;

    public SecuredLoginServiceImpl(final GenericAuthenticationService authenticationService, final SessionService sessionService,
                                   final IdentityService identityService, TechnicalLoggerService tenantTechnicalLoggerService,
                                   TechnicalUser technicalUser, ProfileService profileService) {
        this.authenticationService = authenticationService;
        this.sessionService = sessionService;
        this.identityService = identityService;
        this.logger = tenantTechnicalLoggerService;
        this.technicalUser = technicalUser;
        this.profileService = profileService;
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

    private SSession createSession(Long tenantId, String userName, long id, boolean b) throws SLoginException {
        try {
            List<SProfile> profilesOfUser = profileService.getProfilesOfUser(id);
            List<String> profiles = profilesOfUser.stream().map(SProfile::getName).collect(Collectors.toList());
            return sessionService.createSession(tenantId, id, userName, b, profiles);
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
                && technicalUser.getPassword().equals(String.valueOf(credentials.get(AuthenticationConstants.BASIC_PASSWORD)));
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
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, message);
        }
    }
}
