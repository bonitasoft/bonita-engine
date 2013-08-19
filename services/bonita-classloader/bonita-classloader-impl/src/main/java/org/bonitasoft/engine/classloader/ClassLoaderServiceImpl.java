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

	private final ParentClassLoaderResolver parentClassLoaderResolver;
	
    private final TechnicalLoggerService logger;

    private final String temporaryFolder;

    private VirtualClassLoader virtualGlobalClassLoader;

    private final Map<String, VirtualClassLoader> localClassLoaders = new HashMap<String, VirtualClassLoader>();

    private static final String separator = ":";

    private static final String GLOBAL_FOLDER = "global";

    public static final String GLOBAL_TYPE = "___global___";

    public static final long GLOBAL_ID = -1;

    private static final String LOCAL_FOLDER = "local";

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

    private String getKey(final String type, final long id) {
        final StringBuffer stb = new StringBuffer();
        stb.append(type);
        stb.append(separator);
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

    private synchronized VirtualClassLoader getVirtualGlobalClassLoader() {
        if (this.virtualGlobalClassLoader == null) {
            this.virtualGlobalClassLoader = new VirtualClassLoader(GLOBAL_TYPE, GLOBAL_ID, VirtualClassLoader.class.getClassLoader());
        }
        return this.virtualGlobalClassLoader;
    }

    @Override
    public synchronized ClassLoader getGlobalClassLoader() {
        return getVirtualGlobalClassLoader();
    }

    @Override
    public synchronized ClassLoader getLocalClassLoader(final String type, final long id) {
        if (this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            this.logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getLocalClassLoader"));
        }
        NullCheckingUtil.checkArgsNotNull(id, type);
        final String key = getKey(type, id);
        final VirtualClassLoader classLoader = this.localClassLoaders.get(key);
        if (classLoader == null) {
            final VirtualClassLoader virtualClassLoader = new VirtualClassLoader(type, id, new ParentRedirectClassLoader(this.parentClassLoaderResolver, this, type, id));
            this.localClassLoaders.put(key, virtualClassLoader);
        }
        if (this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            this.logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getLocalClassLoader"));
        }
        return this.localClassLoaders.get(key);
    }

    @Override
    public void removeLocalClassLoader(final String type, final long id) {
        if (this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            this.logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "removeLocalClassLoader"));
        }
        NullCheckingUtil.checkArgsNotNull(id, type);
        final String key = getKey(type, id);
        final VirtualClassLoader localClassLoader = this.localClassLoaders.get(key);
        if (localClassLoader != null) {
            localClassLoader.release();
            this.localClassLoaders.remove(key);
        }
        if (this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            this.logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "removeLocalClassLoader"));
        }
        // TODO FIXME else log
    }

    private String getGlobalTemporaryFolder() {
        final StringBuffer stb = new StringBuffer(this.temporaryFolder);
        stb.append(File.separator);
        stb.append(GLOBAL_FOLDER);
        return stb.toString();
    }

    private String getLocalTemporaryFolder(final String artifactType, final long artifactId) {
        final StringBuffer stb = new StringBuffer(this.temporaryFolder);
        stb.append(File.separator);
        stb.append(LOCAL_FOLDER);
        stb.append(File.separator);
        stb.append(artifactType);
        stb.append(File.separator);
        stb.append(artifactId);
        return stb.toString();
    }

    @Override
    public synchronized void removeAllLocalClassLoaders(final String application) {
        if (this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            this.logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "removeAllLocalClassLoaders"));
        }
        NullCheckingUtil.checkArgsNotNull(application);
        final Set<String> keySet = new HashSet<String>(this.localClassLoaders.keySet());
        for (final String key : keySet) {
            if (key.startsWith(application + separator)) {
                final VirtualClassLoader localClassLoader = this.localClassLoaders.get(key);
                if (localClassLoader != null) {
                    localClassLoader.release();
                }
                this.localClassLoaders.remove(key);
            }
        }
        if (this.logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            this.logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "removeAllLocalClassLoaders"));
        }
    }

    public String getTemporaryFolder() {
        return this.temporaryFolder;
    }

    @Override
    public synchronized void refreshGlobalClassLoader(final Map<String, byte[]> resources) {
        final VirtualClassLoader virtualClassloader = (VirtualClassLoader) getGlobalClassLoader();
        virtualClassloader.release();
        virtualClassloader.setClassLoader(new BonitaClassLoader(resources, getGlobalClassLoaderType(), getGlobalClassLoaderId(), getGlobalTemporaryFolder(),
                ClassLoaderServiceImpl.class.getClassLoader()));
    }

    @Override
    public synchronized void refreshLocalClassLoader(final String type, final long id, final Map<String, byte[]> resources) {
        final VirtualClassLoader virtualClassloader = (VirtualClassLoader) getLocalClassLoader(type, id);
        virtualClassloader.release();
        virtualClassloader.setClassLoader(new BonitaClassLoader(resources, type, id, getLocalTemporaryFolder(type, id), getGlobalClassLoader()));
    }

}
