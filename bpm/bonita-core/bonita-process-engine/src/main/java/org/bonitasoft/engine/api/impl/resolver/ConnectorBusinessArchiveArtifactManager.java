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
package org.bonitasoft.engine.api.impl.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.resources.SBARResource;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.Problem.Level;
import org.bonitasoft.engine.bpm.process.impl.internal.ProblemImpl;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.SRecorderException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ConnectorBusinessArchiveArtifactManager implements BusinessArchiveArtifactManager {

    public static final String CONNECTOR = "connector";
    public static final int BATCH_SIZE = 10;
    private final ConnectorService connectorService;

    public ConnectorBusinessArchiveArtifactManager(ConnectorService connectorService) {
        this.connectorService = connectorService;
    }

    void addToBusinessArchive(BusinessArchiveBuilder businessArchiveBuilder, List<SBARResource> resources) {
        for (SBARResource resource : resources) {
            businessArchiveBuilder.addConnectorImplementation(new BarResource(resource.getName(), resource.getContent()));
        }
    }

    @Override
    public boolean deploy(final BusinessArchive businessArchive, final SProcessDefinition processDefinition)
            throws ConnectorException, SRecorderException {
        try {
            final Map<String, byte[]> resources = businessArchive.getResources("^" + CONNECTOR + "/.*$");
            for (Map.Entry<String, byte[]> entry : resources.entrySet()) {
                connectorService.addConnectorImplementation(processDefinition.getId(), entry.getKey().substring((CONNECTOR + "/").length()), entry.getValue());
            }
            return connectorService.loadConnectors(processDefinition)
                    && checkAllConnectorsHaveImplementation(connectorService, processDefinition).isEmpty();
        } catch (final SConnectorException e) {
            throw new ConnectorException(e);
        }
    }

    private List<Problem> checkAllConnectorsHaveImplementation(final ConnectorService connectorService, final SProcessDefinition processDefinition) {
        final List<SConnectorDefinition> processConnectors = processDefinition.getProcessContainer().getConnectors();
        final List<Problem> problems = new ArrayList<Problem>();
        if (processConnectors != null) {
            for (final SConnectorDefinition sConnectorDefinition : processConnectors) {
                try {
                    connectorService.getConnectorImplementation(processDefinition.getId(), sConnectorDefinition.getConnectorId(),
                            sConnectorDefinition.getVersion());
                } catch (final SConnectorException e) {
                    final Problem problem = new ProblemImpl(Level.ERROR, sConnectorDefinition.getName(), "connector", "The process connector '"
                            + sConnectorDefinition.getName() + "' has no implementation");
                    problems.add(problem);
                }
            }
        }
        final Set<SFlowNodeDefinition> flowNodes = processDefinition.getProcessContainer().getFlowNodes();
        if (flowNodes != null) {
            for (final SFlowNodeDefinition sFlowNodeDefinition : flowNodes) {
                final List<SConnectorDefinition> flowNodeConnectors = sFlowNodeDefinition.getConnectors();
                if (flowNodeConnectors != null) {
                    for (final SConnectorDefinition sConnectorDefinition : flowNodeConnectors) {
                        try {
                            connectorService.getConnectorImplementation(processDefinition.getId(), sConnectorDefinition.getConnectorId(),
                                    sConnectorDefinition.getVersion());
                        } catch (final SConnectorException e) {
                            final Problem problem = new ProblemImpl(Level.ERROR, sConnectorDefinition.getName(), "connector", "The connector '"
                                    + sConnectorDefinition.getName() + "' of flow node '" + sFlowNodeDefinition.getName() + "' has no implementation");
                            problems.add(problem);
                        }
                    }
                }
            }
        }
        return problems;
    }

    @Override
    public List<Problem> checkResolution(final SProcessDefinition processDefinition) {
        return checkAllConnectorsHaveImplementation(connectorService, processDefinition);
    }

    @Override
    public void delete(SProcessDefinition processDefinition) throws SObjectModificationException, SBonitaReadException, SRecorderException {
        connectorService.removeConnectorImplementations(processDefinition.getId());
    }

    @Override
    public void exportToBusinessArchive(long processDefinitionId, BusinessArchiveBuilder businessArchiveBuilder) throws SBonitaException {
        List<SBARResource> resources = getConnectorImplementations(processDefinitionId);
        addToBusinessArchive(businessArchiveBuilder, resources);
    }

    List<SBARResource> getConnectorImplementations(long processDefinitionId) throws SBonitaReadException {
        List<SBARResource> allResources = new ArrayList<>();
        List<SBARResource> resources;
        int from = 0;
        do {
            resources = connectorService.getConnectorImplementations(processDefinitionId, from, BATCH_SIZE);
            from += BATCH_SIZE;
            allResources.addAll(resources);
        } while (resources.size() == BATCH_SIZE);
        return allResources;
    }

}
