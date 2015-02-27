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
import java.util.List;

import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.core.process.definition.model.SLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SActivityDefinitionImpl;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;

/**
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public abstract class SActivityDefinitionBinding extends SFlowNodeDefinitionBinding {

    private final List<SDataDefinition> dataDefinitions = new ArrayList<SDataDefinition>();

    private final List<SOperation> operations = new ArrayList<SOperation>();

    private SLoopCharacteristics loopCharacteristics;

    private final List<SBoundaryEventDefinition> boundaryEventDefinitions = new ArrayList<SBoundaryEventDefinition>(1);

    private final List<SBusinessDataDefinition> businessDataDefinitions = new ArrayList<SBusinessDataDefinition>(3);

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLSProcessDefinition.DATA_DEFINITION_NODE.equals(name)) {
            dataDefinitions.add((SDataDefinition) value);
        } else if (XMLSProcessDefinition.TEXT_DATA_DEFINITION_NODE.equals(name)) {
            dataDefinitions.add((SDataDefinition) value);
        } else if (XMLSProcessDefinition.XML_DATA_DEFINITION_NODE.equals(name)) {
            dataDefinitions.add((SDataDefinition) value);
        } else if (XMLSProcessDefinition.OPERATION_NODE.equals(name)) {
            operations.add((SOperation) value);
        } else if (XMLSProcessDefinition.STANDARD_LOOP_CHARACTERISTICS_NODE.equals(name)
                || XMLSProcessDefinition.MULTI_INSTANCE_LOOP_CHARACTERISTICS_NODE.equals(name)) {
            loopCharacteristics = (SLoopCharacteristics) value;
        } else if (XMLSProcessDefinition.BOUNDARY_EVENT_NODE.equals(name)) {
            boundaryEventDefinitions.add((SBoundaryEventDefinition) value);
        } else if (XMLSProcessDefinition.BUSINESS_DATA_DEFINITION_NODE.equals(name)) {
            businessDataDefinitions.add((SBusinessDataDefinition) value);
        }
    }

    protected void fillNode(final SActivityDefinition sActivityDefinition) {
        super.fillNode(sActivityDefinition);
        if (sActivityDefinition instanceof SActivityDefinitionImpl) {
            final SActivityDefinitionImpl activity = (SActivityDefinitionImpl) sActivityDefinition;
            for (final SDataDefinition sDataDefinition : dataDefinitions) {
                activity.addSDataDefinition(sDataDefinition);
            }
            for (final SOperation operation : operations) {
                activity.addSOperation(operation);
            }
            activity.setLoopCharacteristics(loopCharacteristics);
            for (final SBoundaryEventDefinition boundaryEvent : boundaryEventDefinitions) {
                activity.addBoundaryEventDefinition(boundaryEvent);
            }
            for (final SBusinessDataDefinition businessDataDefinition : businessDataDefinitions) {
                activity.addBusinessDataDefinition(businessDataDefinition);
            }
        }
    }

}
