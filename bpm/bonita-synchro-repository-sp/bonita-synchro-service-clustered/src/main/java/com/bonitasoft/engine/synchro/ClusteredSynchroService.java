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
package com.bonitasoft.engine.synchro;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.cache.CommonCacheService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.synchro.AbstractSynchroService;
import org.bonitasoft.engine.synchro.SynchroObject;
import org.bonitasoft.engine.synchro.SynchroService;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Emmanuel Duchastenier
 */
public class ClusteredSynchroService extends AbstractSynchroService implements SynchroService {

    private final static String CLUSTERED_SYNCHRO_SERVICE_FIRED = "CLUSTERED_SYNCHRO_SERVICE_FIRED";

    private final static String CLUSTERED_SYNCHRO_SERVICE_WAITERS = "CLUSTERED_SYNCHRO_SERVICE_WAITERS";

    private final static String CLUSTERED_SYNCHRO_SERVICE_MUTEX = "CLUSTERED_SYNCHRO_SERVICE_MUTEX";

    private final HazelcastInstance hazelcastInstance;

    public ClusteredSynchroService(final TechnicalLoggerService logger, final HazelcastInstance hazelcastInstance, final CommonCacheService cacheService) {
        super(logger, cacheService);
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
        this.hazelcastInstance = hazelcastInstance;
        this.hazelcastInstance.getMap(CLUSTERED_SYNCHRO_SERVICE_MUTEX).put(CLUSTERED_SYNCHRO_SERVICE_MUTEX, new Object());

    }

    @Override
    protected Map<Map<String, Serializable>, SynchroObject> getWaitersMap() {
        return hazelcastInstance.getMap(CLUSTERED_SYNCHRO_SERVICE_WAITERS);
    }

    @Override
    protected Object getMutex() {
        // FIXME: Can we use something else than a Map to have a simple shared object?:
        return hazelcastInstance.getMap(CLUSTERED_SYNCHRO_SERVICE_MUTEX).get(CLUSTERED_SYNCHRO_SERVICE_MUTEX);
    }

}
