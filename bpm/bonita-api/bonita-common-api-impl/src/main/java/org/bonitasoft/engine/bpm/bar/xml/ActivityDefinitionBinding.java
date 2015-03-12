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

import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.LoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowNodeDefinitionImpl;
import org.bonitasoft.engine.operation.Operation;

/**
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public abstract class ActivityDefinitionBinding extends FlowNodeDefinitionBinding {

    protected final List<DataDefinition> dataDefinitions = new ArrayList<DataDefinition>();

    private final List<BusinessDataDefinition> businessDataDefinitions = new ArrayList<BusinessDataDefinition>();

    protected final List<Operation> operations = new ArrayList<Operation>();

    protected LoopCharacteristics loopCharacteristics;

    private final List<BoundaryEventDefinition> boundaryEventDefinitions = new ArrayList<BoundaryEventDefinition>(1);

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLProcessDefinition.DATA_DEFINITION_NODE.equals(name)) {
            dataDefinitions.add((DataDefinition) value);
        }
        if (XMLProcessDefinition.TEXT_DATA_DEFINITION_NODE.equals(name)) {
            dataDefinitions.add((DataDefinition) value);
        }
        if (XMLProcessDefinition.XML_DATA_DEFINITION_NODE.equals(name)) {
            dataDefinitions.add((DataDefinition) value);
        }
        if (XMLProcessDefinition.OPERATION_NODE.equals(name)) {
            operations.add((Operation) value);
        }
        if (XMLProcessDefinition.STANDARD_LOOP_CHARACTERISTICS_NODE.equals(name) || XMLProcessDefinition.MULTI_INSTANCE_LOOP_CHARACTERISTICS_NODE.equals(name)) {
            loopCharacteristics = (LoopCharacteristics) value;
        }
        if (XMLProcessDefinition.BOUNDARY_EVENT_NODE.equals(name)) {
            boundaryEventDefinitions.add((BoundaryEventDefinition) value);
        }
        if (XMLProcessDefinition.BUSINESS_DATA_DEFINITION_NODE.equals(name)) {
            businessDataDefinitions.add((BusinessDataDefinition) value);
        }
    }

    @Override
    protected void fillNode(final FlowNodeDefinitionImpl flowNode) {
        super.fillNode(flowNode);
        if (flowNode instanceof ActivityDefinitionImpl) {
            final ActivityDefinitionImpl activity = (ActivityDefinitionImpl) flowNode;
            for (final DataDefinition dataDefinition : dataDefinitions) {
                activity.addDataDefinition(dataDefinition);
            }
            for (final Operation operation : operations) {
                activity.addOperation(operation);
            }
            activity.setLoopCharacteristics(loopCharacteristics);
            for (final BoundaryEventDefinition boundaryEvent : boundaryEventDefinitions) {
                activity.addBoundaryEventDefinition(boundaryEvent);
            }
            for (final BusinessDataDefinition businessDataDefinition : businessDataDefinitions) {
                activity.addBusinessDataDefinition(businessDataDefinition);
            }
        }
    }

}
