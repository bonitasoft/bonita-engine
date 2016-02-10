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
package org.bonitasoft.engine.api.impl.transaction.flownode;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.QueryOptions;

/**
 * @author Elias Ricken de Medeiros
 */
public class SearchFlowNodeInstances<T extends SFlowNodeInstance> implements TransactionContentWithResult<List<T>> {

    private final FlowNodeInstanceService flowNodeInstanceService;

    private final QueryOptions queryOptions;

    private final Class<T> entityClass;

    private List<T> result;

    private long count;

    public SearchFlowNodeInstances(final FlowNodeInstanceService flowNodeInstanceService, final QueryOptions queryOptions,
            final Class<T> entityClass) {
        super();
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.queryOptions = queryOptions;
        this.entityClass = entityClass;
    }

    @Override
    public void execute() throws SBonitaException {
        result = flowNodeInstanceService.searchFlowNodeInstances(entityClass, queryOptions);
        final QueryOptions countOptions = new QueryOptions(0, 1, Collections.<OrderByOption> emptyList(), queryOptions.getFilters(),
                queryOptions.getMultipleFilter());
        count = flowNodeInstanceService.getNumberOfFlowNodeInstances(entityClass, countOptions);
    }

    @Override
    public List<T> getResult() {
        return result;
    }

    public long getCount() {
        return count;
    }

}
