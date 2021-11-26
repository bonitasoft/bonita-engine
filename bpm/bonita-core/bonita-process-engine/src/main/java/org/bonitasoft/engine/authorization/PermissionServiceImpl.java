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
package org.bonitasoft.engine.authorization;

import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.impl.APIAccessorImpl;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.api.permission.PermissionRule;
import org.bonitasoft.engine.authorization.properties.CompoundPermissionsMapping;
import org.bonitasoft.engine.authorization.properties.CustomPermissionsMapping;
import org.bonitasoft.engine.authorization.properties.PropertiesWithSet;
import org.bonitasoft.engine.authorization.properties.ResourcesPermissionsMapping;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.page.ContentType;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.impl.ServerLoggerWrapper;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

/**
 * Permission service implementation
 *
 * @author Baptiste Mesta
 */
@Component
@Slf4j
@ConditionalOnSingleCandidate(PermissionService.class)
public class PermissionServiceImpl implements PermissionService {

    public static final String RESOURCES_PROPERTY = "resources";
    public static final String PROPERTY_CONTENT_TYPE = "contentType";
    public static final String PROPERTY_API_EXTENSIONS = "apiExtensions";
    public static final String PROPERTY_METHOD_MASK = "%s.method";
    public static final String PROPERTY_PATH_TEMPLATE_MASK = "%s.pathTemplate";
    public static final String PROPERTY_PERMISSIONS_MASK = "%s.permissions";
    public static final String RESOURCE_PERMISSION_KEY_MASK = "%s|extension/%s";
    public static final String RESOURCE_PERMISSION_VALUE = "[%s]";
    public static final String EXTENSION_SEPARATOR = ",";

    private final ClassLoaderService classLoaderService;
    private final TechnicalLoggerService logger;
    private final SessionAccessor sessionAccessor;
    private final SessionService sessionService;
    private GroovyClassLoader groovyClassLoader;
    private final CompoundPermissionsMapping compoundPermissionsMapping;
    private final ResourcesPermissionsMapping resourcesPermissionsMapping;
    private final CustomPermissionsMapping customPermissionsMapping;

    protected final long tenantId;

    public PermissionServiceImpl(final ClassLoaderService classLoaderService, final TechnicalLoggerService logger,
            final SessionAccessor sessionAccessor, final SessionService sessionService,
            @Value("${tenantId}") final long tenantId,
            CompoundPermissionsMapping compoundPermissionsMapping,
            ResourcesPermissionsMapping resourcesPermissionsMapping,
            CustomPermissionsMapping customPermissionsMapping) {
        this.classLoaderService = classLoaderService;
        this.logger = logger;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.tenantId = tenantId;
        this.compoundPermissionsMapping = compoundPermissionsMapping;
        this.resourcesPermissionsMapping = resourcesPermissionsMapping;
        this.customPermissionsMapping = customPermissionsMapping;
    }

    @Override
    public boolean checkAPICallWithScript(final String className, final APICallContext context, final boolean reload)
            throws SExecutionException, ClassNotFoundException {
        checkStarted();
        //groovy class loader load class from files and cache then when loaded, no need to do some lazy loading or load all class on start
        Class<?> aClass;
        if (reload) {
            reload();
            aClass = groovyClassLoader.loadClass(className, true, true, true);
        } else {
            aClass = Class.forName(className, true, groovyClassLoader);
        }
        if (!PermissionRule.class.isAssignableFrom(aClass)) {
            throw new SExecutionException("The class " + aClass.getName()
                    + " does not implements org.bonitasoft.engine.api.permission.PermissionRule");
        }
        SSession session = getSession();
        try {
            final APISession apiSession = ModelConvertor.toAPISession(session, null);
            final PermissionRule permissionRule = (PermissionRule) aClass.getDeclaredConstructor().newInstance();
            return permissionRule.isAllowed(apiSession, context, createAPIAccessorImpl(),
                    new ServerLoggerWrapper(permissionRule.getClass(), logger));
        } catch (final Throwable e) {
            throw new SExecutionException("The permission rule " + aClass.getName() + " threw an exception", e);
        }
    }

    public SSession getSession() throws SExecutionException {
        try {
            return sessionService.getSession(sessionAccessor.getSessionId());
        } catch (SSessionNotFoundException | SessionIdNotSetException e) {
            throw new SExecutionException("The session is not set.", e);
        }
    }

    private void reload() throws SExecutionException {
        stop();
        try {
            start();
        } catch (SBonitaException e) {
            throw new SExecutionException("The permission rule service could not be reloaded", e);
        }
    }

    protected APIAccessorImpl createAPIAccessorImpl() {
        return new APIAccessorImpl();
    }

    private void checkStarted() throws SExecutionException {
        if (groovyClassLoader == null) {
            throw new SExecutionException("The permission rule service is not started");
        }
    }

    @Override
    public void start() throws SBonitaException {
        groovyClassLoader = new GroovyClassLoader(
                classLoaderService.getClassLoader(identifier(ScopeType.TENANT, tenantId)));
        groovyClassLoader.setShouldRecompile(true);
        try {
            final File folder = getBonitaHomeServer().getSecurityScriptsFolder(tenantId);
            groovyClassLoader.addClasspath(folder.getAbsolutePath());
        } catch (BonitaHomeNotSetException | IOException e) {
            throw new SExecutionException(e);
        }
    }

    BonitaHomeServer getBonitaHomeServer() {
        return BonitaHomeServer.getInstance();
    }

    @Override
    public void stop() {
        if (groovyClassLoader != null) {
            groovyClassLoader.clearCache();
            groovyClassLoader = null;
        }
    }

    @Override
    public void pause() {
        stop();
    }

    @Override
    public void resume() throws SBonitaException {
        start();
    }

    @Override
    public boolean isAuthorized(APICallContext apiCallContext, boolean reload) throws SExecutionException {
        if (log.isDebugEnabled()) {
            log.debug("Static REST API permissions check");
        }
        final Set<String> resourcePermissions = getDeclaredPermissions(apiCallContext.getApiName(),
                apiCallContext.getResourceName(), apiCallContext.getMethod(), apiCallContext.getResourceId(),
                resourcesPermissionsMapping);
        final Set<String> userPermissions = getSession().getUserPermissions();
        for (final String resourcePermission : resourcePermissions) {
            if (userPermissions.contains(resourcePermission)) {
                return true;
            }
        }
        logger.log(this.getClass(), TechnicalLogSeverity.DEBUG,
                "Unauthorized access to " + apiCallContext.getMethod() + " " + apiCallContext.getApiName() + "/"
                        + apiCallContext.getResourceName()
                        + (apiCallContext.getResourceId() != null ? "/" + apiCallContext.getResourceId() : "")
                        + " attempted by " + getSession().getUserName()
                        + ", required permissions: " + resourcePermissions);

        return false;
    }

    protected Set<String> getDeclaredPermissions(final String apiName, final String resourceName, final String method,
            final String resourceQualifiers, final ResourcesPermissionsMapping resourcesPermissionsMapping) {
        List<String> resourceQualifiersIds = null;
        if (resourceQualifiers != null) {
            resourceQualifiersIds = Arrays
                    .asList(resourceQualifiers.split(ResourcesPermissionsMapping.RESOURCE_IDS_SEPARATOR));
        }
        Set<String> resourcePermissions = resourcesPermissionsMapping.getResourcePermissions(method, apiName,
                resourceName, resourceQualifiersIds);
        if (resourcePermissions.isEmpty()) {
            resourcePermissions = resourcesPermissionsMapping.getResourcePermissionsWithWildCard(method, apiName,
                    resourceName, resourceQualifiersIds);
        }
        if (resourcePermissions.isEmpty()) {
            resourcePermissions = resourcesPermissionsMapping.getResourcePermissions(method, apiName, resourceName);
        }
        return resourcePermissions;
    }

    @Override
    public void addPermissions(final String pageName, final Properties pageProperties) {
        Set<String> customPagePermissions = getCustomPagePermissions(
                pageProperties.getProperty(RESOURCES_PROPERTY),
                resourcesPermissionsMapping);
        addRestApiExtensionPermissions(resourcesPermissionsMapping, pageProperties);
        addPagePermissions(pageName, pageProperties, customPagePermissions);
    }

    private void addPagePermissions(String pageName, Properties pageProperties, Set<String> customPagePermissions) {
        if (ContentType.PAGE.equals(pageProperties.getProperty(PROPERTY_CONTENT_TYPE))) {
            compoundPermissionsMapping.setInternalPropertyAsSet(pageName, customPagePermissions);
        }
    }

    @Override
    public void removePermissions(Properties pageProperties) {
        for (String key : getApiExtensionResourcesPermissionsMapping(pageProperties).keySet()) {
            resourcesPermissionsMapping.removeInternalProperty(key);
        }
        compoundPermissionsMapping.removeInternalProperty(pageProperties.getProperty(PageService.PROPERTIES_NAME));
    }

    public Set<String> getCustomPagePermissions(final String declaredPageResources,
            final ResourcesPermissionsMapping resourcesPermissionsMapping) {
        final Set<String> pageRestResources = PropertiesWithSet.stringToSet(declaredPageResources);
        final Set<String> permissions = new HashSet<>();
        for (final String pageRestResource : pageRestResources) {
            final Set<String> resourcePermissions = resourcesPermissionsMapping.getPropertyAsSet(pageRestResource);
            if (resourcePermissions.isEmpty()) {
                if (log.isWarnEnabled()) {
                    log.warn("Error while getting resources permissions. Unknown resource: " + pageRestResource
                            + " defined in page.properties");
                }
            }
            permissions.addAll(resourcePermissions);
        }
        return permissions;
    }

    void addRestApiExtensionPermissions(final ResourcesPermissionsMapping resourcesPermissionsMapping,
            final Properties pageProperties) {
        final Map<String, String> permissionsMapping = getApiExtensionResourcesPermissionsMapping(
                pageProperties);
        for (final String key : permissionsMapping.keySet()) {
            resourcesPermissionsMapping.setInternalProperty(key, permissionsMapping.get(key));
        }
    }

    private Map<String, String> getApiExtensionResourcesPermissionsMapping(Properties pageProperties) {
        final Properties propertiesWithSet = new PropertiesWithSet(pageProperties);
        final Map<String, String> permissionsMap = new HashMap<>();
        if (ContentType.API_EXTENSION.equals(propertiesWithSet.getProperty(PROPERTY_CONTENT_TYPE))) {
            final String apiExtensionList = propertiesWithSet.getProperty(PROPERTY_API_EXTENSIONS);
            final String[] apiExtensions = apiExtensionList.split(EXTENSION_SEPARATOR);
            for (final String apiExtension : apiExtensions) {
                final String method = propertiesWithSet
                        .getProperty(String.format(PROPERTY_METHOD_MASK, apiExtension.trim()));
                final String pathTemplate = propertiesWithSet
                        .getProperty(String.format(PROPERTY_PATH_TEMPLATE_MASK, apiExtension.trim()));
                final String permissions = propertiesWithSet
                        .getProperty(String.format(PROPERTY_PERMISSIONS_MASK, apiExtension.trim()));
                permissionsMap.put(String.format(RESOURCE_PERMISSION_KEY_MASK, method, pathTemplate),
                        String.format(RESOURCE_PERMISSION_VALUE, permissions));
            }
        }
        return permissionsMap;
    }

    @Override
    public Set<String> getResourcePermissions(final String resourceKey) {
        return resourcesPermissionsMapping.getPropertyAsSet(resourceKey);
    }

    public void addCustomEntityPermissions(final String entity, final Set<String> resourcePermissions) {
        customPermissionsMapping.setPropertyAsSet(entity, resourcePermissions);
    }

    public void removeCustomEntityPermissions(String entity) {
        customPermissionsMapping.removeProperty(entity);
    }

}
