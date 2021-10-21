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
package org.bonitasoft.engine.api.impl;

import static java.lang.String.format;
import static org.bonitasoft.engine.service.PermissionService.*;

import java.util.HashSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.PermissionAPI;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.service.PermissionService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;

/**
 * @author Baptiste Mesta
 */
@Slf4j
public class PermissionAPIImpl implements PermissionAPI {

    @Override
    public boolean checkAPICallWithScript(String className, APICallContext context, boolean reload)
            throws ExecutionException, NotFoundException {
        TenantServiceAccessor serviceAccessor = getTenantServiceAccessor();
        PermissionService permissionService = serviceAccessor.getPermissionService();
        try {
            return permissionService.checkAPICallWithScript(className, context, reload);
        } catch (SExecutionException e) {
            throw new ExecutionException(
                    "Unable to execute the security rule " + className + " for the api call " + context, e);
        } catch (ClassNotFoundException e) {
            throw new NotFoundException("Unable to execute the security rule " + className + " for the api call "
                    + context + " because the class " + className
                    + " is not found", e);
        }
    }

    @Override
    public boolean isAuthorized(APICallContext apiCallContext, boolean reload, Set<String> userPermissions,
            Set<String> resourceDynamicPermissions) {
        checkResourceAuthorizationsSyntax(resourceDynamicPermissions);
        TenantServiceAccessor serviceAccessor = getTenantServiceAccessor();
        PermissionService permissionService = serviceAccessor.getPermissionService();
        try {
            if (permissionService.checkDynamicPermissionsWithUsername(resourceDynamicPermissions)
                    || checkDynamicPermissionsWithProfiles(resourceDynamicPermissions, userPermissions)) {
                return true;
            }
        } catch (SessionIdNotSetException | SSessionNotFoundException e) {
            log.error("Cannot access current user session", e);
            return false;
        }
        final String resourceClassName = getResourceClassName(resourceDynamicPermissions);
        if (resourceClassName != null) {
            return checkDynamicPermissionsWithScript(apiCallContext, resourceClassName, reload);
        }
        return false;
    }

    protected boolean checkDynamicPermissionsWithScript(final APICallContext apiCallContext,
            final String resourceClassName, boolean reload) {
        try {
            final boolean authorized = checkAPICallWithScript(resourceClassName, apiCallContext, reload);
            if (!authorized) {
                if (log.isTraceEnabled()) {
                    log.trace(
                            "Unauthorized access to " + apiCallContext.getMethod() + " " + apiCallContext.getApiName()
                                    + "/" + apiCallContext.getResourceName()
                                    + (apiCallContext.getResourceId() != null ? "/" + apiCallContext.getResourceId()
                                            : "")
                                    + " attempted by "
                                    + /* FIXME */ "Toto" + " Permission script: " + resourceClassName);
                }
            }
            return authorized;
        } catch (final NotFoundException e) {
            if (log.isErrorEnabled()) {
                log.error("Unable to find the dynamic permissions script: " + resourceClassName, e);
            }
            return false;
        } catch (final ExecutionException e) {
            if (log.isErrorEnabled()) {
                log.error("Unable to execute the dynamic permissions script: " + resourceClassName, e);
            }
            return false;
        }
    }

    protected String getResourceClassName(final Set<String> resourcePermissions) {
        String className = null;
        for (final String resourcePermission : resourcePermissions) {
            if (resourcePermission.startsWith(SCRIPT_TYPE_AUTHORIZATION_PREFIX + "|")) {
                className = resourcePermission.substring((SCRIPT_TYPE_AUTHORIZATION_PREFIX + "|").length());
            }
        }
        return className;
    }

    protected void checkResourceAuthorizationsSyntax(final Set<String> resourceAuthorizations) {
        for (final String resourceAuthorization : resourceAuthorizations) {
            if (!resourceAuthorization.matches("(" + USER_TYPE_AUTHORIZATION_PREFIX + "|"
                    + PROFILE_TYPE_AUTHORIZATION_PREFIX + "|" + SCRIPT_TYPE_AUTHORIZATION_PREFIX + ")\\|.+")) {
                if (log.isWarnEnabled()) {
                    log.warn("Error while getting dynamic authorizations. Unknown syntax: " + resourceAuthorization
                            + " defined in dynamic-permissions-checks.properties");
                }
                // TODO: should we really change the default behaviour?
                throw new RuntimeException(format("Dynamic permission rule %s is not valid", resourceAuthorization));
            }
        }
    }

    protected boolean checkDynamicPermissionsWithProfiles(final Set<String> resourceAuthorizations,
            final Set<String> userPermissions) {
        final Set<String> profileAuthorizations = getResourceProfileAuthorizations(resourceAuthorizations);
        for (final String profileAuthorization : profileAuthorizations) {
            if (userPermissions.contains(profileAuthorization)) {
                return true;
            }
        }
        return false;
    }

    protected Set<String> getResourceProfileAuthorizations(final Set<String> resourcePermissions) {
        final Set<String> profileAuthorizations = new HashSet<>();
        for (final String authorizedItem : resourcePermissions) {
            if (authorizedItem.startsWith(PROFILE_TYPE_AUTHORIZATION_PREFIX + "|")) {
                profileAuthorizations.add(authorizedItem);
            }
        }
        return profileAuthorizations;
    }

    TenantServiceAccessor getTenantServiceAccessor() {
        return TenantServiceSingleton.getInstance();
    }
}
