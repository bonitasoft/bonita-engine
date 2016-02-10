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
package org.bonitasoft.engine.work;

import org.bonitasoft.engine.commons.TenantLifecycleService;

/**
 * This service allows to trigger the execution of work asynchronously
 * Any runnable registered on the service will be launched in other thread at the end of the transaction.
 *
 * @author Charles Souillard
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface WorkService extends TenantLifecycleService {

    /**
     * This operation MUST be called within an active transaction. If no active transaction is found, a SWorkRegisterException is thrown
     *
     * @param work
     * @throws SWorkRegisterException
     * @since 6.0
     */
    void registerWork(final BonitaWork work) throws SWorkRegisterException;

    /**
     * @param work
     * @throws SWorkRegisterException
     * @since 6.0
     */
    void executeWork(final BonitaWork work) throws SWorkRegisterException;

    /**
     * @return
     *         true if the work service is stopped
     * @since 6.3
     */
    boolean isStopped();

    /**
     * @since 6.3.1
     */
    void notifyNodeStopped(String nodeName);

    /**
     * Removes the current <code>AbstractWorkSynchronization</code> from the current Thread.
     */
    void removeSynchronization();

}
