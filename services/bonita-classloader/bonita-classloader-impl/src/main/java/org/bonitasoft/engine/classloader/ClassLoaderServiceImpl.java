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

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.impl.SEventImpl;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ClassLoaderServiceImpl implements ClassLoaderService {

    private static final String SEPARATOR = ":";

    public static final String GLOBAL_TYPE = "GLOBAL";

    public static final long GLOBAL_ID = -1;

    private final ParentClassLoaderResolver parentClassLoaderResolver;

    private final TechnicalLoggerService logger;

    private VirtualClassLoader virtualGlobalClassLoader = new VirtualClassLoader(GLOBAL_TYPE, GLOBAL_ID, VirtualClassLoader.class.getClassLoader());

    private final Map<String, VirtualClassLoader> localClassLoaders = new HashMap<String, VirtualClassLoader>();

    private final Object mutex = new ClassLoaderServiceMutex();

    private boolean shuttingDown = false;

    private final EventService eventService;

    public ClassLoaderServiceImpl(final ParentClassLoaderResolver parentClassLoaderResolver, final TechnicalLoggerService logger, final EventService eventService) {
        this.parentClassLoaderResolver = parentClassLoaderResolver;
        this.logger = logger;
        this.eventService = eventService;
        // BS-9304 : Create the temporary directory with the IOUtil class, to delete it at the end of the JVM
    }

    private static final class ClassLoaderServiceMutex {

    }

    private String getKey(final String type, final long id) {
        final StringBuffer stb = new StringBuffer();
        stb.append(type);
        stb.append(SEPARATOR);
        stb.append(id);
        return stb.toString();
    }

    @Override
    public long getGlobalClassLoaderId() {
        return GLOBAL_ID;
    }

    @Override
    public String getGlobalClassLoaderType() {
        return GLOBAL_TYPE;
    }

    private VirtualClassLoader getVirtualGlobalClassLoader() {
        return virtualGlobalClassLoader;
    }

    @Override
    public ClassLoader getGlobalClassLoader() {
        return getVirtualGlobalClassLoader();
    }

    private void warnOnShuttingDown(final String key) {
        if (shuttingDown && logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
            logger.log(getClass(), TechnicalLogSeverity.WARNING, "Using local classloader on after ClassLoaderService shuttingdown: " + key);
        }
    }

    @Override
    public ClassLoader getLocalClassLoader(final String type, final long id) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getLocalClassLoader"));
        }
        NullCheckingUtil.checkArgsNotNull(id, type);
        final String key = getKey(type, id);
        warnOnShuttingDown(key);
        // here we have to manage the case of the "first" get to avoid creating 2 classloaders
        // we decided to do it in a "double" check manner
        // as it happens almost "never" (concurrency maybe on 2 or more threads but only on time...
        // we use the same mutex for all pair type/id
        if (!localClassLoaders.containsKey(key)) {
            synchronized (mutex) {
                // here we have to check again the classloader is still not null as it can be not null if the thread executing now is the "second" one
                if (!localClassLoaders.containsKey(key)) {
                    final VirtualClassLoader virtualClassLoader = new VirtualClassLoader(type, id, new ParentRedirectClassLoader(getGlobalClassLoader(),
                            parentClassLoaderResolver, this, type, id));
                    localClassLoaders.put(key, virtualClassLoader);
                }
            }
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getLocalClassLoader"));
        }
        // final VirtualClassLoader virtualClassLoader = localClassLoaders.get(key);
        // final String newKey = getKey(virtualClassLoader.getArtifactType(), virtualClassLoader.getArtifactId());
        // if (!key.equals(newKey)) {
        // logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "***************** WARNING: getLocalClassLoader() returning wrong Local CL. key: " + newKey
        // + "  *******************");
        // }
        return localClassLoaders.get(key);
    }

    @Override
    public void removeLocalClassLoader(final String type, final long id) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "removeLocalClassLoader")
                    + ": Removing local classloader for type " + type + " of id " + id);
        }
        NullCheckingUtil.checkArgsNotNull(id, type);

        // Remove the class loader
        final String key = getKey(type, id);
        destroyLocalClassLoader(key);

        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "removeLocalClassLoader"));
        }
    }

    @Override
    public void removeAllLocalClassLoaders(final String application) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "removeAllLocalClassLoaders"));
        }
        NullCheckingUtil.checkArgsNotNull(application);
        final Set<String> keySet = new HashSet<String>(localClassLoaders.keySet());
        for (final String key : keySet) {
            if (key.startsWith(application + SEPARATOR)) {
                destroyLocalClassLoader(key);
            }
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "removeAllLocalClassLoaders"));
        }
    }

    private void destroyLocalClassLoader(final String key) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Destroying local classloader with key: " + key);
        }
        final VirtualClassLoader localClassLoader = localClassLoaders.get(key);
        if (localClassLoader != null) {
            localClassLoader.destroy();
            localClassLoaders.remove(key);
        }
    }

    @Override
    public void refreshGlobalClassLoader(final Map<String, byte[]> resources) throws SClassLoaderException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Refreshing global classloader");
        }
        final VirtualClassLoader virtualClassloader = (VirtualClassLoader) getGlobalClassLoader();
        try {
            refreshClassLoader(virtualClassloader, resources, getGlobalClassLoaderType(), getGlobalClassLoaderId(), BonitaHomeServer.getInstance().getGlobalTemporaryFolder(),
                    ClassLoaderServiceImpl.class.getClassLoader());
        } catch (Exception e) {
            throw new SClassLoaderException(e);
        }
    }

    @Override
    public void refreshLocalClassLoader(final String type, final long id, final Map<String, byte[]> resources) throws SClassLoaderException {
        final String key = getKey(type, id);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Refreshing local classloader with key: " + key);
        }
        final VirtualClassLoader virtualClassloader = (VirtualClassLoader) getLocalClassLoader(type, id);
        try {
            refreshClassLoader(virtualClassloader, resources, type, id, BonitaHomeServer.getInstance().getLocalTemporaryFolder(type, id), new ParentRedirectClassLoader(
                    getGlobalClassLoader(), parentClassLoaderResolver, this, type, id));
            final String eventType = "ClassLoaderRefreshed";
            final SEvent event = new SEventImpl(eventType);
            event.setObject(key);
            eventService.fireEvent(event);
        } catch (Exception e) {
            throw new SClassLoaderException(e);
        }
    }

    private void refreshClassLoader(final VirtualClassLoader virtualClassloader, final Map<String, byte[]> resources, final String type, final long id,
                                    final URI temporaryFolder, final ClassLoader parent) {
        virtualClassloader.destroy();
        final BonitaClassLoader classLoader = new BonitaClassLoader(resources, type, id, temporaryFolder, parent);
        virtualClassloader.setClassLoader(classLoader);
    }

    @Override
    public void start() {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Starting classloader service");
        }
        shuttingDown = false;
        virtualGlobalClassLoader = new VirtualClassLoader(GLOBAL_TYPE, GLOBAL_ID, VirtualClassLoader.class.getClassLoader());
    }

    @Override
    public void stop() {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Stopping classloader service");
        }
        shuttingDown = true;
        destroyAllLocalClassLoaders();
        virtualGlobalClassLoader.destroy();
    }

    private void destroyAllLocalClassLoaders() {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Destroying all classloaders");
        }
        for (final VirtualClassLoader classLoader : localClassLoaders.values()) {
            classLoader.destroy();
        }
        localClassLoaders.clear();
    }

    @Override
    public void pause() {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Pausing classloader service");
        }
        // Nothing to do
    }

    @Override
    public void resume() {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Resuming classloader service");
        }
        // Nothing to do
    }
}
