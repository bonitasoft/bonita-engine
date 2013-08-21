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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use SynchroRepository as a static accessor to an EventRepository. In a future version we could imagine to transform this utility class into an Engine
 * Singleton Service.
 * 
 * @author Nicolas Chabanoles
 * @author Baptiste Mesta
 */
public class SynchroRepository {

    private static Logger LOGGER = LoggerFactory.getLogger(SynchroRepository.class);

    private static final EventRepository repo = MutexEventRepository.getInstance();

    public static void fireEvent(final Map<String, Serializable> event, final Serializable id) {
        LOGGER.debug("Fire: " + event + " (id:" + id + ")");
        repo.fireEvent(event, id);
    }

    public static Serializable waitForEvent(final Map<String, Serializable> event, final long timeout) throws InterruptedException, TimeoutException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Wait: " + event);
        }
        final Serializable waitForEvent = repo.waitForEvent(event, timeout);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Receive: " + event + " (id:" + waitForEvent + ")");
        }
        return waitForEvent;
    }

    public static void clearAllEvents() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("clear all events");
        }
        repo.clearAllEvents();
    }

    public static boolean hasWaiters() {
        return repo.hasWaiters();

    }
}
