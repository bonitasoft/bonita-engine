/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.test.synchro;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public interface EventRepository {

    /**
     * Blocking method that will wait for the given event to be fired. If the event has already be fired, returns immediately.
     * 
     * @param event
     *            the event to wait for.
     * @return
     * @throws InterruptedException
     *             if thread cannot be interrupted
     * @throws TimeoutException
     *             exception thrown if event is not fired even after timeout milliseconds
     */
	Serializable waitForEvent(final Map<String, Serializable> event, final long timeout) throws InterruptedException, TimeoutException;

    /**
     * Fire the given event and notify waiters.
     * 
     * @param event
     *            the event to fire
     * @param id
     */
    void fireEvent(final Map<String, Serializable> event, Serializable id);

    /**
     * Reset repository state by deleting all generated events not yet consumed.
     */
    void clearAllEvents();

    /**
     * Check whether the repository has some thread currently waiting for a event.
     */
    boolean hasWaiters();

}
