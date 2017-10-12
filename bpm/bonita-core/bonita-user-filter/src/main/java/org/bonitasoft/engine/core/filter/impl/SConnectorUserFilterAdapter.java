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
package org.bonitasoft.engine.core.filter.impl;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.bonitasoft.engine.connector.SConnector;
import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.filter.AbstractUserFilter;
import org.bonitasoft.engine.filter.UserFilter;
import org.bonitasoft.engine.filter.UserFilterException;

/**
 * @author Baptiste Mesta
 */
public class SConnectorUserFilterAdapter implements SConnector {

    private final UserFilter filter;

    private List<Long> userIds;

    private boolean shouldAutoAssignTaskIfSingleResult;

    private final String actorName;

    public SConnectorUserFilterAdapter(final UserFilter filter, final String actorName) {
        this.filter = filter;
        this.actorName = actorName;
    }

    @Override
    public void setInputParameters(final Map<String, Object> parameters) {
        final APIAccessor apiAccessor = (APIAccessor) parameters.remove("connectorApiAccessor");
        final EngineExecutionContext executionContext = (EngineExecutionContext) parameters.remove("engineExecutionContext");
        if (filter instanceof AbstractUserFilter) {
            ((AbstractUserFilter) filter).setAPIAccessor(apiAccessor);
            if (executionContext != null) {
                ((AbstractUserFilter) filter).setExecutionContext(executionContext);
            }
        }
        filter.setInputParameters(parameters);
    }

    @Override
    public void validate() {

    }

    @Override
    public Map<String, Object> execute() throws SConnectorException {
        try {
            userIds = filter.filter(actorName);
            shouldAutoAssignTaskIfSingleResult = filter.shouldAutoAssignTaskIfSingleResult();
        } catch (final UserFilterException e) {
            throw new SConnectorException(e);
        }
        return null;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public boolean shouldAutoAssignTaskIfSingleResult() {
        return shouldAutoAssignTaskIfSingleResult;
    }

    @Override
    public void connect() {
        // nothing for user filters
    }

    @Override
    public void disconnect() {
        // nothing for user filters
    }

}
