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

import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SThrowMessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 */
public class SThrowMessageEventTriggerDefinitionBinding extends SMessageEventTriggerDefinitionBinding {

    private SExpression targetProcess;

    private SExpression targetFlowNode;

    private final List<SDataDefinition> dataDefinitions = new ArrayList<SDataDefinition>(5);

    @Override
    public Object getObject() {
        final SThrowMessageEventTriggerDefinitionImpl messageEventTrigger = new SThrowMessageEventTriggerDefinitionImpl();
        fillNode(messageEventTrigger);
        return messageEventTrigger;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.THROW_MESSAGE_EVENT_TRIGGER_NODE;
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLSProcessDefinition.DATA_DEFINITION_NODE.equals(name)) {
            dataDefinitions.add((SDataDefinition) value);
        } else if (XMLSProcessDefinition.TEXT_DATA_DEFINITION_NODE.equals(name)) {
            dataDefinitions.add((SDataDefinition) value);
        } else if (XMLSProcessDefinition.XML_DATA_DEFINITION_NODE.equals(name)) {
            dataDefinitions.add((SDataDefinition) value);
        } else if (XMLProcessDefinition.TARGET_PROCESS.equals(name)) {
            targetProcess = (SExpression) value;
        } else if (XMLProcessDefinition.TARGET_FLOW_NODE.equals(name)) {
            targetFlowNode = (SExpression) value;
        }
    }

    protected void fillNode(final SThrowMessageEventTriggerDefinitionImpl messageEventTrigger) {
        super.fillNode(messageEventTrigger);
        messageEventTrigger.setTargetProcess(targetProcess);
        messageEventTrigger.setTargetFlowNode(targetFlowNode);
        for (final SDataDefinition dataDefinition : dataDefinitions) {
            messageEventTrigger.addDataDefinition(dataDefinition);
        }
    }

}
