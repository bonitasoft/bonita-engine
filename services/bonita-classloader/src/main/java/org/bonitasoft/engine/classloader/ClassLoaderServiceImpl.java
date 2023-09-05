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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
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
import org.springframework.beans.factory.annotation.Qualifier;
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
    private final Map<ClassLoaderIdentifier, BonitaClassLoader> classLoaders = new ConcurrentHashMap<>();
    private final Set<PlatformClassLoaderListener> platformClassLoaderListeners = new HashSet<>();
    private final Map<ClassLoaderIdentifier, Set<SingleClassLoaderListener>> singleClassLoaderListenersMap = Collections
            .synchronizedMap(new HashMap<>());
    private boolean shuttingDown = false;
    private final EventService eventService;
    private final PlatformDependencyService platformDependencyService;
    private final Map<Long, TenantDependencyService> dependencyServicesByTenant = new HashMap<>();
    private final SessionAccessor sessionAccessor;
    private final UserTransactionService userTransactionService;
    private final BroadcastService broadcastService;
    private final ClassLoaderUpdater classLoaderUpdater;

    public ClassLoaderServiceImpl(final ParentClassLoaderResolver parentClassLoaderResolver,
            @Qualifier("platformEventService") EventService eventService,
            PlatformDependencyService platformDependencyService,
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

    private void warnOnShuttingDown(final ClassLoaderIdentifier key) {
        if (shuttingDown) {
            log.warn("Using local classloader on after ClassLoaderService shuttingdown: " + key);
        }
    }

    @Override
    public BonitaClassLoader getClassLoader(ClassLoaderIdentifier id) {
        NullCheckingUtil.checkArgsNotNull(id);
        log.trace("Get classloader {}", id);
        warnOnShuttingDown(id);
        // when the classloader is initialized, it is done in another thread/transaction
        // We might not need to do so.
        return getOrInitializeClassloader(id, k -> classLoaderUpdater.initializeClassLoader(this, k));
    }

    public BonitaClassLoader getOrInitializeClassloader(ClassLoaderIdentifier id,
            Function<ClassLoaderIdentifier, BonitaClassLoader> createClassloaderFunction) {
        if (!classLoaders.containsKey(id)) {
            log.trace("getOrInitializeClassloader: classloader not found {}, it will be created", id);
            //computed before to avoid having nested "computeIfAbsent"
            BonitaClassLoader newClassLoader = createClassloaderFunction.apply(id);
            BonitaClassLoader classLoader = classLoaders.computeIfAbsent(id, k -> newClassLoader);
            if (!classLoader.equals(newClassLoader)) {
                log.debug("Due to concurrent initialization, the Classloader created here {} will not be used " +
                        "and will be destroyed. {} is the one that will be used", newClassLoader, classLoader);
                newClassLoader.destroy();
            }
            return classLoader;
        } else {
            return classLoaders.get(id);
        }
    }

    ClassLoader getParentClassLoader(ClassLoaderIdentifier identifier) {
        final ClassLoaderIdentifier parentIdentifier = parentClassLoaderResolver
                .getParentClassLoaderIdentifier(identifier);
        if (ClassLoaderIdentifier.APPLICATION.equals(parentIdentifier)) {
            // Application classloader is the one bootstrapping bonita platform
            return ClassLoaderServiceImpl.class.getClassLoader();
        } else {
            //get or initialize parent in the same thread/transaction
            return getOrInitializeClassloader(parentIdentifier, k -> {
                try {
                    return createClassloader(k);
                } catch (IOException | SClassLoaderException e) {
                    throw new BonitaRuntimeException(e);
                }
            });
        }
    }

    @Override
    public void removeLocalClassloader(ClassLoaderIdentifier identifier) throws SClassLoaderException {
        NullCheckingUtil.checkArgsNotNull(identifier);
        log.debug("Removing local classloader with {}", identifier);
        BonitaClassLoader localClassLoader = classLoaders.get(identifier);
        if (localClassLoader != null) {
            destroyAndRemoveClassLoader(localClassLoader);
            notifyDestroyed(localClassLoader);
        }
    }

    private void destroyAndRemoveClassLoader(BonitaClassLoader localClassLoader) throws SClassLoaderException {
        if (localClassLoader.hasChildren()) {
            throw new SClassLoaderException(
                    "Unable to delete classloader " + localClassLoader.getIdentifier() + " because it has children: "
                            + localClassLoader.getChildren());
        }
        localClassLoader.destroy();
        classLoaders.remove(localClassLoader.getIdentifier());
    }

    private List<BonitaClassLoader> getClassLoaderTreeLeavesFirst(BonitaClassLoader root) {
        List<BonitaClassLoader> tree = new ArrayList<>();
        for (BonitaClassLoader child : root.getChildren()) {
            tree.addAll(getClassLoaderTreeLeavesFirst(child));
        }
        tree.add(root);
        return tree;
    }

    URI getLocalTemporaryFolder(ClassLoaderIdentifier identifier) throws IOException {
        return BonitaHomeServer.getInstance().getLocalTemporaryFolder(identifier.getType().name(), identifier.getId());
    }

    BonitaClassLoader createClassloader(ClassLoaderIdentifier id) throws IOException, SClassLoaderException {
        log.debug("Creating classloader {}", id);
        BonitaClassLoader classLoader = BonitaClassLoaderFactory.createClassLoader(getDependencies(id), id,
                getLocalTemporaryFolder(id),
                getParentClassLoader(id));
        log.info("Created classloader {}: {}", id, classLoader);
        return classLoader;
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
        BonitaClassLoader global = classLoaders.get(ClassLoaderIdentifier.GLOBAL);
        if (global == null) {
            log.debug("No ClassLoaders to destroy");
            return;
        }
        //destroy classloader starting from the leaves to avoid checking for children
        List<BonitaClassLoader> allClassLoaders = getClassLoaderTreeLeavesFirst(global);
        for (BonitaClassLoader currentClassLoader : allClassLoaders) {
            currentClassLoader.destroy();
            notifyDestroyed(currentClassLoader);
            if (classLoaders.remove(currentClassLoader.getIdentifier()) == null) {
                log.warn("One classloader of the tree is not present in the list: classloader = {} and list = {}",
                        currentClassLoader, classLoaders);
            }
        }
        //the classloader map should be empty at this point. Clearing it to ensure that we never reuse old classloader in that case
        if (!classLoaders.isEmpty()) {

            log.warn("Classloader tree was destroyed but some classloaders were still references in the map {}",
                    classLoaders);
        }
        classLoaders.clear();
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
     *
     * @param classLoader
     */
    private void notifyDestroyed(BonitaClassLoader classLoader) {
        getListeners(classLoader.getIdentifier()).forEach(l -> {
            log.debug("Notify listener that classloader {} was destroyed: {}", classLoader.getIdentifier(), l);
            l.onDestroy(classLoader);
        });
        platformClassLoaderListeners.forEach(l -> {
            log.debug("Notify listener that classloader {} was destroyed: {}", classLoader.getIdentifier(), l);
            l.onDestroy(classLoader);
        });
    }

    /**
     * Notify listeners that the classloader was updated
     * Also notify that children classloader were updated
     */
    void notifyUpdated(BonitaClassLoader newClassLoader) {
        getListeners(newClassLoader.getIdentifier()).forEach(l -> {
            log.debug("Notify listener that classloader {} was updated: {}", newClassLoader.getIdentifier(), l);
            l.onUpdate(newClassLoader);
        });
        platformClassLoaderListeners.forEach(l -> {
            log.debug("Notify listener that classloader {} was updated: {}", newClassLoader.getIdentifier(), l);
            l.onUpdate(newClassLoader);
        });
    }

    @Override
    public void refreshClassLoaderImmediatelyWithRollback(ClassLoaderIdentifier identifier)
            throws SClassLoaderException {
        // Register the rollback before refreshing classloader in case refreshClassLoaderImmediately() fails:
        registerAfterCommitClassloaderUpdate(identifier);
        refreshClassLoaderImmediately(identifier);
    }

    @Override
    public void refreshClassLoaderImmediately(ClassLoaderIdentifier identifier) throws SClassLoaderException {
        try {
            log.info("Refreshing classloader {}", identifier);
            BonitaClassLoader newClassloader = createClassloader(identifier);
            BonitaClassLoader previous = classLoaders.put(identifier, newClassloader);
            // Destroy and remove all children classloaders of the `previous` classloader. They need to be recreated
            if (previous != null) {
                //destroy classloader starting from the leaves to avoid checking for children
                for (BonitaClassLoader classLoader : getClassLoaderTreeLeavesFirst(previous)) {
                    classLoader.destroy();
                    notifyDestroyed(classLoader);
                    // remove(key,value) only remove the value if its the one given,
                    // We do that to avoid removing the one we just added to the map
                    classLoaders.remove(classLoader.getIdentifier(), classLoader);
                }
                log.debug("Refreshed classloader {}, {} was replaced by {}", identifier, previous, classLoaders);
            } else {
                log.debug("Refreshed classloader {}, There was no classloader, classloader set: {}", identifier,
                        classLoaders);
            }
            notifyUpdated(newClassloader);
            final SEvent event = new SEvent("ClassLoaderRefreshed");
            event.setObject(identifier);
            eventService.fireEvent(event);
        } catch (Exception e) {
            throw new SClassLoaderException(e);
        }
    }

    private void registerAfterCommitClassloaderUpdate(ClassLoaderIdentifier identifier) throws SClassLoaderException {
        try {
            userTransactionService.registerBonitaSynchronization((BonitaTransactionSynchronization) i -> {
                if (i != Status.STATUS_COMMITTED) {
                    try {
                        log.warn("The transaction was not committed. Refreshing classloader on tenantId "
                                + sessionAccessor.getTenantId() + " to return to a clean state.");
                        classLoaderUpdater.refreshClassloaders(this, sessionAccessor.getTenantId(),
                                Collections.singleton(identifier));
                    } catch (STenantIdNotSetException e) {
                        //sessionAccessor.getTenantId() is called by getDependencies in refreshClassLoaderImmediately
                        // In other words this should never happen
                        log.error("Cannot find the tenantID to refresh classloader on. This should not happen.");
                        throw new BonitaRuntimeException(e);
                    }
                }
            });
        } catch (STransactionNotFoundException e) {
            throw new SClassLoaderException(e);
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
