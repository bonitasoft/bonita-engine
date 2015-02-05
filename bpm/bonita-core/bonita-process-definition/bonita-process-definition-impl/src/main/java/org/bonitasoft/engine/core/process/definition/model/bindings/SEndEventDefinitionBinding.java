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

import org.bonitasoft.engine.core.process.definition.model.event.impl.SEndEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.STerminateEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowErrorEventTriggerDefinition;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SEndEventDefinitionBinding extends SThrowEventDefinitionBinding {

    private STerminateEventTriggerDefinition terminateEventTrigger;

    private final List<SThrowErrorEventTriggerDefinition> errorEventTriggers;

    public SEndEventDefinitionBinding() {
        super();
        errorEventTriggers = new ArrayList<SThrowErrorEventTriggerDefinition>(1);
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLSProcessDefinition.TERMINATE_EVENT_TRIGGER_NODE.equals(name)) {
            terminateEventTrigger = (STerminateEventTriggerDefinition) value;
        } else if (XMLSProcessDefinition.THROW_ERROR_EVENT_TRIGGER_NODE.equals(name)) {
            errorEventTriggers.add((SThrowErrorEventTriggerDefinition) value);
        }
    }

    @Override
    public Object getObject() {
        final SEndEventDefinitionImpl endEventDefinitionImpl = new SEndEventDefinitionImpl(id, name);
        fillNode(endEventDefinitionImpl);
        endEventDefinitionImpl.setTerminateEventTriggerDefinition(terminateEventTrigger);

        for (final SThrowErrorEventTriggerDefinition errorEventTrigger : errorEventTriggers) {
            endEventDefinitionImpl.addErrorEventTriggerDefinition(errorEventTrigger);
        }

        return endEventDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.END_EVENT_NODE;
    }

}
