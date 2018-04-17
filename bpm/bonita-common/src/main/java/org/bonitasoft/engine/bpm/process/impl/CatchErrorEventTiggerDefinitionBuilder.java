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
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.flownode.impl.internal.CatchErrorEventTriggerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.CatchEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;


/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class CatchErrorEventTiggerDefinitionBuilder extends FlowElementContainerBuilder {

    protected CatchErrorEventTiggerDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder,
            final FlowElementContainerDefinitionImpl container, final CatchEventDefinitionImpl event) {
        this(processDefinitionBuilder, container, event, null);
    }

    protected CatchErrorEventTiggerDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder,
            final FlowElementContainerDefinitionImpl container, final CatchEventDefinitionImpl event, final String errorCode) {
        super(container, processDefinitionBuilder);
        if (errorCode != null && errorCode.trim().isEmpty()) {
            getProcessBuilder().addError("The error code cannot be empty.");
        }
        final CatchErrorEventTriggerDefinitionImpl triggerDefinition = new CatchErrorEventTriggerDefinitionImpl(errorCode);
        event.addErrorEventTrigger(triggerDefinition);
    }

}
