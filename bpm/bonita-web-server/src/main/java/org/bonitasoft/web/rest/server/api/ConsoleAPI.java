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
package org.bonitasoft.web.rest.server.api;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.server.framework.API;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APINotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Séverin Moussel
 */
public abstract class ConsoleAPI<T extends IItem> extends API<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleAPI.class);

    private APISession sessionSingleton = null;

    /**
     * Distinct references (as pre-formatted {@code 'attribute' id X} labels) that could not be resolved while
     * enriching the current request, emitted as one summary log when it completes (see
     * {@link #logUnresolvedReferences()}). A {@link Set} because the log only names the missing entities, so the
     * same dangling reference repeated across many rows is recorded once - the buffer size is bounded by the
     * number of distinct missing references, not by the affected-row count. A fresh API instance is created per
     * HTTP request, so this buffer is confined to one request/thread - no synchronization.
     */
    private final Set<String> unresolvedReferences = new LinkedHashSet<>();

    /**
     * Get the session to access the engine SDK
     */
    protected APISession getEngineSession() {
        if (this.sessionSingleton == null) {
            this.sessionSingleton = (APISession) getHttpSession().getAttribute("apiSession");
        }
        return this.sessionSingleton;
    }

    /**
     * Resolve a single deploy (a foreign-key reference such as {@code started_by}) and attach it to
     * {@code item}, tolerating a reference that can no longer be resolved.
     * <p>
     * When the referenced entity has been deleted, resolving it throws {@link APINotFoundException} /
     * {@link APIItemNotFoundException} and would turn the <em>whole</em> page into an HTTP 404. Instead,
     * this leaves the attribute as its raw id and records the miss, so every other deploy and row is
     * returned normally. The miss is recorded only when the cause chain holds a genuine entity-not-found
     * (see {@link #isEntityNotFound(Throwable)}); any other not-found is unexpected and rethrown rather
     * than masked as a successful response.
     * <p>
     * Recorded references are emitted as one summary log when the request completes (see
     * {@link #logUnresolvedReferences()}), so support can identify the deleted entity. The buffer is
     * drained only by the {@code runGet}/{@code runSearch} overrides, the sole callers of
     * {@code fillDeploys}.
     *
     * @param item the item being enriched
     * @param attribute the deploy attribute name being resolved (e.g. {@code started_by})
     * @param referencedId the foreign-key id the deploy points to
     * @param resolver resolves {@code referencedId} into the deployed item (e.g. a datastore lookup)
     */
    protected void deploySafely(final T item, final String attribute, final APIID referencedId,
            final Function<APIID, IItem> resolver) {
        try {
            item.setDeploy(attribute, resolver.apply(referencedId));
        } catch (final APINotFoundException | APIItemNotFoundException e) {
            // Tolerate only a genuine entity-not-found; rethrow any other not-found (see javadoc).
            if (!isEntityNotFound(e.getCause())) {
                throw e;
            }
            // Record (not log) the distinct missing reference; drained as one summary log on completion.
            // Recording exists only to feed that log, so skip it when the log is disabled.
            if (LOGGER.isWarnEnabled()) {
                unresolvedReferences.add("'" + attribute + "' id " + referencedId);
            }
        }
    }

    /**
     * Whether {@code cause} or anything in its cause chain is an engine {@link NotFoundException} (the referenced
     * entity was removed).
     */
    private static boolean isEntityNotFound(final Throwable cause) {
        for (Throwable t = cause; t != null; t = t.getCause()) {
            if (t instanceof NotFoundException) {
                return true;
            }
        }
        return false;
    }

    @Override
    public T runGet(final APIID id, final List<String> deploys, final List<String> counters) {
        try {
            return super.runGet(id, deploys, counters);
        } finally {
            logUnresolvedReferences();
        }
    }

    @Override
    public ItemSearchResult<T> runSearch(final int page, final int resultsByPage, final String search,
            final String orders, final Map<String, String> filters, final List<String> deploys,
            final List<String> counters) {
        try {
            return super.runSearch(page, resultsByPage, search, orders, filters, deploys, counters);
        } finally {
            logUnresolvedReferences();
        }
    }

    /**
     * Emit one summary log listing the distinct references that could not be resolved during the current
     * request, then reset the buffer. Returns early when that log level is disabled (the log is its only effect).
     */
    private void logUnresolvedReferences() {
        if (!LOGGER.isWarnEnabled() || unresolvedReferences.isEmpty()) {
            return;
        }
        LOGGER.warn(
                "Could not resolve {} distinct reference(s) on this {} request: the referenced entities no longer " +
                        "exist in the organization (likely deleted), so each id is returned alone, without its " +
                        "details. Missing references: {}.",
                unresolvedReferences.size(), getItemDefinition().getToken(),
                String.join("; ", unresolvedReferences));
        unresolvedReferences.clear();
    }
}
