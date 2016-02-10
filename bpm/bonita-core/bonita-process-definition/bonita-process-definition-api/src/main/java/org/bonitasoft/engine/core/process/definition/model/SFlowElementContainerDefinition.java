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
package org.bonitasoft.engine.core.process.definition.model;

import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SIntermediateCatchEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SIntermediateThrowEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface SFlowElementContainerDefinition extends SBaseElement {

    SNamedElement getElementContainer();

    Set<SFlowNodeDefinition> getFlowNodes();

    List<SBoundaryEventDefinition> getBoundaryEvents();

    SBoundaryEventDefinition getBoundaryEvent(String name) throws SBoundaryEventNotFoundException;

    SFlowNodeDefinition getFlowNode(long id);

    Set<SActivityDefinition> getActivities();

    Set<STransitionDefinition> getTransitions();

    STransitionDefinition getTransition(String name);

    Set<SGatewayDefinition> getGateways();

    SGatewayDefinition getGateway(String name);

    List<SStartEventDefinition> getStartEvents();

    List<SIntermediateCatchEventDefinition> getIntermediateCatchEvents();

    List<SIntermediateThrowEventDefinition> getIntermdiateThrowEvents();

    List<SEndEventDefinition> getEndEvents();

    List<SDataDefinition> getDataDefinitions();

    List<SBusinessDataDefinition> getBusinessDataDefinitions();

    SBusinessDataDefinition getBusinessDataDefinition(String name);

    List<SDocumentDefinition> getDocumentDefinitions();

    /**
     * @param name
     *        the name of the connector definition
     * @return
     *         the connector definition having that name
     * @since 6.1
     */
    SConnectorDefinition getConnectorDefinition(String name);

    List<SConnectorDefinition> getConnectors(ConnectorEvent connectorEvent);

    List<SConnectorDefinition> getConnectors();

    SFlowNodeDefinition getFlowNode(String targetFlowNode);

    boolean containsInclusiveGateway();

    /**
     * @return
     * @since 6.4.0
     */
    Set<SSubProcessDefinition> getSubProcessDefinitions();

    /**
     * @return the document list definitions
     * @since 6.4.0
     */
    List<SDocumentListDefinition> getDocumentListDefinitions();
}
