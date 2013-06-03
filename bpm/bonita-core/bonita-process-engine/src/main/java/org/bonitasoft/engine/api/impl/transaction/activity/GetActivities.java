/**
 * Copyright (C) 2011 BonitaSoft S.A.
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

import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public final class GetActivities implements TransactionContentWithResult<List<SActivityInstance>> {

    private final long processInstanceId;

    private final ActivityInstanceService activityInstanceService;

    private final int fromIndex;

    private final int maxResults;

    private List<SActivityInstance> activityInstances;

    public GetActivities(final long processInstanceId, final int fromIndex, final int maxResults, final ActivityInstanceService activityInstanceService) {
        this.processInstanceId = processInstanceId;
        this.activityInstanceService = activityInstanceService;
        this.fromIndex = fromIndex;
        this.maxResults = maxResults;
    }

    @Override
    public void execute() throws SActivityReadException {
        activityInstances = activityInstanceService.getActivityInstances(processInstanceId, fromIndex, maxResults, null, null);
    }

    @Override
    public List<SActivityInstance> getResult() {
        return activityInstances;
    }

}
