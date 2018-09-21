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

import java.util.stream.Stream;

import org.bonitasoft.engine.commons.PlatformLifecycleService;
import org.bonitasoft.engine.home.BonitaResource;

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
     * @throws SClassLoaderException Error thrown if it's impossible to get the global ClassLoader
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
     * @param type The classloader's type identifier
     * @param id   The local ClassLoader's id
     * @return the local ClassLoader for the given type and id
     * @throws SClassLoaderException Error thrown if it's impossible to get a local ClassLoader for the given type and id
     */
    ClassLoader getLocalClassLoader(final String type, final long id) throws SClassLoaderException;

    /**
     * Remove the local ClassLoader identified by the given type and id;
     *
     * @param type The classloader's type identifier
     * @param id   The local ClassLoader's id
     * @throws SClassLoaderException if we can't remove the classloader because it contains children
     */
    void removeLocalClassLoader(final String type, final long id) throws SClassLoaderException;

    void refreshGlobalClassLoader(Stream<BonitaResource> resources) throws SClassLoaderException;

    void refreshLocalClassLoader(String type, long id, Stream<BonitaResource> resources) throws SClassLoaderException;

    /**
     * add listener on a classloader
     *
     * @param type                the classloader type
     * @param id                  the classloader id
     * @param classLoaderListener the listener to add
     * @return true if the listener was added
     */
    boolean addListener(final String type, final long id, ClassLoaderListener classLoaderListener);

    /**
     * @param type                the classloader type
     * @param id                  the classloader id
     * @param classLoaderListener classloader listener to remove
     * @return true if the listener was removed
     */
    boolean removeListener(String type, long id, ClassLoaderListener classLoaderListener);

    /**
     * add a listener that will listen all classloader events
     *
     * @param classLoaderListener the listener to add
     * @return true if the listener was added
     */
    boolean addListener(ClassLoaderListener classLoaderListener);

    /**
     * remove a global listener
     *
     * @param classLoaderListener classloader listener to remove
     * @return true if the listener was removed
     */
    boolean removeListener(ClassLoaderListener classLoaderListener);
}
