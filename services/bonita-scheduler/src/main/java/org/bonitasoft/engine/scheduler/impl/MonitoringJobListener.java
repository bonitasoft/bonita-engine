/**
 * Copyright (C) 2019 BonitaSoft S.A.
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
package org.bonitasoft.engine.scheduler.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.bonitasoft.engine.scheduler.BonitaJobListener;

public class MonitoringJobListener implements BonitaJobListener {

    private static final long serialVersionUID = 2830540082890033377L;

    private final Map<Long, AtomicLong> executing = new ConcurrentHashMap<>();
    private final Map<Long, AtomicLong> executed = new ConcurrentHashMap<>();


    public MonitoringJobListener() {
    }

    @Override
    public void jobToBeExecuted(final Map<String, Serializable> context) {
        final Long tenantId = (Long) context.get(TENANT_ID);
        initializeOrGet(tenantId, this.executing).incrementAndGet();

    }

    private AtomicLong initializeOrGet(Long tenantId, Map<Long, AtomicLong> map) {
        AtomicLong counter = map.get(tenantId);
        if (counter == null) {
            synchronized(this) {
                if (map.get(tenantId) == null) {
                    map.put(tenantId, new AtomicLong());
                }
                counter = map.get(tenantId);
            }
        }
        return counter;
    }

    @Override
    public void jobExecutionVetoed(final Map<String, Serializable> context) {
        final Long tenantId = (Long) context.get(TENANT_ID);
        initializeOrGet(tenantId, this.executing).decrementAndGet();
    }

    @Override
    public void jobWasExecuted(final Map<String, Serializable> context, final Exception jobException) {
        final Long tenantId = (Long) context.get(TENANT_ID);
        initializeOrGet(tenantId, this.executing).decrementAndGet();
        initializeOrGet(tenantId, this.executed).incrementAndGet();
    }

    public long getNumberOfExecutingJobs(Long tenantId) {
        return initializeOrGet(tenantId, this.executing).get();
    }
    public long getNumberOfExecutedJobs(Long tenantId) {
        return initializeOrGet(tenantId, this.executed).get();

    }


}
