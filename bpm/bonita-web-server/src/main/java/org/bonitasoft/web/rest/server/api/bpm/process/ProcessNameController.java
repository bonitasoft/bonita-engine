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
package org.bonitasoft.web.rest.server.api.bpm.process;

import static java.util.stream.Collectors.toList;
import static org.bonitasoft.web.rest.server.APIPaginationUtils.buildContentRange;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessNameInfo;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.web.rest.server.QueryParameterUtils;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.bonitasoft.web.rest.server.datastore.bpm.process.helper.ProcessSearchDescriptorConverter;
import org.bonitasoft.web.rest.server.datastore.filter.Filters;
import org.bonitasoft.web.rest.server.datastore.filter.GenericFilterCreator;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.rest.server.datastore.utils.Sorts;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller to search process deployment infos grouped by (name, displayName).
 * Each result is a distinct (name, displayName) combination with the list of its deployed versions.
 *
 * @author Anthony Birembaut
 */
@RestController
@RequestMapping("/API/bpm/processName")
public class ProcessNameController extends AbstractRESTController {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of(
            ProcessDeploymentInfoSearchDescriptor.DISPLAY_NAME, ProcessDeploymentInfoSearchDescriptor.NAME);

    private static final String DEFAULT_ORDER = ProcessDeploymentInfoSearchDescriptor.DISPLAY_NAME + " ASC";

    /**
     * Searches process deployment infos grouped by (name, displayName).
     * <p>
     * The search term ({@code s=}) selects which groups are returned (matching on name, displayName or version) but is
     * not applied per version: a group's version list always contains all of its versions allowed by the
     * activationState filter, even those that did not individually match the term. The only supported filter is
     * {@code f=activationState=...} (other filters are ignored); an unknown value is rejected with HTTP 400. Ordering
     * ({@code o=}) is restricted to {@code displayName} and {@code name} and defaults to {@code displayName ASC}; a
     * single sort clause is applied, so a compound order such as {@code displayName ASC, name DESC} is rejected with
     * HTTP 400.
     */
    @GetMapping
    public ResponseEntity<List<ProcessNameResponse>> searchProcessNames(
            @RequestParam(value = "p") int page,
            @RequestParam(value = "c") int count,
            @RequestParam(value = "s", required = false) String search,
            @RequestParam(value = "o", required = false) String order,
            @RequestParam(value = "f", required = false) List<String> filters,
            HttpSession httpSession) throws BonitaException {

        ProcessAPI processAPI = getProcessAPI(httpSession);

        Map<String, String> parsedFilters = QueryParameterUtils.parseFilters(filters);
        validateActivationState(parsedFilters);
        ProcessSearchDescriptorConverter converter = new ProcessSearchDescriptorConverter();
        SearchOptions searchOptions = new SearchOptionsCreator(page, count, search,
                new Sorts(validateOrder(order), converter),
                new Filters(parsedFilters, new GenericFilterCreator(converter))).create();

        SearchResult<ProcessNameInfo> searchResult = processAPI.searchProcessNames(searchOptions);

        List<ProcessNameResponse> result = searchResult.getResult().stream()
                .map(ProcessNameResponse::from)
                .collect(toList());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, buildContentRange(page, result.size(), searchResult.getCount()))
                .body(result);
    }

    /**
     * Defaults the order to displayName ASC and rejects ordering on anything other than displayName or name, since
     * the grouped query selects only those columns (ordering on other fields is not portable with SELECT DISTINCT).
     * Compound orders are rejected too: the resource applies a single sort, so accepting extra clauses that are then
     * ignored would be a misleading contract.
     */
    private static String validateOrder(final String order) {
        if (order == null || order.isBlank()) {
            return DEFAULT_ORDER;
        }
        final String[] clauses = order.split(",");
        if (clauses.length > 1) {
            throw new IllegalArgumentException(
                    "Compound sort is not supported; specify a single sort on displayName or name.");
        }
        final String field = clauses[0].trim().split("\\s+")[0];
        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new IllegalArgumentException("Unsupported sort field '" + field
                    + "'. Only " + ALLOWED_SORT_FIELDS + " are supported for this resource.");
        }
        return order;
    }

    /**
     * Rejects an activationState filter value that is not a real {@link ActivationState}, giving a 400 (rather than a
     * silently empty result) for an unknown or wrong-case value, symmetrically with {@link #validateOrder(String)}.
     */
    private static void validateActivationState(final Map<String, String> filters) {
        if (filters == null) {
            return;
        }
        final String value = filters.get(ProcessDeploymentInfoSearchDescriptor.ACTIVATION_STATE);
        if (value == null) {
            return;
        }
        for (final ActivationState state : ActivationState.values()) {
            if (state.name().equals(value)) {
                return;
            }
        }
        throw new IllegalArgumentException("Unsupported activationState filter value '" + value
                + "'. Supported values: " + Arrays.toString(ActivationState.values()) + ".");
    }

}
