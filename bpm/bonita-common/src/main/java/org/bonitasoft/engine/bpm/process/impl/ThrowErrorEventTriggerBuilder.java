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

import org.bonitasoft.engine.bpm.flownode.impl.internal.EndEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ThrowErrorEventTriggerDefinitionImpl;


/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class ThrowErrorEventTriggerBuilder extends FlowElementContainerBuilder {

    ThrowErrorEventTriggerBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final EndEventDefinitionImpl event, final String errorCode) {
        super(container, processDefinitionBuilder);
        final ThrowErrorEventTriggerDefinitionImpl errorEventTrigger = new ThrowErrorEventTriggerDefinitionImpl(errorCode);
        event.addErrorEventTriggerDefinition(errorEventTrigger);
        if (errorCode == null || errorCode.length() == 0) {
            processDefinitionBuilder.addError("The error code cannot be empty in a throw error event: " + event.getName());
        }
    }

}
