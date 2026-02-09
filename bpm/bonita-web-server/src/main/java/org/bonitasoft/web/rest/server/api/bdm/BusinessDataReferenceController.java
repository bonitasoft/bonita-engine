/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.bdm;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.business.data.BusinessDataReference;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.web.rest.server.QueryParameterUtils;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring MVC controller for BusinessDataReference REST API.
 * <p>
 * Provides endpoints to retrieve business data references for process instances.
 * </p>
 *
 * @author Matthieu Chaffotte
 * @author Colin Puy
 */
@RestController
@RequestMapping(path = "/API/bdm/businessDataReference", produces = MediaType.APPLICATION_JSON_VALUE)
public class BusinessDataReferenceController extends AbstractRESTController {

    /**
     * Gets a specific business data reference by case ID and data name.
     *
     * @param caseId the process instance ID
     * @param dataName the name of the business data variable
     * @param httpSession the HTTP session
     * @return the business data reference as a client object
     * @throws DataNotFoundException if the business data reference is not found
     * @throws BonitaException if an error occurs while accessing the API
     */
    @GetMapping("/{caseId}/{dataName}")
    public BusinessDataReferenceClient getProcessBusinessDataReference(
            @PathVariable Long caseId,
            @PathVariable String dataName,
            HttpSession httpSession) throws DataNotFoundException, BonitaException {
        BusinessDataReference reference = getBusinessDataAPI(httpSession)
                .getProcessBusinessDataReference(dataName, caseId);
        return BusinessDataReferenceConverter.toClient(reference);
    }

    /**
     * Gets all business data references for a process instance with pagination.
     * <p>
     * Example:
     *
     * <pre>
     * f=caseId=123&p=0&c=10
     * </pre>
     *
     * </p>
     *
     * @param filters standard filter set that MUST contain a filter named caseId and containing the process instance
     *        ID:
     *
     *        <pre>
     *        f = caseId = 123
     *        </pre>
     *
     * @param page the page number (0-based)
     * @param count the number of results per page
     * @param httpSession the HTTP session
     * @return a list of business data references as client objects
     * @throws BonitaException if an error occurs while accessing the API
     */
    @GetMapping
    public List<BusinessDataReferenceClient> getProcessBusinessDataReferences(
            @RequestParam("f") List<String> filters,
            @RequestParam("p") Integer page,
            @RequestParam("c") Integer count,
            HttpSession httpSession) throws BonitaException {

        Long caseId = QueryParameterUtils.extractLongFilterFromFilterList(filters, "caseId");
        List<BusinessDataReference> references = getBusinessDataAPI(httpSession)
                .getProcessBusinessDataReferences(caseId, page * count, count);
        return references.stream()
                .map(BusinessDataReferenceConverter::toClient)
                .toList();
    }

}
