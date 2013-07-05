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
package org.bonitasoft.engine.api.impl.transaction.process;

import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SAProcessInstanceNotFoundException;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.process.SearchArchivedProcessInstances;

/**
 * Returns the archived process instance from the SLIDING archive, if is started.
 * 
 * @author Celine Souchet
 */
public class GetStartedArchivedProcessInstance implements TransactionContentWithResult<ArchivedProcessInstance> {

    private final ProcessInstanceService processInstanceService;

    private final long processInstanceId;

    private final ReadPersistenceService readPersistenceService;

    private final SearchEntitiesDescriptor searchEntitiesDescriptor;

    private ArchivedProcessInstance archivedProcessInstance;

    public GetStartedArchivedProcessInstance(final ProcessInstanceService processInstanceService, final ReadPersistenceService readPersistenceService,
            final SearchEntitiesDescriptor searchEntitiesDescriptor, final long processInstanceId) {
        this.processInstanceService = processInstanceService;
        this.processInstanceId = processInstanceId;
        this.readPersistenceService = readPersistenceService;
        this.searchEntitiesDescriptor = searchEntitiesDescriptor;
    }

    @Override
    public void execute() throws SBonitaException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 2);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.ARCHIVE_DATE, Order.ASC);
        searchOptionsBuilder.filter(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, processInstanceId);
        searchOptionsBuilder.filter(ArchivedProcessInstancesSearchDescriptor.STATE_ID, ProcessInstanceState.STARTED.getId());
        final SearchArchivedProcessInstances searchArchivedProcessInstances = new SearchArchivedProcessInstances(processInstanceService,
                searchEntitiesDescriptor.getArchivedProcessInstancesDescriptor(), searchOptionsBuilder.done(), readPersistenceService);
        searchArchivedProcessInstances.execute();

        try {
            archivedProcessInstance = searchArchivedProcessInstances.getResult().getResult().get(0);
        } catch (final IndexOutOfBoundsException e) {
            throw new SAProcessInstanceNotFoundException(processInstanceId, ProcessInstanceState.STARTED.name());
        }
    }

    @Override
    public ArchivedProcessInstance getResult() {
        return archivedProcessInstance;
    }

}
