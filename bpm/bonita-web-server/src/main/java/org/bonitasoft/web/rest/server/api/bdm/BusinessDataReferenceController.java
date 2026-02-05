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
import org.bonitasoft.engine.business.data.MultipleBusinessDataReference;
import org.bonitasoft.engine.business.data.SimpleBusinessDataReference;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.web.rest.server.BonitaRestletApplication;
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
        return toClient(reference);
    }

    /**
     * Gets all business data references for a process instance with pagination.
     *
     * @param caseId the process instance ID (required filter)
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

        Long caseId = extractCaseIdFromFilters(filters);
        List<BusinessDataReference> references = getBusinessDataAPI(httpSession)
                .getProcessBusinessDataReferences(caseId, page * count, count);
        return references.stream()
                .map(BusinessDataReferenceController::toClient)
                .toList();
    }

    /**
     * Extracts the caseId from the filter list.
     *
     * @param filters the list of filter strings in format "key=value"
     * @return the caseId value
     * @throws IllegalArgumentException if caseId filter is missing or not a number
     */
    private Long extractCaseIdFromFilters(List<String> filters) {
        if (filters == null) {
            throw new IllegalArgumentException("filter caseId is mandatory");
        }
        for (String filter : filters) {
            if (filter.startsWith("caseId=")) {
                String value = filter.substring("caseId=".length());
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("filter caseId should be a number");
                }
            }
        }
        throw new IllegalArgumentException("filter caseId is mandatory");
    }

    /**
     * Converts a BusinessDataReference to its client representation.
     *
     * @param reference the business data reference
     * @return the client representation with appropriate URL and storage ID fields
     */
    public static BusinessDataReferenceClient toClient(BusinessDataReference reference) {
        if (reference instanceof SimpleBusinessDataReference simpleReference) {
            return new SimpleBusinessDataReferenceClient(
                    reference.getName(),
                    reference.getType(),
                    getUrl(reference.getType(), getStorageIdString(simpleReference)),
                    simpleReference.getStorageId());
        } else {
            MultipleBusinessDataReference multipleReference = (MultipleBusinessDataReference) reference;
            return new MultipleBusinessDataReferenceClient(
                    reference.getName(),
                    reference.getType(),
                    getUrl(multipleReference.getType(), getStorageIdsValue(multipleReference)),
                    multipleReference.getStorageIds());
        }
    }

    private static String getStorageIdString(SimpleBusinessDataReference reference) {
        Long storageId = reference.getStorageId();
        if (storageId != null) {
            return storageId.toString();
        }
        return "";
    }

    private static String getStorageIdsValue(MultipleBusinessDataReference reference) {
        return "findByIds?ids=" + reference.getStorageIds().toString().replaceAll("[\\[\\] ]", "");
    }

    private static String getUrl(String type, String value) {
        return "API" + BonitaRestletApplication.BDM_BUSINESS_DATA_URL + "/" + type + "/" + value;
    }

}
