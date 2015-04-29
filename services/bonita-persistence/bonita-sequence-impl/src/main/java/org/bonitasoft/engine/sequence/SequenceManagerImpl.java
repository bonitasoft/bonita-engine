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
import java.util.List;
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

    private final SequenceMappingProvider sequenceMappingProvider;

    private final int retries;

    private final int delay;

    private final int delayFactor;

    private final DataSource datasource;

    private final LockService lockService;

    private final Map<Long, TenantSequenceManagerImpl> sequenceManagers = new HashMap<Long, TenantSequenceManagerImpl>();

    private final Object mutex = new SequenceManagerImplMutex();

    public SequenceManagerImpl(final LockService lockService,
            final SequenceMappingProvider sequenceMappingProvider,
            final DataSource datasource, final int retries, final int delay, final int delayFactor) {
        this.lockService = lockService;
        this.sequenceMappingProvider = sequenceMappingProvider;
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
                    mgr = new TenantSequenceManagerImpl(tenantId, lockService, getSequenceIdToRangeSizeMap(), getClassNameToSequenceIdMap(), datasource, retries, delay,
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

    private Map<String, Long> getClassNameToSequenceIdMap() {
        final Map<String, Long> result = new HashMap<>();
        for (SequenceMapping sequenceMapping : sequenceMappingProvider.getSequenceMappings()) {
            for (String className : sequenceMapping.getClassNames()) {
                result.put(className, sequenceMapping.getSequenceId());
            }
        }
        return result;
    }

    private Map<Long, Integer> getSequenceIdToRangeSizeMap() {
        final Map<Long, Integer> result = new HashMap<>();
        for (SequenceMapping sequenceMapping : sequenceMappingProvider.getSequenceMappings()) {
            final long sequenceId = sequenceMapping.getSequenceId();
            if (result.containsKey(sequenceId)) {
                throw new RuntimeException("SequenceMapping for id <" + sequenceId + "> is duplicated. Please make sure there is only one configuration for this sequence.");
            }
            result.put(sequenceId, sequenceMapping.getRangeSize());
        }
        return result;
    }

}
