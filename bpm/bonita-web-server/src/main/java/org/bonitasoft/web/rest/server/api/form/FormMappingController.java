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
package org.bonitasoft.web.rest.server.api.form;

import static java.util.stream.Collectors.toList;
import static org.bonitasoft.web.rest.server.APIPaginationUtils.buildContentRange;
import static org.bonitasoft.web.rest.server.APIPaginationUtils.buildSearchOptions;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller to search Form Mappings.
 * Note: Uses @ConditionalOnSingleCandidate to be registered only when no subclass is present.
 * In subscription builds, FormMappingControllerExt extends this class and takes precedence.
 *
 * @author Anthony Birembaut
 */
@RestController
@RequestMapping("/API/form/mapping")
@ConditionalOnSingleCandidate(FormMappingController.class)
public class FormMappingController extends AbstractRESTController {

    /**
     * Search for form mappings based on query parameters.
     *
     * @param page the page number (0-indexed)
     * @param count the number of results per page
     * @param search the search term
     * @param order the sort order (e.g., "id ASC")
     * @param filters the filters as a list of "key=value" strings
     * @param httpSession the HTTP session
     * @return the list of form mapping items with Content-Range header
     * @throws BonitaException if an error occurs during the search
     */
    @GetMapping
    public ResponseEntity<List<FormMappingItem>> searchFormMapping(
            @RequestParam(value = "p", defaultValue = "0") int page,
            @RequestParam(value = "c", defaultValue = "10") int count,
            @RequestParam(value = "s", required = false) String search,
            @RequestParam(value = "o", required = false) String order,
            @RequestParam(value = "f", required = false) List<String> filters,
            HttpSession httpSession) throws BonitaException {

        ProcessAPI processAPI = getProcessAPI(httpSession);

        SearchOptions searchOptions = buildSearchOptions(page, count, search, order, filters);
        SearchResult<?> searchResult = processAPI.searchFormMappings(searchOptions);

        List<FormMappingItem> result = searchResult.getResult().stream()
                .map(item -> new FormMappingItem((FormMapping) item))
                .collect(toList());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_RANGE,
                buildContentRange(page, searchResult.getResult().size(), searchResult.getCount()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(result);
    }

}
