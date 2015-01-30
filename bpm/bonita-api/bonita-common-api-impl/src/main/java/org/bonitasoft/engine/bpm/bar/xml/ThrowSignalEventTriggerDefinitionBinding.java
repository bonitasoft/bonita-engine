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

import org.bonitasoft.engine.bpm.flownode.impl.internal.ThrowSignalEventTriggerDefinitionImpl;

/**
 * @author Matthieu Chaffotte
 */
public class ThrowSignalEventTriggerDefinitionBinding extends SignalEventTriggerDefinitionBinding {

    @Override
    public Object getObject() {
        final String signalName = getSignalName();
        return new ThrowSignalEventTriggerDefinitionImpl(signalName);
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.THROW_SIGNAL_EVENT_TRIGGER_NODE;
    }

}
