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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.NamedElement;
import org.bonitasoft.engine.bpm.ObjectSeeker;
import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.bpm.document.DocumentListDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.EndEventDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowNodeDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.StartEventDefinition;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.internal.BaseElementImpl;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class FlowElementContainerDefinitionImpl extends BaseElementImpl implements FlowElementContainerDefinition {

    private static final long serialVersionUID = 1L;

    private final List<ActivityDefinition> activities;

    private final Set<TransitionDefinition> transitions;

    private final List<GatewayDefinition> gateways;

    private final List<StartEventDefinition> startEvents;

    private final List<IntermediateCatchEventDefinition> intermediateCatchEvents;

    private final List<IntermediateThrowEventDefinition> intermediateThrowEvents;

    private final List<EndEventDefinition> endEvents;

    private final List<DataDefinition> dataDefinitions;

    private final List<BusinessDataDefinition> businessDataDefinitions;

    private final List<DocumentDefinition> documentDefinitions;

    private final List<DocumentListDefinition> documentListDefinitions;

    private final List<ConnectorDefinition> connectors;

    private final Map<String, FlowNodeDefinition> flowNodes;

    public FlowElementContainerDefinitionImpl() {
        activities = new ArrayList<ActivityDefinition>();
        transitions = new HashSet<TransitionDefinition>();
        gateways = new ArrayList<GatewayDefinition>();
        startEvents = new ArrayList<StartEventDefinition>(1);
        intermediateCatchEvents = new ArrayList<IntermediateCatchEventDefinition>(4);
        endEvents = new ArrayList<EndEventDefinition>(4);
        intermediateThrowEvents = new ArrayList<IntermediateThrowEventDefinition>(4);
        dataDefinitions = new ArrayList<DataDefinition>();
        businessDataDefinitions = new ArrayList<BusinessDataDefinition>();
        documentDefinitions = new ArrayList<DocumentDefinition>();
        documentListDefinitions = new ArrayList<DocumentListDefinition>();
        connectors = new ArrayList<ConnectorDefinition>();
        flowNodes = new HashMap<String, FlowNodeDefinition>();
    }

    @Override
    public FlowNodeDefinition getFlowNode(final long sourceId) {
        final Set<FlowNodeDefinition> flowNodes = getFlowNodes();
        return getElementById(flowNodes, sourceId);
    }

    @Override
    public FlowNodeDefinition getFlowNode(final String sourceName) {
        final Set<FlowNodeDefinition> flowNodes = getFlowNodes();
        return getElementByName(flowNodes, sourceName);
    }

    private Set<FlowNodeDefinition> getFlowNodes() {
        final Set<FlowNodeDefinition> flowNodes = new HashSet<FlowNodeDefinition>();
        flowNodes.addAll(gateways);
        flowNodes.addAll(activities);
        flowNodes.addAll(startEvents);
        flowNodes.addAll(intermediateCatchEvents);
        flowNodes.addAll(intermediateThrowEvents);
        flowNodes.addAll(endEvents);
        flowNodes.addAll(getBoundaryEvents());
        return Collections.unmodifiableSet(flowNodes);
    }

    private List<BoundaryEventDefinition> getBoundaryEvents() {
        final List<BoundaryEventDefinition> boundaryEvents = new ArrayList<BoundaryEventDefinition>(3);
        for (final ActivityDefinition activity : activities) {
            boundaryEvents.addAll(activity.getBoundaryEventDefinitions());
        }
        return boundaryEvents;
    }

    @Override
    public List<ActivityDefinition> getActivities() {
        return Collections.unmodifiableList(activities);
    }

    @Override
    public ActivityDefinition getActivity(final String name) {
        return getElementByName(activities, name);
    }

    @Override
    public Set<TransitionDefinition> getTransitions() {
        return Collections.unmodifiableSet(transitions);
    }

    @Deprecated
    @Override
    public Set<GatewayDefinition> getGateways() {
        return Collections.unmodifiableSet(new HashSet<GatewayDefinition>(gateways));
    }

    @Override
    public List<GatewayDefinition> getGatewaysList() {
        return Collections.unmodifiableList(gateways);
    }

    @Override
    public GatewayDefinition getGateway(final String name) {
        return getElementByName(gateways, name);
    }

    private <T extends BaseElement> T getElementById(final Collection<T> elements, final long id) {
        T element = null;
        boolean found = false;
        final Iterator<T> iterator = elements.iterator();
        while (!found && iterator.hasNext()) {
            final T next = iterator.next();
            if (next.getId() == id) {
                found = true;
                element = next;
            }
        }
        return element;
    }

    private <T extends NamedElement> T getElementByName(final Collection<T> elements, final String name) {
        T element = null;
        boolean found = false;
        final Iterator<T> iterator = elements.iterator();
        while (!found && iterator.hasNext()) {
            final T next = iterator.next();
            if (name.equals(next.getName())) {
                found = true;
                element = next;
            }
        }
        return element;
    }

    @Override
    public List<StartEventDefinition> getStartEvents() {
        return Collections.unmodifiableList(startEvents);
    }

    @Override
    public List<IntermediateCatchEventDefinition> getIntermediateCatchEvents() {
        return Collections.unmodifiableList(intermediateCatchEvents);
    }

    @Override
    public List<IntermediateThrowEventDefinition> getIntermediateThrowEvents() {
        return Collections.unmodifiableList(intermediateThrowEvents);
    }

    @Override
    public List<EndEventDefinition> getEndEvents() {
        return Collections.unmodifiableList(endEvents);
    }

    @Override
    public List<BusinessDataDefinition> getBusinessDataDefinitions() {
        return Collections.unmodifiableList(businessDataDefinitions);
    }

    @Override
    public List<DataDefinition> getDataDefinitions() {
        return Collections.unmodifiableList(dataDefinitions);
    }

    @Override
    public List<DocumentDefinition> getDocumentDefinitions() {
        return Collections.unmodifiableList(documentDefinitions);
    }
    @Override
    public List<DocumentListDefinition> getDocumentListDefinitions() {
        return Collections.unmodifiableList(documentListDefinitions);
    }

    @Override
    public List<ConnectorDefinition> getConnectors() {
        return Collections.unmodifiableList(connectors);
    }

    public void addActivity(final ActivityDefinition activity) {
        activities.add(activity);
        flowNodes.put(activity.getName(), activity);
    }

    public void addTransition(final TransitionDefinition transition) {
        transitions.add(transition);
    }

    public void addGateway(final GatewayDefinition gateway) {
        gateways.add(gateway);
        flowNodes.put(gateway.getName(), gateway);
    }

    public void addStartEvent(final StartEventDefinition startEvent) {
        startEvents.add(startEvent);
        flowNodes.put(startEvent.getName(), startEvent);
    }

    public void addIntermediateCatchEvent(final IntermediateCatchEventDefinition event) {
        intermediateCatchEvents.add(event);
        flowNodes.put(event.getName(), event);
    }

    public void addIntermediateThrowEvent(final IntermediateThrowEventDefinition intermediateThrowEvent) {
        intermediateThrowEvents.add(intermediateThrowEvent);
        flowNodes.put(intermediateThrowEvent.getName(), intermediateThrowEvent);
    }

    public void addEndEvent(final EndEventDefinition endEvent) {
        endEvents.add(endEvent);
        flowNodes.put(endEvent.getName(), endEvent);
    }

    public void addBusinessDataDefinition(final BusinessDataDefinition businessDataDefinition) {
        businessDataDefinitions.add(businessDataDefinition);
    }

    public void addDataDefinition(final DataDefinition dataDefinition) {
        dataDefinitions.add(dataDefinition);
    }

    public void addDocumentDefinition(final DocumentDefinition documentDefinition) {
        documentDefinitions.add(documentDefinition);
    }
    public void addDocumentListDefinition(final DocumentListDefinition documentListDefinition) {
        documentListDefinitions.add(documentListDefinition);
    }

    public void addConnector(final ConnectorDefinition connectorDefinition) {
        connectors.add(connectorDefinition);
    }


    @Override
    public BusinessDataDefinition getBusinessDataDefinition(final String name) {
        return ObjectSeeker.getNamedElement(businessDataDefinitions, name);
    }

    @Override
    public DataDefinition getDataDefinition(final String name) {
        return ObjectSeeker.getNamedElement(dataDefinitions, name);
    }

}
