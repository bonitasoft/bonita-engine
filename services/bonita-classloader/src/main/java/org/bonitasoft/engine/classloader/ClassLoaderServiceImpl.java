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
package org.bonitasoft.engine.classloader;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.bonitasoft.engine.classloader.listeners.ClassReflectorClearer;
import org.bonitasoft.engine.classloader.listeners.JacksonCacheClearer;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.home.BonitaResource;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ClassLoaderServiceImpl implements ClassLoaderService {

    private final ParentClassLoaderResolver parentClassLoaderResolver;

    private final TechnicalLoggerService logger;

    private VirtualClassLoader virtualGlobalClassLoader = new VirtualClassLoader(ClassLoaderIdentifier.GLOBAL_TYPE, ClassLoaderIdentifier.GLOBAL_ID, VirtualClassLoader.class.getClassLoader());

    private final Map<ClassLoaderIdentifier, VirtualClassLoader> localClassLoaders = new HashMap<>();

    private final Set<ClassLoaderListener> globalListeners = new HashSet<>();

    private final Object mutex = new ClassLoaderServiceMutex();

    private boolean shuttingDown = false;

    private final EventService eventService;
    private boolean traceEnabled;

    public ClassLoaderServiceImpl(final ParentClassLoaderResolver parentClassLoaderResolver, final TechnicalLoggerService logger,
                                  final EventService eventService) {
        this.parentClassLoaderResolver = parentClassLoaderResolver;
        this.logger = logger;
        this.eventService = eventService;
        traceEnabled = logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE);
        globalListeners.add(new ClassReflectorClearer());
        globalListeners.add(new JacksonCacheClearer());
        // BS-9304 : Create the temporary directory with the IOUtil class, to delete it at the end of the JVM
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
        if (shuttingDown && logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
            logger.log(getClass(), TechnicalLogSeverity.WARNING, "Using local classloader on after ClassLoaderService shuttingdown: " + key);
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
        if (traceEnabled) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "creating classloader with key " + identifier);
        }
        VirtualClassLoader parent = getParentClassLoader(identifier);
        final VirtualClassLoader virtualClassLoader = new VirtualClassLoader(identifier.getType(), identifier.getId(), parent);
        localClassLoaders.put(identifier, virtualClassLoader);
    }

    private VirtualClassLoader getParentClassLoader(ClassLoaderIdentifier identifier) {
        final ClassLoaderIdentifier parentIdentifier = parentClassLoaderResolver.getParentClassLoaderIdentifier(identifier);
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
        if (traceEnabled) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Removing local classloader for type " + type + " of id " + id);
        }
        NullCheckingUtil.checkArgsNotNull(id, type);

        // Remove the class loader
        final ClassLoaderIdentifier key = getKey(type, id);
        destroyLocalClassLoader(key);
    }

    private void destroyLocalClassLoader(final ClassLoaderIdentifier key) throws SClassLoaderException {
        if (traceEnabled) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Destroying local classloader with key: " + key);
        }
        final VirtualClassLoader localClassLoader = localClassLoaders.get(key);
        if (localClassLoader != null) {
            if (localClassLoader.hasChildren()) {
                throw new SClassLoaderException("Unable to delete classloader " + key + " because it has children: " + localClassLoader.getChildren());
            }
            localClassLoader.destroy();
            localClassLoaders.remove(key);
            for (ClassLoaderListener globalListener : globalListeners) {
                globalListener.onDestroy(localClassLoader);
            }
        }
    }

    public void refreshGlobalClassLoader(Stream<BonitaResource> resources) throws SClassLoaderException {
        logger.log(this.getClass(), TechnicalLogSeverity.INFO, "Refreshing global classloader");
        final VirtualClassLoader virtualClassloader = (VirtualClassLoader) getGlobalClassLoader();
        try {
            refreshClassLoader(virtualClassloader, resources, getGlobalClassLoaderType(), getGlobalClassLoaderId(),
                    BonitaHomeServer.getInstance().getGlobalTemporaryFolder(),
                    ClassLoaderServiceImpl.class.getClassLoader());
        } catch (Exception e) {
            throw new SClassLoaderException(e);
        }
    }

    @Override
    public void refreshLocalClassLoader(String type, long id, Stream<BonitaResource> resources) throws SClassLoaderException {
        final ClassLoaderIdentifier key = getKey(type, id);
        logger.log(this.getClass(), TechnicalLogSeverity.INFO, "Refreshing classloader with key: " + key);
        final VirtualClassLoader virtualClassloader = getLocalClassLoader(type, id);
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

    protected URI getLocalTemporaryFolder(String type, long id) throws BonitaHomeNotSetException, IOException {
        return BonitaHomeServer.getInstance().getLocalTemporaryFolder(type, id);
    }

    private void refreshClassLoader(final VirtualClassLoader virtualClassloader, Stream<BonitaResource> resources, final String type, final long id,
                                    final URI temporaryFolder, final ClassLoader parent) {
        final BonitaClassLoader classLoader = new BonitaClassLoader(resources, type, id, temporaryFolder, parent);
        virtualClassloader.replaceClassLoader(classLoader);
        for (ClassLoaderListener globalListener : new HashSet<>(globalListeners)) {
            globalListener.onUpdate(virtualClassloader);
        }
    }

    @Override
    public void start() {
        if (traceEnabled) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Starting classloader service, creating the platform classloader");
        }
        shuttingDown = false;
        //we do not create or destroy the global classloader because it does not point to a bonita classloader
    }

    @Override
    public void stop() throws SClassLoaderException {
        if (traceEnabled) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Stopping classloader service, destroying all classloaders");
        }
        shuttingDown = true;
        destroyAllLocalClassLoaders();
    }

    private void destroyAllLocalClassLoaders() throws SClassLoaderException {
        if (traceEnabled) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Destroying all classloaders");
        }
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
        logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Added listener " + classLoaderListener + " on " + type + " " + id);
        final VirtualClassLoader localClassLoader = getLocalClassLoader(type, id);
        return localClassLoader.addListener(classLoaderListener);
    }

    @Override
    public boolean removeListener(String type, long id, ClassLoaderListener classLoaderListener) {
        logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Removed listener " + classLoaderListener + " on " + type + " " + id);
        VirtualClassLoader localClassLoader = getLocalClassLoader(type, id);
        return localClassLoader.removeListener(classLoaderListener);
    }

    @Override
    public boolean addListener(ClassLoaderListener classLoaderListener) {
        logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Added global listener " + classLoaderListener);
        return globalListeners.add(classLoaderListener);
    }

    @Override
    public boolean removeListener(ClassLoaderListener classLoaderListener) {
        logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Removed  global listener " + classLoaderListener);
        return globalListeners.remove(classLoaderListener);
    }
}
