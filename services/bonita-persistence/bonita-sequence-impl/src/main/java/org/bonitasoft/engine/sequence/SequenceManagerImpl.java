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
package org.bonitasoft.engine.sequence;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.lock.LockService;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class SequenceManagerImpl implements SequenceManager {

    private final Map<Long, Integer> rangeSizes;

    private final int defaultRangeSize;

    private final Map<String, Long> sequencesMappings;

    private final int retries;

    private final int delay;

    private final int delayFactor;

    private final DataSource datasource;

    private final LockService lockService;

    private final Map<Long, TenantSequenceManagerImpl> sequenceManagers = new HashMap<Long, TenantSequenceManagerImpl>();

    private final Object mutex = new SequenceManagerImplMutex();

    public SequenceManagerImpl(final LockService lockService, final Map<Long, Integer> rangeSizes, final int defaultRangeSize,
            final Map<String, Long> sequencesMappings,
            final DataSource datasource, final int retries, final int delay, final int delayFactor) {
        this.lockService = lockService;
        this.defaultRangeSize = defaultRangeSize;
        this.rangeSizes = rangeSizes;
        this.sequencesMappings = sequencesMappings;
        this.retries = retries;
        this.delay = delay;
        this.delayFactor = delayFactor;
        this.datasource = datasource;
    }

    private static final class SequenceManagerImplMutex {

    }

    @Override
    public void reset() {
        this.sequenceManagers.clear();
    }

    @Override
    public long getNextId(final String entityName, final long tenantId) throws SObjectNotFoundException {
        TenantSequenceManagerImpl mgr = this.sequenceManagers.get(tenantId);
        if (mgr == null) {
            synchronized (mutex) {
                mgr = this.sequenceManagers.get(tenantId);
                if (mgr == null) {
                    mgr = new TenantSequenceManagerImpl(tenantId, lockService, rangeSizes, defaultRangeSize, sequencesMappings, datasource, retries, delay,
                            delayFactor);
                    this.sequenceManagers.put(tenantId, mgr);
                }
            }
        }
        return mgr.getNextId(entityName);
    }

    @Override
    public void clear() {
        this.sequenceManagers.clear();
    }

    @Override
    public void close() {
        // Do nothing
    }

    @Override
    public void clear(final long tenantId) {
        this.sequenceManagers.remove(tenantId);
    }

}
