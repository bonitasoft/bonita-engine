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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.bpm.NamedElement;
import org.bonitasoft.engine.bpm.ObjectSeeker;
import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataDefinitionImpl;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.impl.ConnectorDefinitionImpl;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.impl.DataDefinitionImpl;
import org.bonitasoft.engine.bpm.data.impl.TextDataDefinitionImpl;
import org.bonitasoft.engine.bpm.data.impl.XMLDataDefinitionImpl;
import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.bpm.document.DocumentListDefinition;
import org.bonitasoft.engine.bpm.document.impl.DocumentDefinitionImpl;
import org.bonitasoft.engine.bpm.document.impl.DocumentListDefinitionImpl;
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
import org.bonitasoft.engine.bpm.internal.ProcessBaseElementImpl;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.bpm.process.Visitable;
import org.bonitasoft.engine.bpm.process.impl.internal.SubProcessDefinitionImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class FlowElementContainerDefinitionImpl extends ProcessBaseElementImpl implements FlowElementContainerDefinition, Visitable {

    private static final long serialVersionUID = 1L;
    @XmlElements({
            @XmlElement(type = AutomaticTaskDefinitionImpl.class, name = "automaticTask"),
            @XmlElement(type = CallActivityDefinitionImpl.class, name = "callActivity"),
            @XmlElement(type = ManualTaskDefinitionImpl.class, name = "manualTask"),
            @XmlElement(type = ReceiveTaskDefinitionImpl.class, name = "receiveTask"),
            @XmlElement(type = SendTaskDefinitionImpl.class, name = "sendTask"),
            @XmlElement(type = UserTaskDefinitionImpl.class, name = "userTask"),
            @XmlElement(type = SubProcessDefinitionImpl.class, name = "subProcess")
    })
    private final List<ActivityDefinition> activities;
    @XmlElementWrapper(name = "transitions")
    @XmlElement(type = TransitionDefinitionImpl.class, name = "transition")
    private final Set<TransitionDefinition> transitions;
    @XmlElement(type = GatewayDefinitionImpl.class, name = "gateway")
    private final List<GatewayDefinition> gateways;
    @XmlElement(type = StartEventDefinitionImpl.class, name = "startEvent")
    private final List<StartEventDefinition> startEvents;
    @XmlElement(type = IntermediateCatchEventDefinitionImpl.class, name = "intermediateCatchEvent")
    private final List<IntermediateCatchEventDefinition> intermediateCatchEvents;
    @XmlElement(type = IntermediateThrowEventDefinitionImpl.class, name = "intermediateThrowEvent")
    private final List<IntermediateThrowEventDefinition> intermediateThrowEvents;
    @XmlElement(type = EndEventDefinitionImpl.class, name = "endEvent")
    private final List<EndEventDefinition> endEvents;
    @XmlElementWrapper(name = "dataDefinitions")
    @XmlElements({
            @XmlElement(type = DataDefinitionImpl.class, name = "dataDefinition"),
            @XmlElement(type = TextDataDefinitionImpl.class, name = "textDataDefinition"),
            @XmlElement(type = XMLDataDefinitionImpl.class, name = "xmlDataDefinition")
    })
    private final List<DataDefinition> dataDefinitions;
    @XmlElementWrapper(name = "businessDataDefinitions")
    @XmlElement(type = BusinessDataDefinitionImpl.class, name = "businessDataDefinition")
    private final List<BusinessDataDefinition> businessDataDefinitions;
    @XmlElementWrapper(name = "documentDefinitions")
    @XmlElement(type = DocumentDefinitionImpl.class, name = "documentDefinition")
    private final List<DocumentDefinition> documentDefinitions;
    @XmlElementWrapper(name = "documentListDefinitions")
    @XmlElement(type = DocumentListDefinitionImpl.class, name = "documentListDefinition")
    private final List<DocumentListDefinition> documentListDefinitions;
    @XmlElementWrapper(name = "connectors")
    @XmlElement(type = ConnectorDefinitionImpl.class, name = "connector")
    private final List<ConnectorDefinition> connectors;
    @XmlElement
    private final ElementFinder elementFinder = new ElementFinder();

    public FlowElementContainerDefinitionImpl() {
        activities = new ArrayList<>();
        transitions = new HashSet<>();
        gateways = new ArrayList<>();
        startEvents = new ArrayList<>(1);
        intermediateCatchEvents = new ArrayList<>(4);
        endEvents = new ArrayList<>(4);
        intermediateThrowEvents = new ArrayList<>(4);
        dataDefinitions = new ArrayList<>();
        businessDataDefinitions = new ArrayList<>();
        documentDefinitions = new ArrayList<>();
        documentListDefinitions = new ArrayList<>();
        connectors = new ArrayList<>();
    }

    @Override
    public FlowNodeDefinition getFlowNode(final long sourceId) {
        return elementFinder.getElementById(getFlowNodes(), sourceId);
    }

    @Override
    public FlowNodeDefinition getFlowNode(final String sourceName) {
        final Set<FlowNodeDefinition> flowNodes = getFlowNodes();
        return getElementByName(flowNodes, sourceName);
    }

    private Set<FlowNodeDefinition> getFlowNodes() {
        final Set<FlowNodeDefinition> flowNodes = new HashSet<>();
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
        final List<BoundaryEventDefinition> boundaryEvents = new ArrayList<>(3);
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
        return Collections.unmodifiableSet(new HashSet<>(gateways));
    }

    @Override
    public List<GatewayDefinition> getGatewaysList() {
        return Collections.unmodifiableList(gateways);
    }

    @Override
    public GatewayDefinition getGateway(final String name) {
        return getElementByName(gateways, name);
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
    }

    public void addTransition(final TransitionDefinition transition) {
        transitions.add(transition);
    }

    public void addGateway(final GatewayDefinition gateway) {
        gateways.add(gateway);
    }

    public void addStartEvent(final StartEventDefinition startEvent) {
        startEvents.add(startEvent);
    }

    public void addIntermediateCatchEvent(final IntermediateCatchEventDefinition event) {
        intermediateCatchEvents.add(event);
    }

    public void addIntermediateThrowEvent(final IntermediateThrowEventDefinition intermediateThrowEvent) {
        intermediateThrowEvents.add(intermediateThrowEvent);
    }

    public void addEndEvent(final EndEventDefinition endEvent) {
        endEvents.add(endEvent);
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

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("activities", activities)
                .append("transitions", transitions)
                .append("gateways", gateways)
                .append("startEvents", startEvents)
                .append("intermediateCatchEvents", intermediateCatchEvents)
                .append("intermediateThrowEvents", intermediateThrowEvents)
                .append("endEvents", endEvents)
                .append("dataDefinitions", dataDefinitions)
                .append("businessDataDefinitions", businessDataDefinitions)
                .append("documentDefinitions", documentDefinitions)
                .append("documentListDefinitions", documentListDefinitions)
                .append("connectors", connectors)
                .append("elementFinder", elementFinder)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        FlowElementContainerDefinitionImpl that = (FlowElementContainerDefinitionImpl) o;
        return Objects.equals(activities, that.activities) &&
                Objects.equals(transitions, that.transitions) &&
                Objects.equals(gateways, that.gateways) &&
                Objects.equals(startEvents, that.startEvents) &&
                Objects.equals(intermediateCatchEvents, that.intermediateCatchEvents) &&
                Objects.equals(intermediateThrowEvents, that.intermediateThrowEvents) &&
                Objects.equals(endEvents, that.endEvents) &&
                Objects.equals(dataDefinitions, that.dataDefinitions) &&
                Objects.equals(businessDataDefinitions, that.businessDataDefinitions) &&
                Objects.equals(documentDefinitions, that.documentDefinitions) &&
                Objects.equals(documentListDefinitions, that.documentListDefinitions) &&
                Objects.equals(connectors, that.connectors) &&
                Objects.equals(elementFinder, that.elementFinder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), activities, transitions, gateways, startEvents, intermediateCatchEvents, intermediateThrowEvents, endEvents,
                dataDefinitions, businessDataDefinitions, documentDefinitions, documentListDefinitions, connectors, elementFinder);
    }
}
