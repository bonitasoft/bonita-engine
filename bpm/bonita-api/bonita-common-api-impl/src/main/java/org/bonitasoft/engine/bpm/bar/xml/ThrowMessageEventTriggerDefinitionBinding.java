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

import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ThrowMessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public class ThrowMessageEventTriggerDefinitionBinding extends MessageEventTriggerDefinitionBinding {

    private Expression targetProcess;

    private Expression targetFlowNode;

    private final List<DataDefinition> dataDefinitions = new ArrayList<DataDefinition>(5);

    @Override
    public Object getObject() {
        final ThrowMessageEventTriggerDefinitionImpl messageEventTrigger = new ThrowMessageEventTriggerDefinitionImpl(getMessageName());
        fillNode(messageEventTrigger);
        return messageEventTrigger;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.THROW_MESSAGE_EVENT_TRIGGER_NODE;
    }

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
        if (XMLProcessDefinition.TARGET_PROCESS.equals(name)) {
            targetProcess = (Expression) value;
        }
        if (XMLProcessDefinition.TARGET_FLOW_NODE.equals(name)) {
            targetFlowNode = (Expression) value;
        }
    }

    protected void fillNode(final ThrowMessageEventTriggerDefinitionImpl messageEventTrigger) {
        super.fillNode(messageEventTrigger);
        messageEventTrigger.setTargetProcess(targetProcess);
        messageEventTrigger.setTargetFlowNode(targetFlowNode);
        for (final DataDefinition dataDefinition : dataDefinitions) {
            messageEventTrigger.addDataDefinition(dataDefinition);
        }
    }

}
