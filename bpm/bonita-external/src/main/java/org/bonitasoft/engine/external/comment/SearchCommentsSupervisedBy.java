/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.external.comment;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.external.comment.transaction.SearchCommentsSupervisedByTransaction;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Hongwen Zang
 * @author Matthieu Chaffotte
 */
public class SearchCommentsSupervisedBy extends TenantCommand {

    private static final String SUPERVISOR_ID_KEY = "supervisorId";

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {

        final Long supervisorId = (Long) parameters.get(SUPERVISOR_ID_KEY);
        if (supervisorId == null) {
            throw new SCommandParameterizationException(SUPERVISOR_ID_KEY + " is missing");
        }

        final SearchOptions searchOptions = (SearchOptions) parameters.get("SEARCH_OPTIONS_KEY");
        if (searchOptions == null) {
            throw new SCommandParameterizationException("SEARCH_OPTIONS_KEY is missing");
        }

        final SCommentService commentService = serviceAccessor.getCommentService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = serviceAccessor.getSearchEntitiesDescriptor();
        final SearchCommentsSupervisedByTransaction searchTransaction = new SearchCommentsSupervisedByTransaction(supervisorId, commentService,
                searchEntitiesDescriptor.getSearchCommentDescriptor(), searchOptions);
        try {
            searchTransaction.execute();
        } catch (final SBonitaException sbe) {
            throw new SCommandExecutionException(sbe);
        }
        return searchTransaction.getResult();
    }

}
