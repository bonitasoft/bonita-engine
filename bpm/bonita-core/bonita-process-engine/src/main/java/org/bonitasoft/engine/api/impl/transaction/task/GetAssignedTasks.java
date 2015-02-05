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
package org.bonitasoft.engine.api.impl.transaction.task;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.persistence.OrderByType;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class GetAssignedTasks implements TransactionContentWithResult<List<SHumanTaskInstance>> {

    private final ActivityInstanceService instanceService;

    private final long userId;

    private final int fromIndex;

    private final int maxResults;

    private final String sortFieldName;

    private final OrderByType order;

    private List<SHumanTaskInstance> userTasks;

    public GetAssignedTasks(final ActivityInstanceService instanceService, final long userId, final int fromIndex, final int maxResults,
            final String sortFieldName, final OrderByType order) {
        super();
        this.instanceService = instanceService;
        this.userId = userId;
        this.fromIndex = fromIndex;
        this.maxResults = maxResults;
        this.sortFieldName = sortFieldName;
        this.order = order;
    }

    @Override
    public void execute() throws SBonitaException {
        userTasks = instanceService.getAssignedUserTasks(userId, fromIndex, maxResults, sortFieldName, order);
    }

    @Override
    public List<SHumanTaskInstance> getResult() {
        return userTasks;
    }

}
