/**
 * Copyright (C) 2026 Bonitasoft S.A.
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

import static org.bonitasoft.web.rest.server.api.AbstractRESTController.API_SPRING_INTERNAL;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.bonitasoft.web.rest.server.FinderFactory;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API controller for retrieving case (process instance) execution context.
 * Provides access to process variables, business data, and other context elements.
 */
@RestController
@RequestMapping("/" + API_SPRING_INTERNAL + "/bpm/case/{caseId}/context")
public class CaseContextController extends AbstractRESTController {

    private final FinderFactory finderFactory = new FinderFactory();

    @GetMapping
    public Map<String, Serializable> getCaseContext(@PathVariable Long caseId, HttpSession httpSession)
            throws Exception {
        Map<String, Serializable> resultMap = new HashMap<>();
        Map<String, Serializable> caseExecutionContext = getProcessAPI(httpSession)
                .getProcessInstanceExecutionContext(caseId);

        for (Map.Entry<String, Serializable> entry : caseExecutionContext.entrySet()) {
            resultMap.put(entry.getKey(), finderFactory.getContextResultElement(entry.getValue()));
        }

        return resultMap;
    }
}
