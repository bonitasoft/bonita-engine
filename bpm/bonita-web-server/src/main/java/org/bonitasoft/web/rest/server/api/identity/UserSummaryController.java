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
package org.bonitasoft.web.rest.server.api.identity;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.bonitasoft.web.rest.server.APIPaginationUtils.buildContentRange;
import static org.bonitasoft.web.rest.server.framework.utils.SearchOptionsBuilderUtil.computeIndex;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.web.rest.server.QueryParameterUtils;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller serving a lightweight, paginated projection of users (id, userName, firstname, lastname, job_title),
 * backed by {@link IdentityAPI#searchUsers(SearchOptions)}.
 */
@RestController
@RequestMapping("/API/identity/userSummary")
public class UserSummaryController extends AbstractRESTController {

    private static final String ENABLED_FILTER = "enabled";

    /** Maps a client-facing sort field to the engine descriptor field to sort on. */
    private static final Map<String, String> SORT_FIELDS = Map.of(
            "username", UserSearchDescriptor.USER_NAME,
            "firstname", UserSearchDescriptor.FIRST_NAME,
            "lastname", UserSearchDescriptor.LAST_NAME);

    /**
     * Returns a paginated page of user summaries.
     * <p>
     * Supported query parameters:
     * <ul>
     * <li>{@code p} (default 0): 0-indexed page number</li>
     * <li>{@code c} (default 10): page size</li>
     * <li>{@code s}: free-text search term, delegated to {@link IdentityAPI#searchUsers}'s default term matching</li>
     * <li>{@code o}: one or more comma-separated sort clauses over {@code username}, {@code firstname}
     * or {@code lastname}, applied in order (e.g. {@code lastname,firstname}), defaulting to {@code username ASC}</li>
     * <li>{@code f}: only the {@code enabled} filter is supported, e.g. {@code enabled=true}</li>
     * </ul>
     */
    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> searchUserSummaries(
            @RequestParam(value = "p") int page,
            @RequestParam(value = "c") int count,
            @RequestParam(value = "s", required = false) String search,
            @RequestParam(value = "o", required = false) String order,
            @RequestParam(value = "f", required = false) List<String> filters,
            HttpSession httpSession) throws BonitaException {

        IdentityAPI identityAPI = getIdentityAPI(httpSession);

        SearchOptions searchOptions = buildSearchOptions(page, count, search, order, filters);
        SearchResult<User> searchResult = identityAPI.searchUsers(searchOptions);

        List<User> users = searchResult.getResult();
        List<UserSummaryResponse> result = users.stream()
                .map(UserSummaryResponse::from)
                .collect(toList());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, buildContentRange(page, users.size(), searchResult.getCount()))
                .body(result);
    }

    // Options are built directly with SearchOptionsBuilder rather than via APIPaginationUtils.buildSearchOptions:
    // that shared helper is hardwired to form-mapping filters/sorts and cannot express the strict sort/filter
    // allowlist enforced here.
    private SearchOptions buildSearchOptions(int page, int count, String search, String order, List<String> filters) {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(computeIndex(page, count), count);
        if (search != null && !search.isBlank()) {
            builder.searchTerm(search);
        }
        applySort(builder, order);
        applyFilters(builder, filters);
        return builder.done();
    }

    /**
     * Applies the requested sort to the builder. Defaults to {@code username ASC} when no order is provided.
     * Accepts one or more comma-separated clauses, applied in order. Malformed clauses and unsupported fields raise
     * an {@link IllegalArgumentException}, which the global exception handler maps to HTTP 400.
     */
    private void applySort(SearchOptionsBuilder builder, String order) {
        if (order == null || order.isBlank()) {
            builder.sort(UserSearchDescriptor.USER_NAME, Order.ASC);
            return;
        }
        for (String clause : order.split(",")) {
            applySortClause(builder, clause);
        }
    }

    private void applySortClause(SearchOptionsBuilder builder, String clause) {
        String[] parts = clause.trim().split("\\s+");
        if (clause.isBlank() || parts.length > 2) {
            throw new IllegalArgumentException(
                    "A sort clause must be a field optionally followed by ASC or DESC, but got: " + clause);
        }
        String field = parts[0].toLowerCase(Locale.ROOT);
        Order direction = parts.length == 2 ? parseOrder(parts[1]) : Order.ASC;
        String engineField = SORT_FIELDS.get(field);
        if (engineField == null) {
            throw new IllegalArgumentException("Unsupported sort field: " + field
                    + ". Supported fields: " + SORT_FIELDS.keySet().stream().sorted().collect(joining(", ")));
        }
        builder.sort(engineField, direction);
    }

    private Order parseOrder(String value) {
        try {
            return Order.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sort order: " + value);
        }
    }

    /**
     * Applies supported filters. Only the {@code enabled} filter (boolean) is allowed; any other filter key or a
     * non-boolean value raises an {@link IllegalArgumentException} (HTTP 400).
     */
    private void applyFilters(SearchOptionsBuilder builder, List<String> filters) {
        if (filters == null || filters.isEmpty()) {
            return;
        }
        Map<String, String> parsedFilters = QueryParameterUtils.parseFilters(filters);
        // parseFilters collapses duplicate keys (one value silently wins) and drops malformed entries;
        // reject any such mismatch to stay consistent with the strict validation below.
        if (parsedFilters.size() != filters.size()) {
            throw new IllegalArgumentException("Invalid or duplicate filter keys: " + filters);
        }
        for (Map.Entry<String, String> filter : parsedFilters.entrySet()) {
            if (filter.getKey().isBlank()) {
                throw new IllegalArgumentException("Filter key must not be blank.");
            }
            if (!ENABLED_FILTER.equals(filter.getKey())) {
                throw new IllegalArgumentException(
                        "Unsupported filter: " + filter.getKey() + ". Only 'enabled' is supported.");
            }
            if (filter.getValue() == null) {
                throw new IllegalArgumentException("Filter 'enabled' must have a value: enabled=true|false");
            }
            builder.filter(UserSearchDescriptor.ENABLED, parseBoolean(filter.getValue()));
        }
    }

    private boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException("Filter 'enabled' must be 'true' or 'false', but got: " + value);
    }

}
