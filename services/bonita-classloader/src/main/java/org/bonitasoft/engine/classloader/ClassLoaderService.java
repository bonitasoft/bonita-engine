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

import org.bonitasoft.engine.commons.PlatformLifecycleService;
import org.bonitasoft.engine.dependency.impl.TenantDependencyService;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @since 6.0
 */
public interface ClassLoaderService extends PlatformLifecycleService {

    void registerDependencyServiceOfTenant(Long tenantId, TenantDependencyService tenantDependencyService);

    /**
     * Get the local ClassLoader for the given type and id.
     * If no ClassLoader already exists, a new one is created and initialized.
     * This initialization is executed in a <b>different thread/transaction</b>.
     * It eagerly initialize parent classloaders.
     *
     * @return the local ClassLoader for the given type and id
     * @throws SClassLoaderException Error thrown if it's impossible to get a local ClassLoader for the given type and
     *         id
     * @param identifier of the classloader to refresh
     */
    ClassLoader getClassLoader(ClassLoaderIdentifier identifier) throws SClassLoaderException;

    void removeLocalClassloader(ClassLoaderIdentifier identifier) throws SClassLoaderException;

    /**
     * add listener on a classloader
     *
     * @param identifier the classloader id
     * @param singleClassLoaderListener the listener to add
     * @return true if the listener was added
     */
    boolean addListener(ClassLoaderIdentifier identifier, SingleClassLoaderListener singleClassLoaderListener);

    /**
     * @param identifier the classloader id
     * @param singleClassLoaderListener classloader listener to remove
     * @return true if the listener was removed
     */
    boolean removeListener(ClassLoaderIdentifier identifier, SingleClassLoaderListener singleClassLoaderListener);

    void refreshClassLoaderAfterUpdate(ClassLoaderIdentifier identifier) throws SClassLoaderException;

    void refreshClassLoaderOnOtherNodes(ClassLoaderIdentifier identifier) throws SClassLoaderException;

    /**
     * This method refreshes in the current thread/transaction the classLoader with the given identifier.
     * Contrary to refreshClassLoaderImmediately, it creates a synchronization that triggers a reload of the classloader
     * in case the transaction was rolled back, insuring there is no new loaded class in the classloader after the
     * rollback.
     *
     * @param identifier of the classloader to refresh
     */
    void refreshClassLoaderImmediatelyWithRollback(ClassLoaderIdentifier identifier) throws SClassLoaderException;

    /**
     * This method refreshes in the current thread/transaction the classLoader with the given identifier.
     * It eagerly initializes parents classloaders.
     * <p>
     * A new classloader will be created. In order to use the new classloader, references to the old one should be
     * updated.
     * <p>
     * e.g. If the classloader was set as the current context classloader, it should be reset like this
     *
     * <pre>
     *  {@code
     * Thread.currentThread().setContextClassLoader(classLoaderService.getLocalClassLoader(identifier));
     * }
     * </pre>
     *
     * @param identifier of the classloader to refresh
     */
    void refreshClassLoaderImmediately(ClassLoaderIdentifier identifier) throws SClassLoaderException;

    void removeRefreshClassLoaderSynchronization();
}
