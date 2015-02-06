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

import java.util.Map;

import org.bonitasoft.engine.commons.PlatformLifecycleService;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @since 6.0
 */
public interface ClassLoaderService extends PlatformLifecycleService {

    /**
     * Get the global ClassLoader. If no global ClassLoader already exists, create it.
     * 
     * @return the global ClassLoader
     * @throws SClassLoaderException
     *         Error thrown if it's impossible to get the global ClassLoader
     */
    ClassLoader getGlobalClassLoader() throws SClassLoaderException;

    /**
     * Get type of global class loader
     * 
     * @return type of global class loader
     */
    String getGlobalClassLoaderType();

    /**
     * Get id of global class loader
     * 
     * @return id of global class loader
     */
    long getGlobalClassLoaderId();

    /**
     * Get the local ClassLoader for the given type and id. If no ClassLoader is associated to them,
     * a new one is created.
     * 
     * @param type
     *        The classloader's type identifier
     * @param id
     *        The local ClassLoader's id
     * @return the local ClassLoader for the given type and id
     * @throws SClassLoaderException
     *         Error thrown if it's impossible to get a local ClassLoader for the given type and id
     */
    ClassLoader getLocalClassLoader(final String type, final long id) throws SClassLoaderException;

    /**
     * Remove the local ClassLoader identified by the given type and id;
     * 
     * @param type
     *        The classloader's type identifier
     * @param id
     *        The local ClassLoader's id
     */
    void removeLocalClassLoader(final String type, final long id);

    /**
     * Remove all local ClassLoaders associated to the given type
     * 
     * @param type
     *        the ClassLoader's type
     */
    void removeAllLocalClassLoaders(final String type);

    void refreshGlobalClassLoader(final Map<String, byte[]> resources) throws SClassLoaderException;

    void refreshLocalClassLoader(final String type, final long id, final Map<String, byte[]> resources) throws SClassLoaderException;
}
