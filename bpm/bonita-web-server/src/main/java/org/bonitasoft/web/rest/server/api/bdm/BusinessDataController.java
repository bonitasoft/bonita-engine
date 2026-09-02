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

import static org.bonitasoft.web.rest.server.QueryParameterUtils.parseFilters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.bpm.businessdata.BusinessDataQueryResult;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/API/bdm/businessData/{className}", produces = MediaType.APPLICATION_JSON_VALUE)
public class BusinessDataController extends AbstractRESTController {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // For the "f" parameters in the request URL of a BDM query REST API call, register a CustomCollectionEditor
        // in the data binder so that when the BDM query has a single multi-valued parameter,
        // the string of the value won't be split into a list of values separated by a comma (',').
        //
        // BEWARE: we only register the CustomCollectionEditor for request parameter "f" or else it would break
        // the /findByIds?ids=<id1>,<id2> endpoint.
        // (==> It would cause the list of "ids" to be parsed as a single parameter value  “<id1>,<id2>”
        // instead of two values “<id1>” and “<id2>”, which would result in an IllegalArgumentException.)
        if ("f".equals(binder.getObjectName())) {
            binder.registerCustomEditor(List.class, new CustomCollectionEditor(List.class));
        }
    }

    @GetMapping("/{id}")
    public String getBusinessData(
            @PathVariable("className") String className,
            @PathVariable("id") Long id,
            HttpSession session)
            throws BonitaException {
        return getBusinessData(className, id, null, session);
    }

    @GetMapping("/{id}/{fieldName}")
    public String getBusinessData(
            @PathVariable("className") String className,
            @PathVariable("id") Long id,
            @PathVariable("fieldName") String fieldName,
            HttpSession session)
            throws BonitaException {
        final Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("entityClassName", className);
        parameters.put("businessDataId", id);
        parameters.put("businessDataURIPattern", BusinessDataFieldValue.URI_PATTERN);
        if (fieldName != null) {
            parameters.put("businessDataChildName", fieldName);
        }
        return (String) getCommandAPI(session).execute("getBusinessDataById", parameters);
    }

    @GetMapping("/findByIds")
    public String getBusinessData(
            @PathVariable("className") String className,
            @RequestParam("ids") List<Long> ids,
            HttpSession session)
            throws BonitaException {

        final Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("entityClassName", className);
        parameters.put("businessDataIds", (Serializable) ids);
        parameters.put("businessDataURIPattern", BusinessDataFieldValue.URI_PATTERN);
        return (String) getCommandAPI(session).execute("getBusinessDataByIds", parameters);
    }

    @GetMapping("")
    public ResponseEntity<String> getBusinessDataByQuery(@PathVariable("className") String className,
            @RequestParam("c") Integer searchPageSize,
            @RequestParam(value = "f", required = false) List<String> filters,
            @RequestParam("p") Integer searchPageNumber,
            @RequestParam("q") String queryName,
            HttpSession session)
            throws BonitaException {
        final Map<String, Serializable> parameters = new HashMap<>();

        parameters.put("queryName", queryName);
        parameters.put("queryParameters", (Serializable) parseFilters(filters));
        parameters.put("entityClassName", className);
        parameters.put("startIndex", searchPageNumber * searchPageSize);
        parameters.put("maxResults", searchPageSize);
        parameters.put("businessDataURIPattern", BusinessDataFieldValue.URI_PATTERN);

        if (log.isDebugEnabled()) {
            log.debug("Executing business Data Query: {}", parameters.get("queryName"));
            log.debug("entityClassName: {}", parameters.get("entityClassName"));
            log.debug("queryParameters: {}", parameters.get("queryParameters").toString());
            log.debug("startIndex: {}", parameters.get("startIndex"));
            log.debug("maxResults: {}", parameters.get("maxResults"));
        }

        BusinessDataQueryResult businessDataQueryResult = (BusinessDataQueryResult) getCommandAPI(session).execute(
                "getBusinessDataByQueryCommand",
                parameters);

        var json = (String) businessDataQueryResult.getJsonResults();

        // Build response with proper headers
        final var businessDataQueryMetadata = businessDataQueryResult.getBusinessDataQueryMetadata();
        Long totalCount = null;
        if (businessDataQueryMetadata != null) {
            totalCount = businessDataQueryMetadata.getCount();
        }
        HttpHeaders headers = buildHttpHeaders(searchPageSize, searchPageNumber, totalCount);

        return new ResponseEntity<>(json, headers, HttpStatus.OK);
    }

    private static HttpHeaders buildHttpHeaders(Integer searchPageSize, Integer searchPageNumber, Long totalCount) {
        HttpHeaders headers = new HttpHeaders();
        if (totalCount != null) {
            // Format content range header similar to what is produced by APIServletCall.doGet and CommonResource.setContentRange
            // Our API is not conform to the Content-range header specs
            String contentRangeValue = searchPageNumber + "-" + searchPageSize + "/" + totalCount;
            headers.add(HttpHeaders.CONTENT_RANGE, contentRangeValue);
        }
        return headers;
    }

}
