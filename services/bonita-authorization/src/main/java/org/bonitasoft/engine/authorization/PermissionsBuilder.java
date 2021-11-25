/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.authorization;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.authorization.properties.CompoundPermissionsMapping;
import org.bonitasoft.engine.authorization.properties.CustomPermissionsMapping;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.session.model.SSession;
import org.springframework.stereotype.Component;

@Component
public class PermissionsBuilder {

    public static final String PROFILE_TYPE_AUTHORIZATION_PREFIX = "profile";

    public static final String USER_TYPE_AUTHORIZATION_PREFIX = "user";

    private final CustomPermissionsMapping customPermissionsMapping;
    private final CompoundPermissionsMapping compoundPermissionsMapping;
    private final ApplicationService applicationService;

    PermissionsBuilder(final ApplicationService applicationService,
            final CustomPermissionsMapping customPermissionsMapping,
            final CompoundPermissionsMapping compoundPermissionsMapping) {
        this.applicationService = applicationService;
        this.customPermissionsMapping = customPermissionsMapping;
        this.compoundPermissionsMapping = compoundPermissionsMapping;
    }

    public Set<String> getPermissions(SSession session) throws SBonitaReadException {
        Set<String> permissions;
        if (session.isTechnicalUser()) {
            permissions = Collections.emptySet();
        } else {
            permissions = new HashSet<>();
            permissions.addAll(getProfilesPermissions(session.getProfiles()));
            permissions.addAll(getCustomUserPermissions(session.getUserName()));
            permissions.add(getUserPermission(session.getUserName()));
        }
        return permissions;
    }

    protected Set<String> getProfilesPermissions(List<String> profiles) throws SBonitaReadException {
        Set<String> permissions = new HashSet<>();
        for (final String pageToken : getAllPagesForUser(profiles)) {
            permissions.addAll(getCompoundPermissions(pageToken));
        }
        for (String profile : profiles) {
            permissions.addAll(getCustomProfilePermissions(profile));
            permissions.add(getProfilePermission(profile));
        }
        return permissions;
    }

    private Set<String> getAllPagesForUser(List<String> profiles) throws SBonitaReadException {
        final Set<String> pageTokens = new HashSet<>();
        for (final String profile : profiles) {
            pageTokens.addAll(getPageTokensForApplicationsMappedToProfile(profile));
        }
        return pageTokens;
    }

    private String getProfilePermission(final String profile) {
        return PROFILE_TYPE_AUTHORIZATION_PREFIX + "|" + profile;
    }

    private String getUserPermission(final String username) {
        return USER_TYPE_AUTHORIZATION_PREFIX + "|" + username;
    }

    private List<String> getPageTokensForApplicationsMappedToProfile(final String profile) throws SBonitaReadException {
        return applicationService.getAllPagesForProfile(profile);
    }

    private Set<String> getCustomProfilePermissions(final String profile) {
        return getCustomPermissions(PROFILE_TYPE_AUTHORIZATION_PREFIX, profile);
    }

    protected Set<String> getCustomUserPermissions(String userName) {
        return getCustomPermissions(USER_TYPE_AUTHORIZATION_PREFIX, userName);
    }

    protected Set<String> getCustomPermissions(final String type, final String identifier) {
        final Set<String> profileSinglePermissions = new HashSet<>();
        final Set<String> customPermissionsForEntity = getCustomPermissionsRaw(type, identifier);
        for (final String customPermissionForEntity : customPermissionsForEntity) {
            final Set<String> simplePermissions = getCompoundPermissions(customPermissionForEntity);
            if (!simplePermissions.isEmpty()) {
                profileSinglePermissions.addAll(simplePermissions);
            } else {
                profileSinglePermissions.add(customPermissionForEntity);
            }
        }
        return profileSinglePermissions;
    }

    private Set<String> getCustomPermissionsRaw(final String type, final String identifier) {
        return customPermissionsMapping.getPropertyAsSet(type + "|" + identifier);
    }

    private Set<String> getCompoundPermissions(final String compoundName) {
        return compoundPermissionsMapping.getPropertyAsSet(compoundName);
    }

}
