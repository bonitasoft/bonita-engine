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
package org.bonitasoft.engine.api.impl.transaction.process;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.persistence.OrderByType;

/**
 * @author Yanyan Liu
 */
public class GetChildInstanceIdsOfProcessInstance implements TransactionContentWithResult<List<Long>> {

    private final ProcessInstanceService processInstanceService;

    private final long processInstanceId;

    private final int pageIndex;

    private final int numberPerPage;

    private final String sortingField;

    private final OrderByType sortingOrder;

    private List<Long> childInstanceIds;

    public GetChildInstanceIdsOfProcessInstance(final ProcessInstanceService processInstanceService, final long processInstanceId, final int pageIndex,
            final int numberPerPage, final String field, final OrderByType order) {
        this.processInstanceService = processInstanceService;
        this.processInstanceId = processInstanceId;
        this.pageIndex = pageIndex;
        this.numberPerPage = numberPerPage;
        this.sortingField = field;
        this.sortingOrder = order;
    }

    @Override
    public void execute() throws SBonitaException {
        this.childInstanceIds = this.processInstanceService.getChildInstanceIdsOfProcessInstance(this.processInstanceId, this.pageIndex * this.numberPerPage,
                this.numberPerPage, this.sortingField, this.sortingOrder);
    }

    @Override
    public List<Long> getResult() {
        return this.childInstanceIds;
    }

}
