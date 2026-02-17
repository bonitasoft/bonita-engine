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

import static org.bonitasoft.web.rest.server.APIPaginationUtils.buildContentRange;
import static org.bonitasoft.web.rest.server.QueryParameterUtils.extractLongFilterFromFilterList;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseVariable;
import org.bonitasoft.web.rest.model.bpm.cases.CaseVariableItem;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/API/bpm/archivedCaseVariable")
public class ArchivedCaseVariableController extends AbstractRESTController {

    @GetMapping("/{caseId}/{variableName}")
    public ArchivedCaseVariable getArchivedCaseVariable(
            @PathVariable long caseId,
            @PathVariable String variableName,
            HttpSession session) throws Exception {
        var archivedProcessDataInstance = getProcessAPI(session)
                .getArchivedProcessDataInstance(variableName, caseId);
        return ArchivedCaseVariable.create(archivedProcessDataInstance);
    }

    @GetMapping
    public ResponseEntity<List<ArchivedCaseVariable>> getArchivedCaseVariables(
            @RequestParam("p") int page,
            @RequestParam("c") int count,
            @RequestParam(value = "f", required = false) List<String> filters,
            HttpSession session) throws Exception {
        long caseId = extractLongFilterFromFilterList(filters, CaseVariableItem.ATTRIBUTE_CASE_ID);
        List<ArchivedDataInstance> allResults = getProcessAPI(session)
                .getArchivedProcessDataInstances(caseId, 0, Integer.MAX_VALUE);
        List<ArchivedCaseVariable> pagedResults = allResults.stream()
                .skip((long) page * count)
                .limit(count)
                .map(ArchivedCaseVariable::create)
                .toList();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_RANGE, buildContentRange(page, count, allResults.size()))
                .body(pagedResults);
    }

}
