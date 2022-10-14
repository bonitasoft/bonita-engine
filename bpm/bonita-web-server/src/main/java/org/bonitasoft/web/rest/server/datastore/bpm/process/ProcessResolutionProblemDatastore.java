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
package org.bonitasoft.web.rest.server.datastore.bpm.process;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.process.ProcessResolutionProblemItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;

/**
 * @author Séverin Moussel
 */
public class ProcessResolutionProblemDatastore extends CommonDatastore<ProcessResolutionProblemItem, Problem> implements
        DatastoreHasSearch<ProcessResolutionProblemItem> {

    public ProcessResolutionProblemDatastore(final APISession engineSession) {
        super(engineSession);
    }

    @Override
    protected ProcessResolutionProblemItem convertEngineToConsoleItem(final Problem item) {
        final ProcessResolutionProblemItem consoleItem = new ProcessResolutionProblemItem();
        consoleItem.setMessage(item.getDescription());
        consoleItem.setTargetType(item.getResource());
        consoleItem.setRessourceId(item.getResourceId());
        return consoleItem;
    }

    @Override
    public ItemSearchResult<ProcessResolutionProblemItem> search(final int page, final int resultsByPage,
            final String search, final String orders,
            final Map<String, String> filters) {
        try {

            final List<Problem> errors = TenantAPIAccessor.getProcessAPI(getEngineSession())
                    .getProcessResolutionProblems(
                            MapUtil.getValueAsLong(filters, ProcessResolutionProblemItem.FILTER_PROCESS_ID));

            final int startIndex = page * resultsByPage;
            return new ItemSearchResult<>(
                    page,
                    resultsByPage,
                    errors.size(),
                    convertEngineToConsoleItemsList(
                            errors.subList(
                                    startIndex,
                                    Math.min(startIndex + resultsByPage, errors.size()))));

        } catch (final Exception e) {
            throw new APIException(e);
        }
    }
}
