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

import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.SendTaskDefinitionImpl;

/**
 * @author Matthieu Chaffotte
 */
public class SendTaskDefinitionBinding extends ActivityDefinitionBinding {

    private ThrowMessageEventTriggerDefinition trigger;

    @Override
    public Object getObject() {
        final SendTaskDefinitionImpl sendTask = new SendTaskDefinitionImpl(id, name, trigger);
        fillNode(sendTask);
        return sendTask;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.SEND_TASK_NODE;
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLProcessDefinition.THROW_MESSAGE_EVENT_TRIGGER_NODE.equals(name)) {
            trigger = (ThrowMessageEventTriggerDefinition) value;
        }
    }

}
