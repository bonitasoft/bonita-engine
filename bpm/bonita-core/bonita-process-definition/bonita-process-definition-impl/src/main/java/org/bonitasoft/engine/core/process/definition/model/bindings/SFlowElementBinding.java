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
package org.bonitasoft.engine.core.process.definition.model.bindings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SDocumentDefinition;
import org.bonitasoft.engine.core.process.definition.model.SDocumentListDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SEndEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SIntermediateCatchEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SIntermediateThrowEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SStartEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SActivityDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowNodeDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SGatewayDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.STransitionDefinitionImpl;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SFlowElementBinding extends ElementBinding {

    private final List<SActivityDefinitionImpl> activities = new ArrayList<SActivityDefinitionImpl>();

    private final List<SGatewayDefinitionImpl> gateways = new ArrayList<SGatewayDefinitionImpl>();

    private final List<SStartEventDefinitionImpl> startEvents = new ArrayList<SStartEventDefinitionImpl>();

    private final List<SIntermediateCatchEventDefinitionImpl> intermediateCatchEvents = new ArrayList<SIntermediateCatchEventDefinitionImpl>();

    private final List<SEndEventDefinitionImpl> endEvents = new ArrayList<SEndEventDefinitionImpl>();

    private final List<SIntermediateThrowEventDefinitionImpl> intermediateThrowEvents = new ArrayList<SIntermediateThrowEventDefinitionImpl>();

    private final List<STransitionDefinition> sTransitions = new ArrayList<STransitionDefinition>();

    private final List<SDataDefinition> dataDefinitions = new ArrayList<SDataDefinition>();

    private final List<SBusinessDataDefinition> businessDataDefinitions = new ArrayList<SBusinessDataDefinition>();

    private final List<SDocumentDefinition> documentDefinitions = new ArrayList<SDocumentDefinition>();

    private final List<SDocumentListDefinition> documentListDefinitions = new ArrayList<SDocumentListDefinition>();

    private final List<SConnectorDefinition> connectors = new ArrayList<SConnectorDefinition>();

    @Override
    public void setAttributes(final Map<String, String> attributes) {
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLSProcessDefinition.AUTOMATIC_TASK_NODE.equals(name) || XMLSProcessDefinition.USER_TASK_NODE.equals(name)
                || XMLSProcessDefinition.MANUAL_TASK_NODE.equals(name) || XMLSProcessDefinition.CALL_ACTIVITY_NODE.equals(name)
                || XMLSProcessDefinition.RECEIVE_TASK_NODE.equals(name) || XMLSProcessDefinition.SUB_PROCESS.equals(name)
                || XMLSProcessDefinition.SEND_TASK_NODE.equals(name)) {
            activities.add((SActivityDefinitionImpl) value);
        } else if (XMLSProcessDefinition.CONNECTOR_NODE.equals(name)) {
            connectors.add((SConnectorDefinition) value);
        } else if (XMLSProcessDefinition.BUSINESS_DATA_DEFINITION_NODE.equals(name)) {
            businessDataDefinitions.add((SBusinessDataDefinition) value);
        } else if (XMLSProcessDefinition.DATA_DEFINITION_NODE.equals(name)) {
            dataDefinitions.add((SDataDefinition) value);
        } else if (XMLSProcessDefinition.TEXT_DATA_DEFINITION_NODE.equals(name)) {
            dataDefinitions.add((SDataDefinition) value);
        } else if (XMLSProcessDefinition.XML_DATA_DEFINITION_NODE.equals(name)) {
            dataDefinitions.add((SDataDefinition) value);
        } else if (XMLSProcessDefinition.DOCUMENT_DEFINITION_NODE.equals(name)) {
            documentDefinitions.add((SDocumentDefinition) value);
        } else if (XMLSProcessDefinition.DOCUMENT_LIST_DEFINITION_NODE.equals(name)) {
            documentListDefinitions.add((SDocumentListDefinition) value);
        } else if (XMLSProcessDefinition.GATEWAY_NODE.equals(name)) {
            gateways.add((SGatewayDefinitionImpl) value);
        } else if (XMLSProcessDefinition.TRANSITION_NODE.equals(name)) {
            sTransitions.add((STransitionDefinition) value);
        } else if (XMLSProcessDefinition.START_EVENT_NODE.equals(name)) {
            startEvents.add((SStartEventDefinitionImpl) value);
        } else if (XMLSProcessDefinition.INTERMEDIATE_CATCH_EVENT_NODE.equals(name)) {
            intermediateCatchEvents.add((SIntermediateCatchEventDefinitionImpl) value);
        } else if (XMLSProcessDefinition.END_EVENT_NODE.equals(name)) {
            endEvents.add((SEndEventDefinitionImpl) value);
        } else if (XMLSProcessDefinition.INTERMEDIATE_THROW_EVENT_NODE.equals(name)) {
            intermediateThrowEvents.add((SIntermediateThrowEventDefinitionImpl) value);
        }
    }

    @Override
    public Object getObject() {
        final SFlowElementContainerDefinitionImpl container = new SFlowElementContainerDefinitionImpl();
        for (final STransitionDefinition transition : sTransitions) {
            container.addTransition(transition);
        }
        for (final SActivityDefinitionImpl activity : activities) {
            container.addActivity(activity);
            activity.setParentContainer(container);
        }
        for (final SGatewayDefinitionImpl gateway : gateways) {
            container.addGateway(gateway);
            gateway.setParentContainer(container);
        }
        for (final SConnectorDefinition connector : connectors) {
            container.addConnector(connector);
        }
        for (final SBusinessDataDefinition businessDataDefinition : businessDataDefinitions) {
            container.addBusinessDataDefinition(businessDataDefinition);
        }
        for (final SDataDefinition dataDefinition : dataDefinitions) {
            container.addDataDefinition(dataDefinition);
        }
        for (final SDocumentDefinition documentDefinition : documentDefinitions) {
            container.addDocumentDefinition(documentDefinition);
        }
        for (final SDocumentListDefinition documentListDefinition : documentListDefinitions) {
            container.addDocumentListDefinition(documentListDefinition);
        }
        for (final SStartEventDefinitionImpl startEvent : startEvents) {
            container.addStartEvent(startEvent);
            startEvent.setParentContainer(container);
        }
        for (final SIntermediateCatchEventDefinitionImpl intermediateCatchEvent : intermediateCatchEvents) {
            container.addIntemediateCatchEvent(intermediateCatchEvent);
            intermediateCatchEvent.setParentContainer(container);
        }
        for (final SEndEventDefinitionImpl endEvent : endEvents) {
            container.addEndEvent(endEvent);
            endEvent.setParentContainer(container);
        }
        for (final SIntermediateThrowEventDefinitionImpl intermediateThrowEvent : intermediateThrowEvents) {
            container.addIntermediateThrowEvent(intermediateThrowEvent);
            intermediateThrowEvent.setParentContainer(container);
        }
        final Map<Long, SFlowNodeDefinitionImpl> allFlowNodes = getAllFlowNodes(container);
        addTransitionOnFlowNodes(allFlowNodes);
        return container;
    }

    private Map<Long, SFlowNodeDefinitionImpl> getAllFlowNodes(final SFlowElementContainerDefinitionImpl container) {
        final Map<Long, SFlowNodeDefinitionImpl> allFlowNodes = new HashMap<Long, SFlowNodeDefinitionImpl>(container.getFlowNodes().size());
        for (final SFlowNodeDefinition flowNode : container.getFlowNodes()) {
            allFlowNodes.put(flowNode.getId(), (SFlowNodeDefinitionImpl) flowNode);
        }
        for (final SActivityDefinition activity : container.getActivities()) {
            if (!activity.getBoundaryEventDefinitions().isEmpty()) {
                for (final SBoundaryEventDefinition boundary : activity.getBoundaryEventDefinitions()) {
                    allFlowNodes.put(boundary.getId(), (SFlowNodeDefinitionImpl) boundary);
                }
            }
        }
        return allFlowNodes;
    }

    private void addTransitionOnFlowNodes(final Map<Long, SFlowNodeDefinitionImpl> allFlowNodes) {
        for (final STransitionDefinition sTransition : sTransitions) {
            final long source = sTransition.getSource();
            final SFlowNodeDefinitionImpl sourceNode = allFlowNodes.get(source);
            if (sourceNode != null) {
                final STransitionDefinition defaultTransition = sourceNode.getDefaultTransition();
                if (defaultTransition != null
                        && ((STransitionDefinitionImpl) defaultTransition).getId().equals(((STransitionDefinitionImpl) sTransition).getId())) {
                    sourceNode.setDefaultTransition(sTransition);
                } else {
                    final List<STransitionDefinition> outgoingTransitionsForSourceNode = sourceNode.getOutgoingTransitions();
                    for (final STransitionDefinition sTransitionRef : outgoingTransitionsForSourceNode) {
                        if (sTransitionRef.getId().equals(sTransition.getId())) {
                            final int indexOfSTransitionRef = outgoingTransitionsForSourceNode.indexOf(sTransitionRef);
                            sourceNode.removeOutgoingTransition(sTransitionRef);
                            sourceNode.addOutgoingTransition(indexOfSTransitionRef, sTransition);
                            break;
                        }
                    }
                }
            }
            final long target = sTransition.getTarget();
            final SFlowNodeDefinitionImpl targetNode = allFlowNodes.get(target);
            if (targetNode != null) {
                final List<STransitionDefinition> incomingTransitionsForTargetNode = targetNode.getIncomingTransitions();
                for (final STransitionDefinition sTransitionRef : incomingTransitionsForTargetNode) {
                    if (sTransitionRef.getId().equals(sTransition.getId())) {
                        final int indexOfSTransitionRef = incomingTransitionsForTargetNode.indexOf(sTransitionRef);
                        targetNode.removeIncomingTransition(sTransitionRef);
                        targetNode.addIncomingTransition(indexOfSTransitionRef, sTransition);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.FLOW_ELEMENTS_NODE;
    }

}
