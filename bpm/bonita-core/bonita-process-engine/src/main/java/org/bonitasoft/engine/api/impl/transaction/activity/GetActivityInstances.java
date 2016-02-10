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
package org.bonitasoft.engine.api.impl.transaction.activity;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.persistence.OrderByType;

/**
 * @author Baptiste Mesta
 */
public class GetActivityInstances implements TransactionContentWithResult<List<SActivityInstance>> {

    private List<SActivityInstance> activities;

    private final ActivityInstanceService activityInstanceService;

    private final long processInstanceUUID;

    private final int pageIndex;

    private final int numberPerPage;

    private final String field;

    private final OrderByType order;

    public GetActivityInstances(final ActivityInstanceService activityInstanceService, final long processInstanceUUID, final int pageIndex,
            final int numberPerPage, final String field, final OrderByType order) {
        this.activityInstanceService = activityInstanceService;
        this.processInstanceUUID = processInstanceUUID;
        this.pageIndex = pageIndex;
        this.numberPerPage = numberPerPage;
        this.field = field;
        this.order = order;
    }

    @Override
    public void execute() throws SBonitaException {
        activities = activityInstanceService.getOpenActivityInstances(processInstanceUUID, pageIndex, numberPerPage, field, order);
    }

    @Override
    public List<SActivityInstance> getResult() {
        return activities;
    }

}
