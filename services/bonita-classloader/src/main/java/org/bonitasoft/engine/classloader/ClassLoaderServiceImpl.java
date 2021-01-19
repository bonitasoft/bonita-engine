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
package org.bonitasoft.engine.classloader;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import javax.transaction.Status;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.classloader.listeners.ClassReflectorClearer;
import org.bonitasoft.engine.classloader.listeners.JacksonCacheClearer;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.impl.PlatformDependencyService;
import org.bonitasoft.engine.dependency.impl.TenantDependencyService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.home.BonitaResource;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.springframework.stereotype.Component;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
@Slf4j
@Component("classLoaderService") //id  used by RefreshClassLoaderTask and @InjectedService
public class ClassLoaderServiceImpl implements ClassLoaderService {

    private final Object synchroLock = new Object();
    private final ThreadLocal<RefreshClassloaderSynchronization> currentRefreshTask = new ThreadLocal<>();
    private final ParentClassLoaderResolver parentClassLoaderResolver;

    private VirtualClassLoader virtualGlobalClassLoader = new VirtualClassLoader(ClassLoaderIdentifier.GLOBAL_TYPE,
            ClassLoaderIdentifier.GLOBAL_ID, VirtualClassLoader.class.getClassLoader());

    private final Map<ClassLoaderIdentifier, VirtualClassLoader> localClassLoaders = new HashMap<>();

    private final Set<ClassLoaderListener> globalListeners = new HashSet<>();

    private final Object mutex = new ClassLoaderServiceMutex();

    private boolean shuttingDown = false;

    private final EventService eventService;
    private PlatformDependencyService platformDependencyService;
    private Map<Long, TenantDependencyService> dependencyServicesByTenant = new HashMap<>();
    private SessionAccessor sessionAccessor;
    private UserTransactionService userTransactionService;
    private BroadcastService broadcastService;
    private ClassLoaderUpdater classLoaderUpdater;

    public ClassLoaderServiceImpl(final ParentClassLoaderResolver parentClassLoaderResolver,
            final EventService eventService, PlatformDependencyService platformDependencyService,
            SessionAccessor sessionAccessor,
            UserTransactionService userTransactionService, BroadcastService broadcastService,
            ClassLoaderUpdater classLoaderUpdater) {
        this.parentClassLoaderResolver = parentClassLoaderResolver;
        this.eventService = eventService;
        this.platformDependencyService = platformDependencyService;
        this.sessionAccessor = sessionAccessor;
        this.userTransactionService = userTransactionService;
        this.broadcastService = broadcastService;
        this.classLoaderUpdater = classLoaderUpdater;
        globalListeners.add(new ClassReflectorClearer());
        globalListeners.add(new JacksonCacheClearer());
    }

    @Override
    public void registerDependencyServiceOfTenant(Long tenantId, TenantDependencyService tenantDependencyService) {
        dependencyServicesByTenant.put(tenantId, tenantDependencyService);
    }

    private static final class ClassLoaderServiceMutex {

    }

    private ClassLoaderIdentifier getKey(final String type, final long id) {
        return new ClassLoaderIdentifier(type, id);
    }

    @Override
    public long getGlobalClassLoaderId() {
        return ClassLoaderIdentifier.GLOBAL_ID;
    }

    @Override
    public String getGlobalClassLoaderType() {
        return ClassLoaderIdentifier.GLOBAL_TYPE;
    }

    private VirtualClassLoader getVirtualGlobalClassLoader() {
        return virtualGlobalClassLoader;
    }

    @Override
    public ClassLoader getGlobalClassLoader() {
        return getVirtualGlobalClassLoader();
    }

    private void warnOnShuttingDown(final ClassLoaderIdentifier key) {
        if (shuttingDown) {
            log.warn("Using local classloader on after ClassLoaderService shuttingdown: " + key);
        }
    }

    @Override
    public VirtualClassLoader getLocalClassLoader(final String type, final long id) {
        NullCheckingUtil.checkArgsNotNull(id, type);
        final ClassLoaderIdentifier key = getKey(type, id);
        return getLocalClassLoader(key);
    }

    private VirtualClassLoader getLocalClassLoader(ClassLoaderIdentifier key) {
        warnOnShuttingDown(key);
        VirtualClassLoader virtualClassLoader = getVirtualClassLoaderWithoutInitializingIt(key);
        if (!virtualClassLoader.isInitialized()) {
            synchronized (mutex) {
                // double check synchronization
                if (!virtualClassLoader.isInitialized()) {
                    classLoaderUpdater.initializeClassLoader(this, virtualClassLoader, key);
                }
            }
        }
        return virtualClassLoader;
    }

    private VirtualClassLoader getVirtualClassLoaderWithoutInitializingIt(ClassLoaderIdentifier key) {
        if (!localClassLoaders.containsKey(key)) {
            synchronized (mutex) {
                // double check synchronization
                if (!localClassLoaders.containsKey(key)) {
                    createClassLoader(key);
                }
            }
        }
        return localClassLoaders.get(key);
    }

    private void createClassLoader(ClassLoaderIdentifier identifier) {
        log.debug("creating classloader with key {}", identifier);
        VirtualClassLoader parent = getParentClassLoader(identifier);
        final VirtualClassLoader virtualClassLoader = new VirtualClassLoader(identifier.getType(), identifier.getId(),
                parent);

        localClassLoaders.put(identifier, virtualClassLoader);
    }

    private VirtualClassLoader getParentClassLoader(ClassLoaderIdentifier identifier) {
        final ClassLoaderIdentifier parentIdentifier = parentClassLoaderResolver
                .getParentClassLoaderIdentifier(identifier);
        NullCheckingUtil.checkArgsNotNull(parentIdentifier);
        VirtualClassLoader parent;
        if (ClassLoaderIdentifier.GLOBAL.equals(parentIdentifier)) {
            parent = getVirtualGlobalClassLoader();
        } else {
            parent = getLocalClassLoader(parentIdentifier);
        }
        return parent;
    }

    @Override
    public void removeLocalClassLoader(final String type, final long id) throws SClassLoaderException {
        log.debug("Removing local classloader for type {} of id {}", type, id);
        NullCheckingUtil.checkArgsNotNull(id, type);

        // Remove the class loader
        final ClassLoaderIdentifier key = getKey(type, id);
        destroyLocalClassLoader(key);
    }

    private void destroyLocalClassLoader(final ClassLoaderIdentifier key) throws SClassLoaderException {
        log.debug("Destroying local classloader with key: {}", key);
        final VirtualClassLoader localClassLoader = localClassLoaders.get(key);
        if (localClassLoader != null) {
            if (localClassLoader.hasChildren()) {
                throw new SClassLoaderException("Unable to delete classloader " + key + " because it has children: "
                        + localClassLoader.getChildren());
            }
            localClassLoader.destroy();
            localClassLoaders.remove(key);
            for (ClassLoaderListener globalListener : globalListeners) {
                log.debug("Notify global classloader listener that classloader {} is destroyed: {}",
                        localClassLoader.getIdentifier(), globalListener);
                globalListener.onDestroy(localClassLoader);
            }
        }
    }

    private void refreshGlobalClassLoader(Stream<BonitaResource> resources) throws SClassLoaderException {
        log.info("Refreshing global classloader");
        final VirtualClassLoader virtualClassloader = (VirtualClassLoader) getGlobalClassLoader();
        try {
            refreshClassLoader(virtualClassloader, resources, getGlobalClassLoaderType(), getGlobalClassLoaderId(),
                    BonitaHomeServer.getInstance().getGlobalTemporaryFolder(),
                    ClassLoaderServiceImpl.class.getClassLoader());
        } catch (Exception e) {
            throw new SClassLoaderException(e);
        }
    }

    private void refreshLocalClassLoader(String type, long id, Stream<BonitaResource> resources)
            throws SClassLoaderException {
        final ClassLoaderIdentifier key = getKey(type, id);
        final VirtualClassLoader virtualClassloader = getVirtualClassLoaderWithoutInitializingIt(
                new ClassLoaderIdentifier(type, id));
        try {
            refreshClassLoader(virtualClassloader, resources, type, id, getLocalTemporaryFolder(type, id),
                    getParentClassLoader(key));
            final SEvent event = new SEvent("ClassLoaderRefreshed");
            event.setObject(key);
            eventService.fireEvent(event);
        } catch (Exception e) {
            throw new SClassLoaderException(e);
        }
    }

    URI getLocalTemporaryFolder(String type, long id) throws BonitaHomeNotSetException, IOException {
        return BonitaHomeServer.getInstance().getLocalTemporaryFolder(type, id);
    }

    private void refreshClassLoader(final VirtualClassLoader virtualClassloader, Stream<BonitaResource> resources,
            final String type, final long id,
            final URI temporaryFolder, final ClassLoader parent) {
        log.info("Refreshing class loader of type {} with id {}", type, id);

        final BonitaClassLoader classLoader = new BonitaClassLoader(resources, type, id, temporaryFolder, parent);
        log.debug("Replacing {} with {}", virtualClassloader.getClassLoader(), classLoader);
        virtualClassloader.replaceClassLoader(classLoader);
        for (ClassLoaderListener globalListener : new HashSet<>(globalListeners)) {
            log.debug("Notify global classloader listener that classloader {} is updated: {}",
                    virtualClassloader.getIdentifier(), globalListener);
            globalListener.onUpdate(virtualClassloader);
        }
    }

    @Override
    public void start() {
        log.debug("Starting classloader service, creating the platform classloader");
        shuttingDown = false;
        //we do not create or destroy the global classloader because it does not point to a bonita classloader
    }

    @Override
    public void stop() {
        log.debug("Stopping classloader service, destroying all classloaders");
        shuttingDown = true;
        destroyAllLocalClassLoaders();
    }

    private void destroyAllLocalClassLoaders() {
        log.debug("Destroying all classloaders");
        //remove elements only that don't have children
        //there is no loop in this so the algorithm finishes
        final Set<Map.Entry<ClassLoaderIdentifier, VirtualClassLoader>> entries = localClassLoaders.entrySet();
        while (!entries.isEmpty()) {
            final Iterator<Map.Entry<ClassLoaderIdentifier, VirtualClassLoader>> iterator = entries.iterator();
            while (iterator.hasNext()) {
                Map.Entry<ClassLoaderIdentifier, VirtualClassLoader> next = iterator.next();
                if (!next.getValue().hasChildren()) {
                    next.getValue().destroy();
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public boolean addListener(String type, long id, ClassLoaderListener classLoaderListener) {
        log.debug("Added listener {} on {} {}", classLoaderListener, type, id);
        final VirtualClassLoader localClassLoader = getVirtualClassLoaderWithoutInitializingIt(
                new ClassLoaderIdentifier(type, id));
        return localClassLoader.addListener(classLoaderListener);
    }

    @Override
    public boolean removeListener(String type, long id, ClassLoaderListener classLoaderListener) {
        log.debug("Removed listener {} on {} {}", classLoaderListener, type, id);
        VirtualClassLoader localClassLoader = getVirtualClassLoaderWithoutInitializingIt(
                new ClassLoaderIdentifier(type, id));
        return localClassLoader.removeListener(classLoaderListener);
    }

    @Override
    public boolean addListener(ClassLoaderListener classLoaderListener) {
        log.debug("Added global listener {}", classLoaderListener);
        return globalListeners.add(classLoaderListener);
    }

    @Override
    public boolean removeListener(ClassLoaderListener classLoaderListener) {
        log.debug("Removed global listener {}", classLoaderListener);
        return globalListeners.remove(classLoaderListener);
    }

    @Override
    public void refreshClassLoaderImmediately(final ScopeType type, final long id) throws SClassLoaderException {
        final Stream<BonitaResource> resources;
        resources = getDependencies(type, id);
        if (type == ScopeType.GLOBAL) {
            refreshGlobalClassLoader(resources);
        } else {
            refreshLocalClassLoader(type.name(), id, resources);
        }
    }

    Stream<BonitaResource> getDependencies(ScopeType type, long id) throws SClassLoaderException {
        Stream<BonitaResource> resources;
        try {
            if (ScopeType.GLOBAL == type) {
                resources = platformDependencyService.getDependenciesResources(type, id);
            } else {
                long tenantId = sessionAccessor.getTenantId();
                TenantDependencyService tenantDependencyService = dependencyServicesByTenant.get(tenantId);
                if (tenantDependencyService == null) {
                    log.warn("No dependency service is initialized on tenant {}. Initializing empty classloader",
                            tenantId);
                    return Stream.empty();
                }
                resources = tenantDependencyService.getDependenciesResources(type, id);
            }
        } catch (STenantIdNotSetException | SDependencyException e) {
            throw new SClassLoaderException(e);
        }
        return resources;
    }

    @Override
    public void refreshClassLoaderAfterUpdate(final ScopeType type, final long id) throws SClassLoaderException {
        try {
            registerRefreshOnAllNodes(type, id);
        } catch (STransactionNotFoundException | STenantIdNotSetException e) {
            throw new SClassLoaderException(e);
        }
    }

    @Override
    public void refreshClassLoaderOnOtherNodes(final ScopeType type, final long id) throws SClassLoaderException {
        try {
            userTransactionService
                    .registerBonitaSynchronization((BonitaTransactionSynchronization) transactionState -> {

                        if (transactionState != Status.STATUS_COMMITTED) {
                            return;
                        }
                        Map<String, TaskResult<Void>> execute;
                        try {
                            execute = broadcastService.executeOnOthersAndWait(new RefreshClassLoaderTask(id, type),
                                    getTenantId(type));
                        } catch (TimeoutException | STenantIdNotSetException | ExecutionException
                                | InterruptedException e) {
                            throw new BonitaRuntimeException(e);
                        }
                        for (Map.Entry<String, TaskResult<Void>> resultEntry : execute.entrySet()) {
                            if (resultEntry.getValue().isError()) {
                                throw new IllegalStateException(resultEntry.getValue().getThrowable());
                            }
                        }
                    });
        } catch (STransactionNotFoundException e) {
            throw new SClassLoaderException(e);
        }
    }

    private void registerRefreshOnAllNodes(ScopeType type, long id)
            throws STransactionNotFoundException, STenantIdNotSetException {
        synchronized (synchroLock) {
            RefreshClassloaderSynchronization refreshTaskSynchronization = currentRefreshTask.get();
            if (refreshTaskSynchronization == null) {
                RefreshClassLoaderTask callable = new RefreshClassLoaderTask(id, type);
                refreshTaskSynchronization = new RefreshClassloaderSynchronization(this, broadcastService, callable,
                        classLoaderUpdater, getTenantId(type), type, id);
                userTransactionService.registerBonitaSynchronization(refreshTaskSynchronization);
                currentRefreshTask.set(refreshTaskSynchronization);
            } else {
                refreshTaskSynchronization.addClassloaderToRefresh(type, id);
            }
        }
    }

    private Long getTenantId(ScopeType type) throws STenantIdNotSetException {
        Long tenantId = null;
        if (ScopeType.GLOBAL != type) {
            tenantId = sessionAccessor.getTenantId();
        }
        return tenantId;
    }

    @Override
    public void removeRefreshClassLoaderSynchronization() {
        currentRefreshTask.remove();
    }

}
