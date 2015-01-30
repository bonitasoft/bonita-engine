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
package org.bonitasoft.engine.bpm.flownode;

import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.bpm.document.DocumentListDefinition;

/**
 * Allows to access all flow elements (activities, gateways, events and transitions) of a process or sub-process.
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface FlowElementContainerDefinition extends BaseElement {

    List<ActivityDefinition> getActivities();

    ActivityDefinition getActivity(String name);

    Set<TransitionDefinition> getTransitions();

    /**
     *
     * @return A set of GatewayDefinition
     * @see #getGatewaysList()
     * @since 6.0
     * @deprecated As of release 6.1, replaced by {@link #getGatewaysList()}
     */
    @Deprecated
    Set<GatewayDefinition> getGateways();

    /**
     *
     * @return A list of GatewayDefinition
     * @since 6.1
     */
    List<GatewayDefinition> getGatewaysList();

    GatewayDefinition getGateway(String name);

    List<StartEventDefinition> getStartEvents();

    List<IntermediateCatchEventDefinition> getIntermediateCatchEvents();

    List<IntermediateThrowEventDefinition> getIntermediateThrowEvents();

    List<EndEventDefinition> getEndEvents();

    List<DataDefinition> getDataDefinitions();

    DataDefinition getDataDefinition(String name);

    List<DocumentDefinition> getDocumentDefinitions();

    List<ConnectorDefinition> getConnectors();

    FlowNodeDefinition getFlowNode(long sourceId);

    FlowNodeDefinition getFlowNode(String sourceName);

    List<BusinessDataDefinition> getBusinessDataDefinitions();

    BusinessDataDefinition getBusinessDataDefinition(String name);

    List<DocumentListDefinition> getDocumentListDefinitions();
}
