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

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.parser.SConnectorImplementationDescriptor;
import org.bonitasoft.engine.persistence.OrderByType;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public class GetConnectorImplementations implements TransactionContentWithResult<List<SConnectorImplementationDescriptor>> {

    private final ConnectorService connectorService;

    private final long processDefinitionId;

    private final int startIndex;

    private final int maxResults;

    private final String field;

    private final OrderByType order;

    private List<SConnectorImplementationDescriptor> sConnectorImplementationDescriptors;

    private final long tenantId;

    public GetConnectorImplementations(final ConnectorService connectorService, final long processDefinitionId, final long tenantId, final int startIndex,
            final int maxResults, final String field, final OrderByType order) {
        this.connectorService = connectorService;
        this.processDefinitionId = processDefinitionId;
        this.tenantId = tenantId;
        this.startIndex = startIndex;
        this.maxResults = maxResults;
        this.field = field;
        this.order = order;
    }

    @Override
    public void execute() throws SBonitaException {
        sConnectorImplementationDescriptors = connectorService.getConnectorImplementations(processDefinitionId, tenantId, startIndex, maxResults, field, order);
    }

    @Override
    public List<SConnectorImplementationDescriptor> getResult() {
        return sConnectorImplementationDescriptors;
    }

}
