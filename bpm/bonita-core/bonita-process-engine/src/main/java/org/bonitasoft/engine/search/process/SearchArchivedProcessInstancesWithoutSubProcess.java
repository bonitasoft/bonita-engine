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
package org.bonitasoft.engine.search.process;

import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.search.AbstractArchivedProcessInstanceSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

/**
 * @author Yanyan Liu
 * @author Baptiste Mesta
 */
public class SearchArchivedProcessInstancesWithoutSubProcess extends AbstractArchivedProcessInstanceSearchEntity {

    private final ProcessInstanceService processInstanceService;

    private final ReadPersistenceService persistenceService;

    public SearchArchivedProcessInstancesWithoutSubProcess(final ProcessInstanceService processInstanceService, final SearchEntityDescriptor searchDescriptor,
            final SearchOptions options, final ReadPersistenceService persistenceService) {
        super(searchDescriptor, options);
        this.processInstanceService = processInstanceService;
        this.persistenceService = persistenceService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaSearchException {
        return processInstanceService.getNumberOfArchivedProcessInstancesWithoutSubProcess(searchOptions, persistenceService);
    }

    @Override
    public List<SAProcessInstance> executeSearch(final QueryOptions searchOptions) throws SBonitaSearchException {
        return processInstanceService.searchArchivedProcessInstancesWithoutSubProcess(searchOptions, persistenceService);
    }

}
