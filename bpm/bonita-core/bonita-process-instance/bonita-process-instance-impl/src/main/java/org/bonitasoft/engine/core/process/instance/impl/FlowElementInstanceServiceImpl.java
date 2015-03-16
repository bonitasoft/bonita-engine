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
package org.bonitasoft.engine.core.process.instance.impl;

import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.FlowElementInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementInstance;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.Recorder;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public abstract class FlowElementInstanceServiceImpl implements FlowElementInstanceService {

    private final Recorder recorder;

    private final ReadPersistenceService persistenceRead;

    public FlowElementInstanceServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceRead) {
        this.recorder = recorder;
        this.persistenceRead = persistenceRead;
    }

    @Override
    public long getNumberOfFlowElementInstances(final Class<? extends PersistentObject> entityClass, final QueryOptions countOptions)
            throws SBonitaReadException {
        return getPersistenceRead().getNumberOfEntities(entityClass, countOptions, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SFlowElementInstance> searchFlowElementInstances(final Class<? extends PersistentObject> entityClass, final QueryOptions searchOptions)
            throws SBonitaReadException {
        return (List<SFlowElementInstance>) getPersistenceRead().searchEntity(entityClass, searchOptions, null);
    }

    protected Recorder getRecorder() {
        return recorder;
    }

    protected ReadPersistenceService getPersistenceRead() {
        return persistenceRead;
    }

    //    @Override
    //    public List<SAFlowElementInstance> searchArchivedFlowElementInstances(final Class<? extends PersistentObject> entityClass, final QueryOptions searchOptions) {
    //        // FIXME Implement me!
    //        return null;
    //    }
    //
    //    @Override
    //    public long getNumberOfArchivedFlowElementInstances(final Class<? extends PersistentObject> entityClass, final QueryOptions countOptions) {
    //        // FIXME Implement me!
    //        return 0;
    //    }

}
