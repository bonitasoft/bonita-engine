/*******************************************************************************
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
 ******************************************************************************/

package org.bonitasoft.engine.bpm.flowElementContainer;

import org.bonitasoft.engine.bdm.model.NamedElement;
import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.bpm.document.DocumentListDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.EndEventDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowNodeDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.StartEventDefinition;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.AutomaticTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.CallActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ManualTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ReceiveTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.SendTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.internal.BaseElementImpl;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.bpm.process.Visitable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author mazourd
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FlowElementContainerDefinitionImplUptoActivitiesTestClass extends BaseElementImpl implements FlowElementContainerDefinition, Visitable {

    private static final long serialVersionUID = 1L;
    @XmlElementWrapper(name = "activities")
    @XmlElements({
            @XmlElement(type = AutomaticTaskDefinitionImpl.class, name = "activity"),
            @XmlElement(type = CallActivityDefinitionImpl.class, name = "activity"),
            @XmlElement(type = ManualTaskDefinitionImpl.class,name = "activity"),
            @XmlElement(type = ReceiveTaskDefinitionImpl.class,name = "activity"),
            @XmlElement(type = SendTaskDefinitionImpl.class,name = "activity"),
            @XmlElement(type = UserTaskDefinitionImpl.class, name = "activity")
    })
    private final List<ActivityDefinition> activities;

    public FlowElementContainerDefinitionImplUptoActivitiesTestClass() {
        activities = new ArrayList<>();
    }

    @Override
    public List<ActivityDefinition> getActivities() {
        return Collections.unmodifiableList(activities);
    }

    @Override
    public ActivityDefinition getActivity(String name) {
        return null;
    }

    public void addActivity(final ActivityDefinition activity) {
        activities.add(activity);
    }

    @Override
    public Set<TransitionDefinition> getTransitions() {
        return null;
    }

    @Override
    public Set<GatewayDefinition> getGateways() {
        return null;
    }

    @Override
    public List<GatewayDefinition> getGatewaysList() {
        return null;
    }

    @Override
    public GatewayDefinition getGateway(String name) {
        return null;
    }

    @Override
    public List<StartEventDefinition> getStartEvents() {
        return null;
    }

    @Override
    public List<IntermediateCatchEventDefinition> getIntermediateCatchEvents() {
        return null;
    }

    @Override
    public List<IntermediateThrowEventDefinition> getIntermediateThrowEvents() {
        return null;
    }

    @Override
    public List<EndEventDefinition> getEndEvents() {
        return null;
    }

    @Override
    public List<DataDefinition> getDataDefinitions() {
        return null;
    }

    @Override
    public DataDefinition getDataDefinition(String name) {
        return null;
    }

    @Override
    public List<DocumentDefinition> getDocumentDefinitions() {
        return null;
    }

    @Override
    public List<ConnectorDefinition> getConnectors() {
        return null;
    }

    @Override
    public FlowNodeDefinition getFlowNode(long sourceId) {
        return null;
    }

    @Override
    public FlowNodeDefinition getFlowNode(String sourceName) {
        return null;
    }

    @Override
    public List<BusinessDataDefinition> getBusinessDataDefinitions() {
        return null;
    }

    @Override
    public BusinessDataDefinition getBusinessDataDefinition(String name) {
        return null;
    }

    @Override
    public List<DocumentListDefinition> getDocumentListDefinitions() {
        return null;
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {

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
}
