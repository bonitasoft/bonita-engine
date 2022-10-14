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

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.data.ArchivedDataNotFoundException;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseVariable;
import org.bonitasoft.web.rest.server.api.resource.CommonResource;
import org.bonitasoft.web.toolkit.client.common.exception.api.APINotFoundException;
import org.restlet.resource.Get;

public class ArchivedCaseVariableResource extends CommonResource {

    static final String CASE_ID = "caseId";
    static final String VARIABLE_NAME = "variableName";

    private final ProcessAPI processAPI;

    public ArchivedCaseVariableResource(final ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }

    @Get("json")
    public ArchivedCaseVariable getArchivedCaseVariable() {
        var name = getAttribute(VARIABLE_NAME);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Attribute '" + VARIABLE_NAME + "' is mandatory in order to get the archived case variable");
        }
        try {
            var archivedProcessDataInstance = processAPI.getArchivedProcessDataInstance(name, getCaseIdParameter());
            return ArchivedCaseVariable.create(archivedProcessDataInstance);
        } catch (ArchivedDataNotFoundException e) {
            throw new APINotFoundException(e);
        }
    }

    private long getCaseIdParameter() {
        final String caseId = getAttribute(CASE_ID);
        if (caseId == null) {
            throw new IllegalArgumentException(
                    "Attribute '" + CASE_ID + "' is mandatory in order to get the archived case variable");
        }
        return Long.parseLong(caseId);
    }
}
