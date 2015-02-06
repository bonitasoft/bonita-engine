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
package org.bonitasoft.engine.bpm.bar.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.bpm.document.DocumentListDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.EndEventDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.StartEventDefinition;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowNodeDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.TransitionDefinitionImpl;
import org.bonitasoft.engine.io.xml.ElementBinding;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class FlowElementBinding extends ElementBinding {

    private final List<ActivityDefinition> activities = new ArrayList<ActivityDefinition>();

    private final List<GatewayDefinition> gateways = new ArrayList<GatewayDefinition>();

    private final List<TransitionDefinition> transitions = new ArrayList<TransitionDefinition>();

    private final List<StartEventDefinition> startEvents = new ArrayList<StartEventDefinition>();

    private final List<IntermediateCatchEventDefinition> intermediateCatchEvents = new ArrayList<IntermediateCatchEventDefinition>();

    private final List<EndEventDefinition> endEvents = new ArrayList<EndEventDefinition>();

    private final List<IntermediateThrowEventDefinition> intermediateThrowEvents = new ArrayList<IntermediateThrowEventDefinition>();

    private final List<DataDefinition> dataDefinitions = new ArrayList<DataDefinition>();

    private final List<BusinessDataDefinition> businessDataDefinitions = new ArrayList<BusinessDataDefinition>();

    private final List<DocumentDefinition> documentDefinitions = new ArrayList<DocumentDefinition>();

    private final List<DocumentListDefinition> documentListDefinitions = new ArrayList<DocumentListDefinition>();

    private final List<ConnectorDefinition> connectors = new ArrayList<ConnectorDefinition>();

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLProcessDefinition.AUTOMATIC_TASK_NODE.equals(name) || XMLProcessDefinition.USER_TASK_NODE.equals(name)
                || XMLProcessDefinition.MANUAL_TASK_NODE.equals(name) || XMLProcessDefinition.CALL_ACTIVITY_NODE.equals(name)
                || XMLProcessDefinition.SUB_PROCESS.equals(name) || XMLProcessDefinition.RECEIVE_TASK_NODE.equals(name)
                || XMLProcessDefinition.SEND_TASK_NODE.equals(name)) {
            activities.add((ActivityDefinition) value);
        } else if (XMLProcessDefinition.CONNECTOR_NODE.equals(name)) {
            connectors.add((ConnectorDefinition) value);
        } else if (XMLProcessDefinition.DATA_DEFINITION_NODE.equals(name)) {
            dataDefinitions.add((DataDefinition) value);
        } else if (XMLProcessDefinition.BUSINESS_DATA_DEFINITION_NODE.equals(name)) {
            businessDataDefinitions.add((BusinessDataDefinition) value);
        } else if (XMLProcessDefinition.XML_DATA_DEFINITION_NODE.equals(name)) {
            dataDefinitions.add((DataDefinition) value);
        } else if (XMLProcessDefinition.TEXT_DATA_DEFINITION_NODE.equals(name)) {
            dataDefinitions.add((DataDefinition) value);
        } else if (XMLProcessDefinition.DOCUMENT_DEFINITION_NODE.equals(name)) {
            documentDefinitions.add((DocumentDefinition) value);
        } else if (XMLProcessDefinition.DOCUMENT_LIST_DEFINITION_NODE.equals(name)) {
            documentListDefinitions.add((DocumentListDefinition) value);
        } else if (XMLProcessDefinition.GATEWAY_NODE.equals(name)) {
            gateways.add((GatewayDefinition) value);
        } else if (XMLProcessDefinition.TRANSITION_NODE.equals(name)) {
            transitions.add((TransitionDefinition) value);
        } else if (XMLProcessDefinition.START_EVENT_NODE.equals(name)) {
            startEvents.add((StartEventDefinition) value);
        } else if (XMLProcessDefinition.INTERMEDIATE_CATCH_EVENT_NODE.equals(name)) {
            intermediateCatchEvents.add((IntermediateCatchEventDefinition) value);
        } else if (XMLProcessDefinition.INTERMEDIATE_THROW_EVENT_NODE.equals(name)) {
            intermediateThrowEvents.add((IntermediateThrowEventDefinition) value);
        } else if (XMLProcessDefinition.END_EVENT_NODE.equals(name)) {
            endEvents.add((EndEventDefinition) value);
        }

    }

    @Override
    public Object getObject() {
        final FlowElementContainerDefinitionImpl container = new FlowElementContainerDefinitionImpl();
        for (final TransitionDefinition transition : transitions) {
            container.addTransition(transition);
        }
        for (final ActivityDefinition activity : activities) {
            container.addActivity(activity);
        }
        for (final GatewayDefinition gateway : gateways) {
            container.addGateway(gateway);
        }
        for (final ConnectorDefinition connector : connectors) {
            container.addConnector(connector);
        }
        for (final DataDefinition dataDefinition : dataDefinitions) {
            container.addDataDefinition(dataDefinition);
        }
        for (final BusinessDataDefinition businessDataDefinition : businessDataDefinitions) {
            container.addBusinessDataDefinition(businessDataDefinition);
        }
        for (final DocumentDefinition documentDefinition : documentDefinitions) {
            container.addDocumentDefinition(documentDefinition);
        }
        for (final DocumentListDefinition documentListDefinition : documentListDefinitions) {
            container.addDocumentListDefinition(documentListDefinition);
        }
        for (final StartEventDefinition startEvent : startEvents) {
            container.addStartEvent(startEvent);
        }
        for (final IntermediateCatchEventDefinition intermediateCatchEvent : intermediateCatchEvents) {
            container.addIntermediateCatchEvent(intermediateCatchEvent);
        }
        for (final IntermediateThrowEventDefinition intermediateThrowEvent : intermediateThrowEvents) {
            container.addIntermediateThrowEvent(intermediateThrowEvent);
        }
        for (final EndEventDefinition endEvent : endEvents) {
            container.addEndEvent(endEvent);
        }
        addTransitionOnFlowNodes(container);
        return container;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.FLOW_ELEMENTS_NODE;
    }

    private void addTransitionOnFlowNodes(final FlowElementContainerDefinitionImpl container) {
        for (final TransitionDefinition transition : transitions) {
            final long source = transition.getSource();
            final FlowNodeDefinitionImpl sourceNode = (FlowNodeDefinitionImpl) container.getFlowNode(source);
            if (sourceNode != null) {
                final TransitionDefinition defaultTransition = sourceNode.getDefaultTransition();
                if (defaultTransition != null && ((TransitionDefinitionImpl) defaultTransition).getId() == ((TransitionDefinitionImpl) transition).getId()) {
                    sourceNode.setDefaultTransition(transition);
                } else {
                    final List<TransitionDefinition> outgoingTransitionsForSourceNode = sourceNode.getOutgoingTransitions();
                    for (final TransitionDefinition transitionRef : outgoingTransitionsForSourceNode) {
                        if (((TransitionDefinitionImpl) transitionRef).getId() == ((TransitionDefinitionImpl) transition).getId()) {
                            final int indexOfSTransitionRef = outgoingTransitionsForSourceNode.indexOf(transitionRef);
                            sourceNode.removeOutgoingTransition(transitionRef);
                            sourceNode.addOutgoingTransition(indexOfSTransitionRef, transition);
                            break;
                        }
                    }
                }
            }
            final long target = transition.getTarget();
            final FlowNodeDefinitionImpl targetNode = (FlowNodeDefinitionImpl) container.getFlowNode(target);
            if (targetNode != null) {
                final List<TransitionDefinition> incomingTransitionsForTargetNode = targetNode.getIncomingTransitions();
                for (final TransitionDefinition transitionRef : incomingTransitionsForTargetNode) {
                    if (((TransitionDefinitionImpl) transitionRef).getId() == ((TransitionDefinitionImpl) transition).getId()) {
                        final int indexOfSTransitionRef = incomingTransitionsForTargetNode.indexOf(transitionRef);
                        targetNode.removeIncomingTransition(transitionRef);
                        targetNode.addIncomingTransition(indexOfSTransitionRef, transition);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) {

    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {

    }

}
