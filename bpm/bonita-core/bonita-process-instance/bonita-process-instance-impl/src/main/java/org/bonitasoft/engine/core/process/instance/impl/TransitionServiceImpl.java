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

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.TransitionState;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionDeletionException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SATransitionInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SATransitionInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;

/**
 * @author Zhao Na
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class TransitionServiceImpl implements TransitionService {

    private final ReadPersistenceService persistenceRead;

    private final ArchiveService archiveService;

    public TransitionServiceImpl(final ReadPersistenceService persistenceRead, final ArchiveService archiveService) {
        this.persistenceRead = persistenceRead;
        this.archiveService = archiveService;
    }

    @Override
    public long getNumberOfArchivedTransitionInstances(final QueryOptions countOptions) throws SBonitaReadException {
        return persistenceRead.getNumberOfEntities(SATransitionInstance.class, countOptions, null);
    }

    @Override
    public List<SATransitionInstance> searchArchivedTransitionInstances(final QueryOptions queryOptions) throws SBonitaReadException {
        return persistenceRead.searchEntity(SATransitionInstance.class, queryOptions, null);
    }

    @Override
    public void archive(final STransitionDefinition sTransitionDefinition, final SFlowNodeInstance sFlowNodeInstance, final TransitionState transitionState)
            throws STransitionCreationException {
        final SATransitionInstance saTransitionInstance = BuilderFactory.get(SATransitionInstanceBuilderFactory.class)
                .createNewTransitionInstance(sTransitionDefinition, sFlowNodeInstance, transitionState).done();
        final long archiveDate = System.currentTimeMillis();
        try {
            archiveTransitionInstanceInsertRecord(saTransitionInstance, archiveDate);
        } catch (final SRecorderException e) {
            throw new STransitionCreationException(e);
        }

    }

    private void archiveTransitionInstanceInsertRecord(final SATransitionInstance saTransitionInstance, final long archiveDate) throws SRecorderException {
        final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saTransitionInstance);
        archiveService.recordInsert(archiveDate, insertRecord);
    }

    @Override
    public void delete(final SATransitionInstance saTransitionInstance) throws STransitionDeletionException {
        final DeleteRecord deleteRecord = new DeleteRecord(saTransitionInstance);
        try {
            archiveService.recordDelete(deleteRecord);
        } catch (final SRecorderException e) {
            throw new STransitionDeletionException(e);
        }
    }

    @Override
    public void deleteArchivedTransitionsOfProcessInstance(final long processInstanceId) throws STransitionDeletionException, SBonitaReadException {
        final SATransitionInstanceBuilderFactory saTransitionInstanceBuilder = BuilderFactory.get(SATransitionInstanceBuilderFactory.class);
        final String rootContainerIdKey = saTransitionInstanceBuilder.getRootContainerIdKey();
        final String idKey = saTransitionInstanceBuilder.getIdKey();

        List<SATransitionInstance> transitionInstances;
        do {
            final List<FilterOption> filters = Collections.singletonList(new FilterOption(SATransitionInstance.class, rootContainerIdKey, processInstanceId));
            final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SATransitionInstance.class, idKey, OrderByType.ASC));
            final QueryOptions queryOptions = new QueryOptions(0, 10, orderByOptions, filters, null);
            transitionInstances = searchArchivedTransitionInstances(queryOptions);

            for (final SATransitionInstance saTransitionInstance : transitionInstances) {
                delete(saTransitionInstance);
            }
        } while (!transitionInstances.isEmpty());
    }
}
