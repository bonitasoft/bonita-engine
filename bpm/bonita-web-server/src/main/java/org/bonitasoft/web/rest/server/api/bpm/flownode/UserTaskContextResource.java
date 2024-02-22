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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.UserTaskNotFoundException;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.web.rest.server.FinderFactory;
import org.bonitasoft.web.rest.server.api.resource.CommonResource;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.restlet.resource.Get;

public class UserTaskContextResource extends CommonResource {

    static final String TASK_ID = "taskId";

    private final ProcessAPI processAPI;
    private final FinderFactory resourceHandler;

    public UserTaskContextResource(final ProcessAPI processAPI, FinderFactory resourceHandler) {
        this.processAPI = processAPI;
        this.resourceHandler = resourceHandler;
    }

    @Get("json")
    public Map<String, Serializable> getUserTaskContext()
            throws UserTaskNotFoundException, ExpressionEvaluationException {
        final Map<String, Serializable> resultMap = new HashMap<>();

        Map<String, Serializable> userTaskExecutionContext = processAPI
                .getUserTaskExecutionContext(getTaskIdParameter());

        for (Map.Entry<String, Serializable> executionContextElement : userTaskExecutionContext.entrySet()) {
            resultMap.put(executionContextElement.getKey(),
                    getContextResultElement(executionContextElement.getValue()));
        }
        return resultMap;
    }

    private Serializable getContextResultElement(Serializable executionContextElementValue) {
        return resourceHandler.getContextResultElement(executionContextElementValue);
    }

    protected long getTaskIdParameter() {
        final String taskId = getAttribute(TASK_ID);
        if (taskId == null) {
            throw new APIException("Attribute '" + TASK_ID + "' is mandatory in order to get the task context");
        }
        return Long.parseLong(taskId);
    }
}
