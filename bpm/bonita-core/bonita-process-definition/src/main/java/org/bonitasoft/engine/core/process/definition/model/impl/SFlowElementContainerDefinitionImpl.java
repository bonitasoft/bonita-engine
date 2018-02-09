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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.bpm.document.DocumentListDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.CallActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.EndEventDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.StartEventDefinition;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.AutomaticTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.HumanTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ManualTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ReceiveTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.SendTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;
import org.bonitasoft.engine.bpm.userfilter.UserFilterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SDocumentDefinition;
import org.bonitasoft.engine.core.process.definition.model.SDocumentListDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SGatewayDefinition;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SIntermediateCatchEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SIntermediateThrowEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SEndEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SIntermediateCatchEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SIntermediateThrowEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SStartEventDefinitionImpl;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.exception.BonitaRuntimeException;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SFlowElementContainerDefinitionImpl extends SBaseElementImpl implements SFlowElementContainerDefinition {

    private static final long serialVersionUID = -40122166157566478L;

    private final Map<ConnectorEvent, List<SConnectorDefinition>> connectorsMap;

    private final Map<String, SFlowNodeDefinition> allElementsMapString;

    private final Map<String, SGatewayDefinition> gatewaysMap;

    private final Map<Long, SFlowNodeDefinition> allElementsMap;

    private final Map<String, SConnectorDefinition> allConnectorsMap;

    private final Map<String, STransitionDefinition> transitionsMap;

    private final Set<SActivityDefinition> activities;

    private final Set<SSubProcessDefinition> subProcessDefinitions;

    private final Set<STransitionDefinition> transitions;

    private final Set<SGatewayDefinition> gateways;

    private final Set<SFlowNodeDefinition> allElements;

    private final List<SConnectorDefinition> connectors;

    private final List<SStartEventDefinition> sStartEvents;

    private final List<SIntermediateCatchEventDefinition> sIntermediateCatchEvents;

    private final List<SBoundaryEventDefinition> sBoundaryEvents;

    private final List<SEndEventDefinition> sEndEvents;

    private final List<SIntermediateThrowEventDefinition> sIntermediateThrowEvents;

    private final List<SDataDefinition> sDataDefinitions;

    private final List<SBusinessDataDefinition> sBusinessDataDefinitions;

    private final List<SDocumentDefinition> sDocumentDefinitions;

    private final List<SDocumentListDefinition> sDocumentListDefinitions;

    private boolean containsInclusiveGateway = false;

    public SFlowElementContainerDefinitionImpl() {
        activities = new HashSet<>();
        subProcessDefinitions = new HashSet<>(0);
        transitions = new HashSet<>();
        transitionsMap = new HashMap<>();
        gateways = new HashSet<>();
        gatewaysMap = new HashMap<>();
        allElements = new HashSet<>();
        allElementsMap = new HashMap<>();
        allElementsMapString = new HashMap<>();
        connectors = new ArrayList<>();
        connectorsMap = new HashMap<>(2);
        connectorsMap.put(ConnectorEvent.ON_ENTER, new ArrayList<SConnectorDefinition>());
        connectorsMap.put(ConnectorEvent.ON_FINISH, new ArrayList<SConnectorDefinition>());
        allConnectorsMap = new HashMap<>(2);
        sStartEvents = new ArrayList<>();
        sIntermediateCatchEvents = new ArrayList<>();
        sIntermediateThrowEvents = new ArrayList<>();
        sEndEvents = new ArrayList<>();
        sDataDefinitions = new ArrayList<>();
        sBusinessDataDefinitions = new ArrayList<>();
        sDocumentDefinitions = new ArrayList<>();
        sDocumentListDefinitions = new ArrayList<>();
        sBoundaryEvents = new ArrayList<>();
    }

    public SFlowElementContainerDefinitionImpl(final FlowElementContainerDefinition container) {
        transitions = new HashSet<>();
        transitionsMap = new HashMap<>();
        for (final TransitionDefinition transition : container.getTransitions()) {
            final STransitionDefinitionImpl sTransitionDefinitionImpl = new STransitionDefinitionImpl(transition);
            addTransition(sTransitionDefinitionImpl);
        }

        allElements = new HashSet<>();
        allElementsMap = new HashMap<>();
        allElementsMapString = new HashMap<>();
        final List<ActivityDefinition> activities2 = container.getActivities();
        sBoundaryEvents = new ArrayList<>();
        activities = new HashSet<>(activities2.size());
        subProcessDefinitions = new HashSet<>(0);
        initializeActivities(activities2);

        final List<GatewayDefinition> gateways2 = container.getGatewaysList();
        gateways = new HashSet<>(gateways2.size());
        gatewaysMap = new HashMap<>(gateways2.size());
        final Iterator<GatewayDefinition> iterator1 = gateways2.iterator();
        while (iterator1.hasNext()) {
            final GatewayDefinition gatewayDefinition = iterator1.next();
            final SGatewayDefinitionImpl gateway;
            gateway = new SGatewayDefinitionImpl(gatewayDefinition, transitionsMap);
            addGateway(gateway);
        }

        final List<ConnectorDefinition> connectors2 = container.getConnectors();
        final List<SConnectorDefinition> mConnectors = new ArrayList<>(connectors2.size());
        connectorsMap = new HashMap<>(2);
        connectorsMap.put(ConnectorEvent.ON_ENTER, new ArrayList<SConnectorDefinition>());
        connectorsMap.put(ConnectorEvent.ON_FINISH, new ArrayList<SConnectorDefinition>());
        allConnectorsMap = new HashMap<>(2);
        for (final ConnectorDefinition connector : connectors2) {
            final SConnectorDefinitionImpl e = new SConnectorDefinitionImpl(connector);
            mConnectors.add(e);
            connectorsMap.get(e.getActivationEvent()).add(e);
            allConnectorsMap.put(e.getName(), e);
        }
        connectors = Collections.unmodifiableList(mConnectors);

        sStartEvents = initializeStartEvents(container.getStartEvents(), transitionsMap);
        sIntermediateCatchEvents = initializeIntermediateCatchEvents(container.getIntermediateCatchEvents(), transitionsMap);
        sIntermediateThrowEvents = initializeIntermediateThrowEvents(container.getIntermediateThrowEvents(), transitionsMap);
        sEndEvents = initializeEndEvents(container.getEndEvents(), transitionsMap);

        final List<BusinessDataDefinition> businessDataDefinitions = container.getBusinessDataDefinitions();
        final List<SBusinessDataDefinition> mBusinessDataDefinitions = new ArrayList<>(businessDataDefinitions.size());
        for (final BusinessDataDefinition businessDataDefinition : businessDataDefinitions) {
            mBusinessDataDefinitions.add(ServerModelConvertor.convertBusinessDataDefinition(businessDataDefinition));
        }
        sBusinessDataDefinitions = Collections.unmodifiableList(mBusinessDataDefinitions);

        final List<DataDefinition> processDataDefinitions = container.getDataDefinitions();
        final List<SDataDefinition> mDataDefinitions = new ArrayList<>(processDataDefinitions.size());
        for (final DataDefinition dataDefinition : processDataDefinitions) {
            mDataDefinitions.add(ServerModelConvertor.convertDataDefinition(dataDefinition));
        }
        sDataDefinitions = Collections.unmodifiableList(mDataDefinitions);
        final List<DocumentDefinition> documentDefinitions2 = container.getDocumentDefinitions();
        final List<SDocumentDefinition> mDocumentDefinitions = new ArrayList<>(documentDefinitions2.size());
        for (final DocumentDefinition documentDefinition : documentDefinitions2) {
            mDocumentDefinitions.add(new SDocumentDefinitionImpl(documentDefinition));
        }
        sDocumentDefinitions = Collections.unmodifiableList(mDocumentDefinitions);
        final List<DocumentListDefinition> documentListDefinitions2 = container.getDocumentListDefinitions();
        final ArrayList<SDocumentListDefinition> mDocumentListDefinitions = new ArrayList<>(documentListDefinitions2.size());
        for (final DocumentListDefinition documentListDefinition : documentListDefinitions2) {
            mDocumentListDefinitions.add(new SDocumentListDefinitionImpl(documentListDefinition));
        }
        sDocumentListDefinitions = Collections.unmodifiableList(mDocumentListDefinitions);

    }

    private void initializeActivities(final List<ActivityDefinition> activities2) {
        final Iterator<ActivityDefinition> iterator = activities2.iterator();
        while (iterator.hasNext()) {
            final ActivityDefinition activityDefinition = iterator.next();
            final SActivityDefinitionImpl activity;
            if (activityDefinition instanceof AutomaticTaskDefinitionImpl) {
                activity = new SAutomaticTaskDefinitionImpl(activityDefinition, transitionsMap);
            } else if (activityDefinition instanceof HumanTaskDefinitionImpl) {
                if (activityDefinition instanceof UserTaskDefinitionImpl) {
                    activity = new SUserTaskDefinitionImpl((UserTaskDefinition) activityDefinition, transitionsMap);
                } else {
                    activity = new SManualTaskDefinitionImpl((ManualTaskDefinitionImpl) activityDefinition, transitionsMap);
                }
                final HumanTaskDefinitionImpl humanTaskDefinitionImpl = (HumanTaskDefinitionImpl) activityDefinition;
                final UserFilterDefinition userFilter = humanTaskDefinitionImpl.getUserFilter();
                final SHumanTaskDefinitionImpl sHumanTaskDefinitionImpl = (SHumanTaskDefinitionImpl) activity;
                if (userFilter != null) {
                    sHumanTaskDefinitionImpl.setUserFilter(new SUserFilterDefinitionImpl(userFilter));
                }
                sHumanTaskDefinitionImpl.setPriority(humanTaskDefinitionImpl.getPriority());
                sHumanTaskDefinitionImpl.setExpectedDuration(ServerModelConvertor.convertExpression(humanTaskDefinitionImpl.getExpectedDuration()));
            } else if (activityDefinition instanceof ReceiveTaskDefinitionImpl) {
                activity = new SReceiveTaskDefinitionImpl((ReceiveTaskDefinitionImpl) activityDefinition, transitionsMap);
            } else if (activityDefinition instanceof SendTaskDefinitionImpl) {
                activity = new SSendTaskDefinitionImpl((SendTaskDefinitionImpl) activityDefinition, transitionsMap);
            } else if (activityDefinition instanceof CallActivityDefinition) {
                activity = new SCallActivityDefinitionImpl((CallActivityDefinition) activityDefinition, transitionsMap);
            } else if (activityDefinition instanceof SubProcessDefinition) {
                activity = new SSubProcessDefinitionImpl((SubProcessDefinition) activityDefinition);
            } else {
                throw new BonitaRuntimeException("Can't find the client type for " + activityDefinition.getClass().getName());
            }
            addActivity(activity);
        }
    }

    private List<SEndEventDefinition> initializeEndEvents(final List<EndEventDefinition> endEvents, final Map<String, STransitionDefinition> transitionsMap) {
        final List<SEndEventDefinition> sEndEvents = new ArrayList<>(endEvents.size());
        for (final EndEventDefinition endEventDefinition : endEvents) {
            final SEndEventDefinitionImpl sEndEvent = new SEndEventDefinitionImpl(endEventDefinition, transitionsMap);
            sEndEvents.add(sEndEvent);
            addFlowNode(sEndEvent);
        }
        return sEndEvents;
    }

    private List<SStartEventDefinition> initializeStartEvents(final List<StartEventDefinition> startEvents,
            final Map<String, STransitionDefinition> transitionsMap) {
        final List<SStartEventDefinition> sStartEvents = new ArrayList<>(startEvents.size());
        for (final StartEventDefinition startEventDefinition : startEvents) {
            final SStartEventDefinitionImpl sStartEventDefinitionImpl = new SStartEventDefinitionImpl(startEventDefinition, transitionsMap);
            sStartEvents.add(sStartEventDefinitionImpl);
            addFlowNode(sStartEventDefinitionImpl);
        }
        return sStartEvents;
    }

    private List<SIntermediateCatchEventDefinition> initializeIntermediateCatchEvents(final List<IntermediateCatchEventDefinition> intermediateCatchEvents,
            final Map<String, STransitionDefinition> transitionsMap) {
        final List<SIntermediateCatchEventDefinition> sIntermediateCatchEvents = new ArrayList<>(
                intermediateCatchEvents.size());
        for (final IntermediateCatchEventDefinition intermediateCatchEventDefinition : intermediateCatchEvents) {
            final SIntermediateCatchEventDefinitionImpl sIntermediateCatchEvent = new SIntermediateCatchEventDefinitionImpl(intermediateCatchEventDefinition,
                    transitionsMap);
            sIntermediateCatchEvents.add(sIntermediateCatchEvent);
            addFlowNode(sIntermediateCatchEvent);
        }
        return sIntermediateCatchEvents;
    }

    private List<SIntermediateThrowEventDefinition> initializeIntermediateThrowEvents(final List<IntermediateThrowEventDefinition> intermediateThrowEvents,
            final Map<String, STransitionDefinition> transitionsMap) {
        final List<SIntermediateThrowEventDefinition> sIntermediateThrowEvents = new ArrayList<>(
                intermediateThrowEvents.size());
        for (final IntermediateThrowEventDefinition intermediateThrowEventDefinition : intermediateThrowEvents) {
            final SIntermediateThrowEventDefinitionImpl sIntermediateThrowEvent = new SIntermediateThrowEventDefinitionImpl(intermediateThrowEventDefinition,
                    transitionsMap);
            sIntermediateThrowEvents.add(sIntermediateThrowEvent);
            addFlowNode(sIntermediateThrowEvent);
        }
        return sIntermediateThrowEvents;
    }

    public void addTransition(final STransitionDefinition transition) {
        transitions.add(transition);
        transitionsMap.put(transition.getId().toString(), transition);
    }

    public void addActivity(final SActivityDefinition activity) {
        sBoundaryEvents.addAll(activity.getBoundaryEventDefinitions());
        for (final SBoundaryEventDefinition boundary : activity.getBoundaryEventDefinitions()) {
            sBoundaryEvents.add(boundary);
            allElements.add(boundary);
            allElementsMap.put(boundary.getId(), boundary);
        }
        activities.add(activity);
        addFlowNode(activity);
        if (activity instanceof SSubProcessDefinition) {
            addSubProcess((SSubProcessDefinition) activity);
        }
    }

    public void addEvent(SEventDefinition eventDefinition) {
        addFlowNode(eventDefinition);
    }

    private void addFlowNode(SFlowNodeDefinition flowNodeDefinition) {
        allElements.add(flowNodeDefinition);
        allElementsMap.put(flowNodeDefinition.getId(), flowNodeDefinition);
        allElementsMapString.put(flowNodeDefinition.getName(), flowNodeDefinition);
    }

    public void addSubProcess(final SSubProcessDefinition sSubProcessDefinition) {
        subProcessDefinitions.add(sSubProcessDefinition);
    }

    public void addGateway(final SGatewayDefinition gateway) {
        gateways.add(gateway);
        if (gateway.getGatewayType() == SGatewayType.INCLUSIVE) {
            containsInclusiveGateway = true;
        }
        gatewaysMap.put(gateway.getName(), gateway);
        addFlowNode(gateway);
    }

    public void addBusinessDataDefinition(final SBusinessDataDefinition businessDataDefinition) {
        sBusinessDataDefinitions.add(businessDataDefinition);
    }

    @Override
    public Set<SGatewayDefinition> getGateways() {
        return gateways;
    }

    @Override
    public SGatewayDefinition getGateway(final String name) {
        SGatewayDefinition sGatewayDefinition = gatewaysMap.get(name);
        if (sGatewayDefinition == null) {
            sGatewayDefinition = getGatewayFromSubProcesses(name);
        }
        return sGatewayDefinition;
    }

    private SGatewayDefinition getGatewayFromSubProcesses(final String name) {
        boolean found = false;
        SGatewayDefinition gateway = null;
        final Iterator<SActivityDefinition> iterator = activities.iterator();
        while (iterator.hasNext() && !found) {
            final SActivityDefinition activityDefinition = iterator.next();
            if (SFlowNodeType.SUB_PROCESS.equals(activityDefinition.getType())) {
                gateway = ((SSubProcessDefinition) activityDefinition).getSubProcessContainer().getGateway(name);
                if (gateway != null) {
                    found = true;
                }
            }
        }
        return gateway;
    }

    @Override
    public Set<SFlowNodeDefinition> getFlowNodes() {
        return allElements;
    }

    @Override
    public SFlowNodeDefinition getFlowNode(final long id) {
        SFlowNodeDefinition flowNodeDefinition = allElementsMap.get(id);
        if (flowNodeDefinition == null) {
            flowNodeDefinition = getFlowNodeFromSubProcesses(id);
        }
        return flowNodeDefinition;
    }

    @Override
    public SFlowNodeDefinition getFlowNode(final String targetFlowNode) {
        SFlowNodeDefinition flowNodeDefinition = allElementsMapString.get(targetFlowNode);
        if (flowNodeDefinition == null) {
            flowNodeDefinition = getFlowNodeFromSubProcesses(targetFlowNode);
        }
        return flowNodeDefinition;
    }

    private SFlowNodeDefinition getFlowNodeFromSubProcesses(final long id) {
        boolean found = false;
        SFlowNodeDefinition flowNode = null;
        final Iterator<SActivityDefinition> iterator = activities.iterator();
        while (iterator.hasNext() && !found) {
            final SActivityDefinition activityDefinition = iterator.next();
            if (SFlowNodeType.SUB_PROCESS.equals(activityDefinition.getType())) {
                flowNode = ((SSubProcessDefinition) activityDefinition).getSubProcessContainer().getFlowNode(id);
                if (flowNode != null) {
                    found = true;
                }
            }
        }
        return flowNode;
    }

    private SFlowNodeDefinition getFlowNodeFromSubProcesses(final String targetFlowNode) {
        boolean found = false;
        SFlowNodeDefinition flowNode = null;
        final Iterator<SActivityDefinition> iterator = activities.iterator();
        while (iterator.hasNext() && !found) {
            final SActivityDefinition activityDefinition = iterator.next();
            if (SFlowNodeType.SUB_PROCESS.equals(activityDefinition.getType())) {
                flowNode = ((SSubProcessDefinition) activityDefinition).getSubProcessContainer().getFlowNode(targetFlowNode);
                if (flowNode != null) {
                    found = true;
                }
            }
        }
        return flowNode;
    }

    @Override
    public STransitionDefinition getTransition(final String transitionId) {
        STransitionDefinition transitionDefinition = transitionsMap.get(transitionId);
        if (transitionDefinition == null) {
            transitionDefinition = getTransitionFromSubProcesses(transitionId);
        }
        return transitionDefinition;
    }

    private STransitionDefinition getTransitionFromSubProcesses(final String transitionId) {
        boolean found = false;
        STransitionDefinition transition = null;
        final Iterator<SActivityDefinition> iterator = activities.iterator();
        while (iterator.hasNext() && !found) {
            final SActivityDefinition activityDefinition = iterator.next();
            if (SFlowNodeType.SUB_PROCESS.equals(activityDefinition.getType())) {
                transition = ((SSubProcessDefinition) activityDefinition).getSubProcessContainer().getTransition(transitionId);
                if (transition != null) {
                    found = true;
                }
            }
        }
        return transition;
    }

    @Override
    public List<SConnectorDefinition> getConnectors() {
        return connectors;
    }

    @Override
    // public SConnectorDefinition getConnectorDefinition(final long id) {// FIXME: Uncomment when generate id
    public SConnectorDefinition getConnectorDefinition(final String name) {
        return allConnectorsMap.get(name);
    }

    @Override
    public List<SConnectorDefinition> getConnectors(final ConnectorEvent connectorEvent) {
        return connectorsMap.get(connectorEvent);
    }

    @Override
    public List<SStartEventDefinition> getStartEvents() {
        return sStartEvents;
    }

    @Override
    public List<SIntermediateCatchEventDefinition> getIntermediateCatchEvents() {
        return sIntermediateCatchEvents;
    }

    @Override
    public List<SEndEventDefinition> getEndEvents() {
        return sEndEvents;
    }

    @Override
    public List<SBusinessDataDefinition> getBusinessDataDefinitions() {
        return sBusinessDataDefinitions;
    }

    @Override
    public SBusinessDataDefinition getBusinessDataDefinition(final String name) {
        if (name == null) {
            return null;
        }
        boolean found = false;
        SBusinessDataDefinition businessData = null;
        final Iterator<SBusinessDataDefinition> iterator = sBusinessDataDefinitions.iterator();
        while (iterator.hasNext() && !found) {
            final SBusinessDataDefinition currentBusinessData = iterator.next();
            if (currentBusinessData.getName().equals(name)) {
                found = true;
                businessData = currentBusinessData;
            }
        }
        return businessData;
    }

    @Override
    public List<SDataDefinition> getDataDefinitions() {
        return sDataDefinitions;
    }

    @Override
    public List<SIntermediateThrowEventDefinition> getIntermdiateThrowEvents() {
        return Collections.unmodifiableList(sIntermediateThrowEvents);
    }

    @Override
    public List<SDocumentDefinition> getDocumentDefinitions() {
        return sDocumentDefinitions;
    }

    @Override
    public Set<SActivityDefinition> getActivities() {
        return activities;
    }

    @Override
    public Set<SSubProcessDefinition> getSubProcessDefinitions() {
        return subProcessDefinitions;
    }

    @Override
    public Set<STransitionDefinition> getTransitions() {
        return transitions;
    }

    @Override
    public List<SBoundaryEventDefinition> getBoundaryEvents() {
        return Collections.unmodifiableList(sBoundaryEvents);
    }

    @Override
    public SBoundaryEventDefinition getBoundaryEvent(final String name) {
        boolean found = false;
        SBoundaryEventDefinition boundaryEvent = null;
        final Iterator<SBoundaryEventDefinition> iterator = sBoundaryEvents.iterator();
        while (iterator.hasNext() && !found) {
            final SBoundaryEventDefinition currentBoundaryEvent = iterator.next();
            if (currentBoundaryEvent.getName().equals(name)) {
                found = true;
                boundaryEvent = currentBoundaryEvent;
            }
        }
        return boundaryEvent;
    }

    @Override
    public boolean containsInclusiveGateway() {
        return containsInclusiveGateway;
    }

    /**
     * @return the document list definitions
     * @since 6.4.0
     */
    @Override
    public List<SDocumentListDefinition> getDocumentListDefinitions() {
        return sDocumentListDefinitions;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SFlowElementContainerDefinitionImpl that = (SFlowElementContainerDefinitionImpl) o;
        return containsInclusiveGateway == that.containsInclusiveGateway &&
                Objects.equals(connectorsMap, that.connectorsMap) &&
                Objects.equals(allElementsMapString, that.allElementsMapString) &&
                Objects.equals(gatewaysMap, that.gatewaysMap) &&
                Objects.equals(allElementsMap, that.allElementsMap) &&
                Objects.equals(allConnectorsMap, that.allConnectorsMap) &&
                Objects.equals(transitionsMap, that.transitionsMap) &&
                Objects.equals(activities, that.activities) &&
                Objects.equals(subProcessDefinitions, that.subProcessDefinitions) &&
                Objects.equals(transitions, that.transitions) &&
                Objects.equals(gateways, that.gateways) &&
                Objects.equals(allElements, that.allElements) &&
                Objects.equals(connectors, that.connectors) &&
                Objects.equals(sStartEvents, that.sStartEvents) &&
                Objects.equals(sIntermediateCatchEvents, that.sIntermediateCatchEvents) &&
                Objects.equals(sBoundaryEvents, that.sBoundaryEvents) &&
                Objects.equals(sEndEvents, that.sEndEvents) &&
                Objects.equals(sIntermediateThrowEvents, that.sIntermediateThrowEvents) &&
                Objects.equals(sDataDefinitions, that.sDataDefinitions) &&
                Objects.equals(sBusinessDataDefinitions, that.sBusinessDataDefinitions) &&
                Objects.equals(sDocumentDefinitions, that.sDocumentDefinitions) &&
                Objects.equals(sDocumentListDefinitions, that.sDocumentListDefinitions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), connectorsMap, allElementsMapString, gatewaysMap, allElementsMap, allConnectorsMap, transitionsMap, activities, subProcessDefinitions, transitions, gateways, allElements, connectors, sStartEvents, sIntermediateCatchEvents, sBoundaryEvents, sEndEvents, sIntermediateThrowEvents, sDataDefinitions, sBusinessDataDefinitions, sDocumentDefinitions, sDocumentListDefinitions, containsInclusiveGateway);
    }
}
