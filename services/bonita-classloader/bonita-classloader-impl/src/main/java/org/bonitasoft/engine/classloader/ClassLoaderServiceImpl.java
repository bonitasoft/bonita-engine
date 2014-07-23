/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ClassLoaderServiceImpl implements ClassLoaderService {

    private static final String SEPARATOR = ":";

    private static final String GLOBAL_FOLDER = "global";

    public static final String GLOBAL_TYPE = "GLOBAL";

    public static final long GLOBAL_ID = -1;

    private static final String LOCAL_FOLDER = "local";

    private final ParentClassLoaderResolver parentClassLoaderResolver;

    private final TechnicalLoggerService logger;

    private final String temporaryFolder;

    private final VirtualClassLoader virtualGlobalClassLoader = new VirtualClassLoader(GLOBAL_TYPE, GLOBAL_ID, VirtualClassLoader.class.getClassLoader());

    private final Map<String, VirtualClassLoader> localClassLoaders = new HashMap<String, VirtualClassLoader>();

    private final Object mutex = new ClassLoaderServiceMutex();

    public ClassLoaderServiceImpl(final ParentClassLoaderResolver parentClassLoaderResolver, final String temporaryFolder, final TechnicalLoggerService logger) {
        this.parentClassLoaderResolver = parentClassLoaderResolver;
        this.logger = logger;
        if (temporaryFolder.startsWith("${") && temporaryFolder.contains("}")) {
            final Pattern pattern = Pattern.compile("^(.*)\\$\\{(.*)\\}(.*)$");
            final Matcher matcher = pattern.matcher(temporaryFolder);
            matcher.find();
            final StringBuilder sb = new StringBuilder();
            sb.append(matcher.group(1));
            sb.append(System.getProperty(matcher.group(2)));
            sb.append(matcher.group(3));
            this.temporaryFolder = sb.toString();
        } else {
            this.temporaryFolder = temporaryFolder;
        }
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

    @Override
    public ClassLoader getLocalClassLoader(final String type, final long id) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getLocalClassLoader"));
        }
        NullCheckingUtil.checkArgsNotNull(id, type);
        final String key = getKey(type, id);
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
        return localClassLoaders.get(key);
    }

    @Override
    public void removeLocalClassLoader(final String type, final long id) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "removeLocalClassLoader"));
        }
        NullCheckingUtil.checkArgsNotNull(id, type);
        final String key = getKey(type, id);
        final VirtualClassLoader localClassLoader = localClassLoaders.get(key);
        if (localClassLoader != null) {
            localClassLoader.release();
            localClassLoaders.remove(key);
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "removeLocalClassLoader"));
        }
        // TODO FIXME else log
    }

    private String getGlobalTemporaryFolder() {
        final StringBuffer stb = new StringBuffer(temporaryFolder);
        stb.append(File.separator);
        stb.append(GLOBAL_FOLDER);
        return stb.toString();
    }

    private String getLocalTemporaryFolder(final String artifactType, final long artifactId) {
        final StringBuffer stb = new StringBuffer(temporaryFolder);
        stb.append(File.separator);
        stb.append(LOCAL_FOLDER);
        stb.append(File.separator);
        stb.append(artifactType);
        stb.append(File.separator);
        stb.append(artifactId);
        return stb.toString();
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
                final VirtualClassLoader localClassLoader = localClassLoaders.get(key);
                if (localClassLoader != null) {
                    localClassLoader.release();
                }
                localClassLoaders.remove(key);
            }
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "removeAllLocalClassLoaders"));
        }
    }

    public String getTemporaryFolder() {
        return temporaryFolder;
    }

    @Override
    public void refreshGlobalClassLoader(final Map<String, byte[]> resources) {
        final VirtualClassLoader virtualClassloader = (VirtualClassLoader) getGlobalClassLoader();
        virtualClassloader.release();
        virtualClassloader.setClassLoader(new BonitaClassLoader(resources, getGlobalClassLoaderType(), getGlobalClassLoaderId(), getGlobalTemporaryFolder(),
                ClassLoaderServiceImpl.class.getClassLoader()));
    }

    @Override
    public void refreshLocalClassLoader(final String type, final long id, final Map<String, byte[]> resources) {
        final VirtualClassLoader virtualClassloader = (VirtualClassLoader) getLocalClassLoader(type, id);
        virtualClassloader.release();
        virtualClassloader.setClassLoader(new BonitaClassLoader(resources, type, id, getLocalTemporaryFolder(type, id), new ParentRedirectClassLoader(
                getGlobalClassLoader(), parentClassLoaderResolver, this, type, id)));
    }

}
