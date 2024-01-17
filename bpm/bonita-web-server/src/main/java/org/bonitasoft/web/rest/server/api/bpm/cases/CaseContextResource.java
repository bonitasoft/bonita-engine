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
package org.bonitasoft.web.rest.server.api.bpm.cases;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.web.rest.server.FinderFactory;
import org.bonitasoft.web.rest.server.api.resource.CommonResource;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.restlet.resource.Get;

/**
 * Created by Fabio Lombardi
 */
public class CaseContextResource extends CommonResource {

    static final String CASE_ID = "caseId";

    private final ProcessAPI processAPI;
    private final FinderFactory resourceHandler;

    public CaseContextResource(final ProcessAPI processAPI, FinderFactory resourceHandler) {
        this.processAPI = processAPI;
        this.resourceHandler = resourceHandler;
    }

    @Get("json")
    public Map<String, Serializable> getCaseContext()
            throws ExpressionEvaluationException, ProcessInstanceNotFoundException {
        final Map<String, Serializable> resultMap = new HashMap<>();

        Map<String, Serializable> caseExecutionContext = processAPI
                .getProcessInstanceExecutionContext(getCaseIdParameter());

        for (Map.Entry<String, Serializable> caseContextElement : caseExecutionContext.entrySet()) {
            resultMap.put(caseContextElement.getKey(), getContextResultElement(caseContextElement.getValue()));
        }
        return resultMap;
    }

    private Serializable getContextResultElement(Serializable executionContextElementValue) {
        return resourceHandler.getContextResultElement(executionContextElementValue);
    }

    protected long getCaseIdParameter() {
        final String caseId = getAttribute(CASE_ID);
        if (caseId == null) {
            throw new APIException("Attribute '" + CASE_ID + "' is mandatory in order to get the case context");
        }
        return Long.parseLong(caseId);
    }
}
