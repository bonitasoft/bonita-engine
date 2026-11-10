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
package org.bonitasoft.engine.search.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessNameInfo;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessNameInfoImpl;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.ProcessNameGroupQuery;
import org.bonitasoft.engine.core.process.definition.model.ProcessNameKey;
import org.bonitasoft.engine.core.process.definition.model.ProcessNameVersion;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.Sort;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.engine.search.impl.SearchResultImpl;

/**
 * Searches process deployment infos grouped by (name, displayName), paginated and ordered at the database level.
 * <p>
 * Three database round-trips, all paginated/bounded at the DB:
 * <ol>
 * <li>count the distinct (name, displayName) groups matching the optional activationState filter and search term,</li>
 * <li>fetch the requested page of (name, displayName) keys (ordered by displayName or name, ASC or DESC),</li>
 * <li>fetch the versions of the page's names (same activationState filter) and group them by (name, displayName).</li>
 * </ol>
 * The search term selects which groups appear; the version lists then contain every version of those groups matching
 * the activationState filter. Only the activationState filter and ordering on displayName/name are supported.
 *
 * @author Anthony Birembaut
 */
public class SearchProcessNames {

    /** Matches the {@code ESCAPE '#'} clause declared in the grouped-search named queries. */
    private static final String LIKE_ESCAPE = "#";

    private final ProcessDefinitionService processDefinitionService;

    private final SearchOptions options;

    public SearchProcessNames(final ProcessDefinitionService processDefinitionService, final SearchOptions options) {
        this.processDefinitionService = processDefinitionService;
        this.options = options;
    }

    public SearchResult<ProcessNameInfo> search() throws SearchException {
        try {
            final ProcessNameGroupQuery query = toQuery();
            final long count = processDefinitionService.getNumberOfProcessNameGroups(query.activationState(),
                    query.searchTerm());
            List<ProcessNameInfo> result = new ArrayList<>();
            if (count > 0 && query.maxResults() != 0) {
                final List<ProcessNameKey> keys = processDefinitionService.searchProcessNameGroups(query);
                result = buildResult(keys, query.activationState());
            }
            return new SearchResultImpl<>(count, result);
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
    }

    /**
     * Translates the {@link SearchOptions} into the grouped-search criteria, resolving the (single) supported sort in
     * one pass: the first sort on displayName or name wins, defaulting to displayName ascending.
     */
    private ProcessNameGroupQuery toQuery() {
        boolean sortByName = false;
        boolean ascending = true;
        for (final Sort sort : options.getSorts()) {
            if (ProcessDeploymentInfoSearchDescriptor.DISPLAY_NAME.equals(sort.getField())
                    || ProcessDeploymentInfoSearchDescriptor.NAME.equals(sort.getField())) {
                sortByName = ProcessDeploymentInfoSearchDescriptor.NAME.equals(sort.getField());
                final Order order = sort.getOrder();
                ascending = order != Order.DESC && order != Order.DESC_NULLS_FIRST && order != Order.DESC_NULLS_LAST;
                break;
            }
        }
        return new ProcessNameGroupQuery(activationStateFilter(), likePattern(options.getSearchTerm()), sortByName,
                ascending, options.getStartIndex(), options.getMaxResults());
    }

    /**
     * @return the value of the activationState filter, or {@code null} when not filtered on activationState.
     */
    private String activationStateFilter() {
        for (final SearchFilter filter : options.getFilters()) {
            if (ProcessDeploymentInfoSearchDescriptor.ACTIVATION_STATE.equals(filter.getField())
                    && filter.getValue() != null) {
                return String.valueOf(filter.getValue());
            }
        }
        return null;
    }

    /**
     * Wraps the search term as a SQL LIKE pattern, or {@code "%"} (match all) when no term is provided. Wildcard
     * characters typed by the user are escaped so they are matched literally; the named queries declare the matching
     * {@code ESCAPE '#'} clause.
     */
    private static String likePattern(final String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return "%";
        }
        // Escape the escape character first, then the LIKE wildcards.
        final String escaped = searchTerm
                .replace(LIKE_ESCAPE, LIKE_ESCAPE + LIKE_ESCAPE)
                .replace("%", LIKE_ESCAPE + "%")
                .replace("_", LIKE_ESCAPE + "_");
        return "%" + escaped + "%";
    }

    private List<ProcessNameInfo> buildResult(final List<ProcessNameKey> keys, final String activationState)
            throws SBonitaException {
        if (keys.isEmpty()) {
            return new ArrayList<>();
        }
        // Fetch versions by name only: simple and portable. When two groups share a name with different display
        // names, this over-fetches versions for the off-page group; grouping below by the full (name, displayName)
        // key keeps the result correct, and the over-fetch is bounded by the page size.
        final List<String> names = keys.stream().map(ProcessNameKey::getName).distinct().collect(Collectors.toList());
        final List<ProcessNameVersion> versions = processDefinitionService.getVersionsForProcessNames(names,
                activationState);

        // Group versions by their (name, displayName) key to match them back to the page's groups. The result order
        // comes from iterating the page's keys below, not from this map, so a plain HashMap is sufficient.
        final Map<ProcessNameKey, List<String>> versionsByGroup = new HashMap<>();
        for (final ProcessNameVersion version : versions) {
            versionsByGroup
                    .computeIfAbsent(new ProcessNameKey(version.getName(), version.getDisplayName()),
                            k -> new ArrayList<>())
                    .add(version.getVersion());
        }

        final List<ProcessNameInfo> result = new ArrayList<>(keys.size());
        for (final ProcessNameKey key : keys) {
            final List<String> groupVersions = versionsByGroup.getOrDefault(key, List.of());
            result.add(new ProcessNameInfoImpl(key.getName(), key.getDisplayName(), groupVersions));
        }
        return result;
    }
}
