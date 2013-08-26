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
package org.bonitasoft.engine.synchro;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Emmanuel Duchastenier
 * @author Baptiste Mesta
 * @author Charles Souillard
 */
public class SynchroServiceImpl extends AbstractSynchroService {

    private final Map<Map<String, Serializable>, SynchroObject> waiters;

    private final Map<Map<String, Serializable>, Serializable> fired;

    private final Object mutex = new Object();

    /**
     * @param initialCapacity
     *            the initial capacity of the map of fired events / waiters (default 50)
     * @param logger
     *            the technical logger service
     */
    private SynchroServiceImpl(final int initialCapacity, final TechnicalLoggerService logger) {
        super(logger);
        fired = new HashMap<Map<String, Serializable>, Serializable>(initialCapacity);
        waiters = new HashMap<Map<String, Serializable>, SynchroObject>(initialCapacity);
    }

    @Override
    protected Map<Map<String, Serializable>, SynchroObject> getWaitersMap() {
        return waiters;
    }

    @Override
    protected Map<Map<String, Serializable>, Serializable> getFiredMap() {
        return fired;
    }

    @Override
    protected Object getMutex() {
        return mutex;
    }
}
