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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Status;

import lombok.extern.slf4j.Slf4j;
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

    private final Map<ClassLoaderIdentifier, VirtualClassLoader> classLoaders = new HashMap<>();

    private final Set<PlatformClassLoaderListener> platformClassLoaderListeners = new HashSet<>();
    private final Map<ClassLoaderIdentifier, Set<SingleClassLoaderListener>> singleClassLoaderListenersMap = Collections
            .synchronizedMap(new HashMap<>());

    private final Object mutex = new ClassLoaderServiceMutex();

    private boolean shuttingDown = false;

    private final EventService eventService;
    private final PlatformDependencyService platformDependencyService;
    private final Map<Long, TenantDependencyService> dependencyServicesByTenant = new HashMap<>();
    private final SessionAccessor sessionAccessor;
    private final UserTransactionService userTransactionService;
    private final BroadcastService broadcastService;
    private final ClassLoaderUpdater classLoaderUpdater;

    public ClassLoaderServiceImpl(final ParentClassLoaderResolver parentClassLoaderResolver,
            final EventService eventService, PlatformDependencyService platformDependencyService,
            SessionAccessor sessionAccessor,
            UserTransactionService userTransactionService, BroadcastService broadcastService,
            ClassLoaderUpdater classLoaderUpdater, List<PlatformClassLoaderListener> platformClassLoaderListeners) {
        this.parentClassLoaderResolver = parentClassLoaderResolver;
        this.eventService = eventService;
        this.platformDependencyService = platformDependencyService;
        this.sessionAccessor = sessionAccessor;
        this.userTransactionService = userTransactionService;
        this.broadcastService = broadcastService;
        this.classLoaderUpdater = classLoaderUpdater;
        this.platformClassLoaderListeners.addAll(platformClassLoaderListeners);
    }

    @Override
    public void registerDependencyServiceOfTenant(Long tenantId, TenantDependencyService tenantDependencyService) {
        dependencyServicesByTenant.put(tenantId, tenantDependencyService);
    }

    private static final class ClassLoaderServiceMutex {

    }

    @Override
    public ClassLoader getGlobalClassLoader() {
        return getLocalClassLoader(ClassLoaderIdentifier.GLOBAL);
    }

    private void warnOnShuttingDown(final ClassLoaderIdentifier key) {
        if (shuttingDown) {
            log.warn("Using local classloader on after ClassLoaderService shuttingdown: " + key);
        }
    }

    @Override
    public VirtualClassLoader getLocalClassLoader(ClassLoaderIdentifier identifier) {
        NullCheckingUtil.checkArgsNotNull(identifier);
        warnOnShuttingDown(identifier);
        VirtualClassLoader virtualClassLoader = getVirtualClassLoaderWithoutInitializingIt(identifier);
        if (!virtualClassLoader.isInitialized()) {
            synchronized (mutex) {
                // double check synchronization
                if (!virtualClassLoader.isInitialized()) {
                    classLoaderUpdater.initializeClassLoader(this, virtualClassLoader, identifier);
                }
            }
        }
        return virtualClassLoader;
    }

    private VirtualClassLoader getVirtualClassLoaderWithoutInitializingIt(ClassLoaderIdentifier identifier) {
        if (!classLoaders.containsKey(identifier)) {
            synchronized (mutex) {
                // double check synchronization
                if (!classLoaders.containsKey(identifier)) {
                    createClassLoader(identifier);
                }
            }
        }
        return classLoaders.get(identifier);
    }

    private void createClassLoader(ClassLoaderIdentifier identifier) {
        log.debug("creating classloader with key {}", identifier);
        ClassLoader parent = getParentClassLoader(identifier);
        final VirtualClassLoader virtualClassLoader = new VirtualClassLoader(identifier.getType(), identifier.getId(),
                parent);

        classLoaders.put(identifier, virtualClassLoader);
    }

    private ClassLoader getParentClassLoader(ClassLoaderIdentifier identifier) {
        final ClassLoaderIdentifier parentIdentifier = parentClassLoaderResolver
                .getParentClassLoaderIdentifier(identifier);
        if (ClassLoaderIdentifier.APPLICATION.equals(parentIdentifier)) {
            // Application classloader is the one bootstrapping bonita platform
            return ClassLoaderServiceImpl.class.getClassLoader();
        } else {
            return getLocalClassLoader(parentIdentifier);
        }
    }

    @Override
    public void removeLocalClassloader(ClassLoaderIdentifier identifier) throws SClassLoaderException {
        log.debug("Removing local classloader with {}", identifier);
        NullCheckingUtil.checkArgsNotNull(identifier);

        // Remove the class loader
        VirtualClassLoader virtualClassLoader = classLoaders.get(identifier);
        if (virtualClassLoader == null) {
            log.debug("No classloader found for identifier {}, nothing to remove", identifier);
            return;
        }
        if (virtualClassLoader.hasChildren()) {
            throw new SClassLoaderException("Unable to remove classloader " + identifier + ", it has children (" +
                    virtualClassLoader.getChildren().stream()
                            .map(VirtualClassLoader::getIdentifier)
                            .map(ClassLoaderIdentifier::toString)
                            .collect(Collectors.joining(", "))
                    + "), remove the children first");
        }
        destroyLocalClassLoader(identifier);
    }

    private void destroyLocalClassLoader(final ClassLoaderIdentifier identifier) throws SClassLoaderException {
        log.debug("Destroying local classloader with key: {}", identifier);
        final VirtualClassLoader localClassLoader = classLoaders.get(identifier);
        if (localClassLoader != null) {
            if (localClassLoader.hasChildren()) {
                throw new SClassLoaderException(
                        "Unable to delete classloader " + identifier + " because it has children: "
                                + localClassLoader.getChildren());
            }
            localClassLoader.destroy();
            classLoaders.remove(identifier);
            notifyDestroyed(localClassLoader);
        }
    }

    private void refreshGlobalClassLoader(Stream<BonitaResource> resources) throws SClassLoaderException {
        log.info("Refreshing global classloader");
        final VirtualClassLoader virtualClassloader = (VirtualClassLoader) getGlobalClassLoader();
        try {
            refreshClassLoader(virtualClassloader, resources, ClassLoaderIdentifier.GLOBAL,
                    BonitaHomeServer.getInstance().getGlobalTemporaryFolder(),
                    ClassLoaderServiceImpl.class.getClassLoader());
        } catch (Exception e) {
            throw new SClassLoaderException(e);
        }
    }

    private void refreshLocalClassLoader(ClassLoaderIdentifier identifier, Stream<BonitaResource> resources)
            throws SClassLoaderException {
        final VirtualClassLoader virtualClassloader = getVirtualClassLoaderWithoutInitializingIt(identifier);
        try {
            refreshClassLoader(virtualClassloader, resources, identifier,
                    getLocalTemporaryFolder(identifier),
                    getParentClassLoader(identifier));
            final SEvent event = new SEvent("ClassLoaderRefreshed");
            event.setObject(identifier);
            eventService.fireEvent(event);
        } catch (Exception e) {
            throw new SClassLoaderException(e);
        }
    }

    URI getLocalTemporaryFolder(ClassLoaderIdentifier identifier) throws BonitaHomeNotSetException, IOException {
        return BonitaHomeServer.getInstance().getLocalTemporaryFolder(identifier.getType().name(), identifier.getId());
    }

    private void refreshClassLoader(VirtualClassLoader virtualClassloader, Stream<BonitaResource> resources,
            ClassLoaderIdentifier id, URI temporaryFolder, ClassLoader parent) throws IOException {
        log.info("Refreshing class loader {}", id);

        final BonitaClassLoader classLoader = new BonitaClassLoader(resources, id, temporaryFolder, parent);
        log.debug("Replacing {} with {}", virtualClassloader.getClassLoader(), classLoader);
        virtualClassloader.replaceClassLoader(classLoader);
        notifyUpdateOnClassLoaderAndItsChildren(virtualClassloader);
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
        final Set<Map.Entry<ClassLoaderIdentifier, VirtualClassLoader>> entries = classLoaders.entrySet();
        while (!entries.isEmpty()) {
            final Iterator<Map.Entry<ClassLoaderIdentifier, VirtualClassLoader>> iterator = entries.iterator();
            while (iterator.hasNext()) {
                Map.Entry<ClassLoaderIdentifier, VirtualClassLoader> next = iterator.next();
                VirtualClassLoader currentClassLoader = next.getValue();
                if (!currentClassLoader.hasChildren()) {
                    currentClassLoader.destroy();
                    notifyDestroyed(currentClassLoader);
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
    public boolean addListener(ClassLoaderIdentifier identifier, SingleClassLoaderListener singleClassLoaderListener) {
        log.debug("Added listener {} on {}", singleClassLoaderListener, identifier);
        return getListeners(identifier).add(singleClassLoaderListener);
    }

    @Override
    public boolean removeListener(ClassLoaderIdentifier identifier,
            SingleClassLoaderListener singleClassLoaderListener) {
        log.debug("Removed listener {} on {}", singleClassLoaderListener, identifier);
        return getListeners(identifier).remove(singleClassLoaderListener);
    }

    Set<SingleClassLoaderListener> getListeners(ClassLoaderIdentifier identifier) {
        return this.singleClassLoaderListenersMap.computeIfAbsent(identifier, k -> new HashSet<>());
    }

    /**
     * Notify listeners that the classloader was destroyed
     * That method do not notify children because we can't destroy a classloader that have children
     */
    private void notifyDestroyed(VirtualClassLoader localClassLoader) {
        getListeners(localClassLoader.getIdentifier()).forEach(l -> {
            log.debug("Notify listener that classloader {} was destroyed: {}", localClassLoader.getIdentifier(), l);
            l.onDestroy(localClassLoader);
        });
        platformClassLoaderListeners.forEach(l -> {
            log.debug("Notify listener that classloader {} was destroyed: {}", localClassLoader.getIdentifier(), l);
            l.onDestroy(localClassLoader);
        });
    }

    /**
     * Notify listeners that the classloader was updated
     * Also notify that children classloader were updated
     */
    void notifyUpdateOnClassLoaderAndItsChildren(VirtualClassLoader virtualClassLoader) {
        getListeners(virtualClassLoader.getIdentifier()).forEach(l -> {
            log.debug("Notify listener that classloader {} was updated: {}", virtualClassLoader.getIdentifier(), l);
            l.onUpdate(virtualClassLoader);
        });
        platformClassLoaderListeners.forEach(l -> {
            log.debug("Notify listener that classloader {} was updated: {}", virtualClassLoader.getIdentifier(), l);
            l.onUpdate(virtualClassLoader);
        });
        virtualClassLoader.getChildren().forEach(this::notifyUpdateOnClassLoaderAndItsChildren);
    }

    @Override
    public void refreshClassLoaderImmediately(ClassLoaderIdentifier identifier) throws SClassLoaderException {
        final Stream<BonitaResource> resources;
        resources = getDependencies(identifier);
        if (identifier.getType() == ScopeType.GLOBAL) {
            refreshGlobalClassLoader(resources);
        } else {
            refreshLocalClassLoader(identifier, resources);
        }
    }

    Stream<BonitaResource> getDependencies(ClassLoaderIdentifier identifier) throws SClassLoaderException {
        Stream<BonitaResource> resources;
        try {
            if (ScopeType.GLOBAL == identifier.getType()) {
                resources = platformDependencyService.getDependenciesResources(identifier.getType(),
                        identifier.getId());
            } else {
                long tenantId = sessionAccessor.getTenantId();
                TenantDependencyService tenantDependencyService = dependencyServicesByTenant.get(tenantId);
                if (tenantDependencyService == null) {
                    log.warn("No dependency service is initialized on tenant {}. Initializing empty classloader",
                            tenantId);
                    return Stream.empty();
                }
                resources = tenantDependencyService.getDependenciesResources(identifier.getType(), identifier.getId());
            }
        } catch (STenantIdNotSetException | SDependencyException e) {
            throw new SClassLoaderException(e);
        }
        return resources;
    }

    @Override
    public void refreshClassLoaderAfterUpdate(ClassLoaderIdentifier identifier) throws SClassLoaderException {
        try {
            registerRefreshOnAllNodes(identifier);
        } catch (STransactionNotFoundException | STenantIdNotSetException e) {
            throw new SClassLoaderException(e);
        }
    }

    @Override
    public void refreshClassLoaderOnOtherNodes(ClassLoaderIdentifier identifier) throws SClassLoaderException {
        try {
            userTransactionService
                    .registerBonitaSynchronization((BonitaTransactionSynchronization) transactionState -> {

                        if (transactionState != Status.STATUS_COMMITTED) {
                            return;
                        }
                        Map<String, TaskResult<Void>> execute;
                        try {
                            execute = broadcastService.executeOnOthersAndWait(new RefreshClassLoaderTask(identifier),
                                    getTenantId(identifier.getType()));
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

    private void registerRefreshOnAllNodes(ClassLoaderIdentifier identifier)
            throws STransactionNotFoundException, STenantIdNotSetException {
        synchronized (synchroLock) {
            RefreshClassloaderSynchronization refreshTaskSynchronization = currentRefreshTask.get();
            if (refreshTaskSynchronization == null) {
                RefreshClassLoaderTask callable = new RefreshClassLoaderTask(identifier);
                refreshTaskSynchronization = new RefreshClassloaderSynchronization(this, broadcastService, callable,
                        classLoaderUpdater, getTenantId(identifier.getType()), identifier);
                userTransactionService.registerBonitaSynchronization(refreshTaskSynchronization);
                currentRefreshTask.set(refreshTaskSynchronization);
            } else {
                refreshTaskSynchronization.addClassloaderToRefresh(identifier);
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
