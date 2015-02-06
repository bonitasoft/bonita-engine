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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.Problem.Level;
import org.bonitasoft.engine.bpm.process.impl.internal.ProblemImpl;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;

/**
 * @author Matthieu Chaffotte
 */
public class CheckConnectorImplementations implements TransactionContent {

    private final ConnectorService connectorService;

    private final long tenantId;

    private final SProcessDefinition sDefinition;

    private List<Problem> problems;

    public CheckConnectorImplementations(final ConnectorService connectorService, final long tenantId, final SProcessDefinition sDefinition) {
        this.connectorService = connectorService;
        this.sDefinition = sDefinition;
        this.tenantId = tenantId;
    }

    @Override
    public void execute() {
        problems = new ArrayList<Problem>();
        final List<SConnectorDefinition> processConnectors = sDefinition.getProcessContainer().getConnectors();
        if (processConnectors != null) {
            for (final SConnectorDefinition sConnectorDefinition : processConnectors) {
                try {
                    connectorService.getConnectorImplementation(sDefinition.getId(), sConnectorDefinition.getConnectorId(), sConnectorDefinition.getVersion(),
                            tenantId);
                } catch (final SConnectorException e) {
                    final Problem problem = new ProblemImpl(Level.ERROR, sConnectorDefinition.getName(), "connector", "The process connector '"
                            + sConnectorDefinition.getName() + "' has no implementation.");
                    problems.add(problem);
                }
            }
        }
        final Set<SFlowNodeDefinition> flowNodes = sDefinition.getProcessContainer().getFlowNodes();
        if (flowNodes != null) {
            for (final SFlowNodeDefinition sFlowNodeDefinition : flowNodes) {
                final List<SConnectorDefinition> flowNodeConnectors = sFlowNodeDefinition.getConnectors();
                if (flowNodeConnectors != null) {
                    for (final SConnectorDefinition sConnectorDefinition : flowNodeConnectors) {
                        try {
                            connectorService.getConnectorImplementation(sDefinition.getId(), sConnectorDefinition.getConnectorId(),
                                    sConnectorDefinition.getVersion(), tenantId);
                        } catch (final SConnectorException e) {
                            final Problem problem = new ProblemImpl(Level.ERROR, sConnectorDefinition.getId(), "connector", "The connector '"
                                    + sConnectorDefinition.getName() + "' of flow node '" + sFlowNodeDefinition.getName() + "' has no implementation.");
                            problems.add(problem);
                        }
                    }
                }
            }
        }
    }

    public List<Problem> getProblems() {
        return problems;
    }

}
