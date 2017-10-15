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
package org.bonitasoft.engine.core.process.definition.model.event.trigger.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 */
public class SThrowMessageEventTriggerDefinitionImpl extends SMessageEventTriggerDefinitionImpl implements SThrowMessageEventTriggerDefinition {

    private static final long serialVersionUID = -513177194601607560L;

    private SExpression targetProcess;

    private SExpression targetFlowNode;

    private final List<SDataDefinition> sDataDefinitions;

    public SThrowMessageEventTriggerDefinitionImpl() {
        sDataDefinitions = new ArrayList<SDataDefinition>();
    }

    public SThrowMessageEventTriggerDefinitionImpl(final ThrowMessageEventTriggerDefinition throwMessageEventTrigger) {
        super(throwMessageEventTrigger);
        final List<DataDefinition> dataDefinitions = throwMessageEventTrigger.getDataDefinitions();
        sDataDefinitions = new ArrayList<SDataDefinition>(dataDefinitions.size());
        for (final DataDefinition dataDefinition : dataDefinitions) {
            sDataDefinitions.add(buildSDataDefinition(dataDefinition));
        }
        targetProcess = ServerModelConvertor.convertExpression(throwMessageEventTrigger.getTargetProcess());
        targetFlowNode = ServerModelConvertor.convertExpression(throwMessageEventTrigger.getTargetFlowNode());
    }

    @Override
    public SExpression getTargetProcess() {
        return targetProcess;
    }

    @Override
    public SExpression getTargetFlowNode() {
        return targetFlowNode;
    }

    @Override
    public List<SDataDefinition> getDataDefinitions() {
        return Collections.unmodifiableList(sDataDefinitions);
    }

    public void setTargetProcess(final SExpression targetProcess) {
        this.targetProcess = targetProcess;
    }

    public void setTargetFlowNode(final SExpression targetFlowNode) {
        this.targetFlowNode = targetFlowNode;
    }

    public void addDataDefinition(final SDataDefinition datadefiniton) {
        sDataDefinitions.add(datadefiniton);
    }

}
