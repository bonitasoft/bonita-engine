/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowElementInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.recorder.Recorder;

/**
 * @author Emmanuel Duchastenier
 */
public abstract class FlowElementInstanceServiceImpl implements FlowElementInstanceService {

    protected final BPMInstanceBuilders instanceBuilders;

    // private final EventService eventService;

    private final Recorder recorder;

    private final ReadPersistenceService persistenceRead;

    public FlowElementInstanceServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceRead, final EventService eventService,
            final BPMInstanceBuilders instanceBuilders) {
        this.recorder = recorder;
        this.persistenceRead = persistenceRead;
        this.instanceBuilders = instanceBuilders;
        // this.eventService = eventService;
    }

    @Override
    public long getNumberOfFlowElementInstances(final Class<? extends PersistentObject> entityClass, final QueryOptions countOptions)
            throws SBonitaSearchException {
        try {
            return getPersistenceRead().getNumberOfEntities(entityClass, countOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SFlowElementInstance> searchFlowElementInstances(final Class<? extends PersistentObject> entityClass, final QueryOptions searchOptions)
            throws SBonitaSearchException {
        try {
            return (List<SFlowElementInstance>) getPersistenceRead().searchEntity(entityClass, searchOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    protected Recorder getRecorder() {
        return recorder;
    }

    protected ReadPersistenceService getPersistenceRead() {
        return persistenceRead;
    }

    protected BPMInstanceBuilders getInstanceBuilders() {
        return instanceBuilders;
    }

    @Override
    public List<SAFlowElementInstance> searchArchivedFlowElementInstances(final Class<? extends PersistentObject> entityClass, final QueryOptions searchOptions)
            throws SBonitaSearchException {
        // TODO Implement me!
        return null;
    }

    @Override
    public long getNumberOfArchivedFlowElementInstances(final Class<? extends PersistentObject> entityClass, final QueryOptions countOptions)
            throws SBonitaSearchException {
        // TODO Implement me!
        return 0;
    }

}
