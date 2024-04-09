/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.web.rest.server.api.resource.CommonResource;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.restlet.resource.Get;

public class ActivityVariableResource extends CommonResource {

    public static final String ACTIVITYDATA_ACTIVITY_ID = "activityid";
    public static final String ACTIVITYDATA_DATA_NAME = "dataname";

    private final ProcessAPI processAPI;

    public ActivityVariableResource(final ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }

    @Get("json")
    public DataInstance getTaskVariable() {
        try {
            final String taskId = getAttribute(ACTIVITYDATA_ACTIVITY_ID);
            final String dataName = getAttribute(ACTIVITYDATA_DATA_NAME);
            return getTaskVariableInstance(dataName, Long.valueOf(taskId));
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    private DataInstance getTaskVariableInstance(final String dataName, final Long activityInstanceId)
            throws DataNotFoundException {
        return processAPI.getActivityDataInstance(dataName, activityInstanceId);
    }
}
