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
package org.bonitasoft.engine.api.impl.transaction.connector;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.connector.ConnectorService;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public class GetNumberOfConnectorImplementations implements TransactionContentWithResult<Long> {

    private final ConnectorService connectorService;

    private final long processDefinitionId;

    private final long tenantId;

    private Long numberOfConnectorImplementations;

    public GetNumberOfConnectorImplementations(final ConnectorService connectorService, final long processDefinitionId, final long tenantId) {
        this.connectorService = connectorService;
        this.processDefinitionId = processDefinitionId;
        this.tenantId = tenantId;
    }

    @Override
    public void execute() throws SBonitaException {
        numberOfConnectorImplementations = connectorService.getNumberOfConnectorImplementations(processDefinitionId, tenantId);
    }

    @Override
    public Long getResult() {
        return numberOfConnectorImplementations;
    }

}
