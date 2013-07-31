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
package org.bonitasoft.engine.api.impl.transaction.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilder;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.process.SearchArchivedProcessInstances;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class GetNumberOfArchivedProcessInstance implements TransactionContentWithResult<Long> {

    private final ProcessInstanceService processInstanceService;

    private final SearchEntitiesDescriptor searchEntitiesDescriptor;

    private final SAProcessInstanceBuilder saProcessInstanceBuilder;

    private long number;

    public GetNumberOfArchivedProcessInstance(final ProcessInstanceService processInstanceService, final SearchEntitiesDescriptor searchEntitiesDescriptor,
            final SAProcessInstanceBuilder saProcessInstanceBuilder) {
        this.processInstanceService = processInstanceService;
        this.searchEntitiesDescriptor = searchEntitiesDescriptor;
        this.saProcessInstanceBuilder = saProcessInstanceBuilder;
    }

    @Override
    public void execute() throws SBonitaException {
        final SearchArchivedProcessInstances searchProcessInstances = new SearchArchivedProcessInstances(processInstanceService,
                searchEntitiesDescriptor.getArchivedProcessInstancesDescriptor(), null);
        final List<FilterOption> filterOptions = new ArrayList<FilterOption>(2);
        filterOptions.add(new FilterOption(SAProcessInstance.class, saProcessInstanceBuilder.getStateIdKey(), ProcessInstanceState.COMPLETED.getId()));
        filterOptions.add(new FilterOption(SAProcessInstance.class, saProcessInstanceBuilder.getCallerIdKey(), -1));
        final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS, Collections.<OrderByOption> emptyList(), filterOptions,
                null);
        number = searchProcessInstances.executeCount(queryOptions);
    }

    @Override
    public Long getResult() {
        return number;
    }

}
