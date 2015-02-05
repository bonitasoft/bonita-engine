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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.impl.internal.ReceiveTaskDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SReceiveTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SCatchMessageEventTriggerDefinitionImpl;

/**
 * @author Julien Molinaro
 * @author Celine Souchet
 */
public class SReceiveTaskDefinitionImpl extends SActivityDefinitionImpl implements SReceiveTaskDefinition {

    private static final long serialVersionUID = 8112705930442175231L;

    private final SCatchMessageEventTriggerDefinitionImpl trigger;

    public SReceiveTaskDefinitionImpl(final ReceiveTaskDefinitionImpl activityDefinition,
            final Map<String, STransitionDefinition> transitionsMap) {
        super(activityDefinition, transitionsMap);
        trigger = new SCatchMessageEventTriggerDefinitionImpl(activityDefinition.getTrigger());
    }

    public SReceiveTaskDefinitionImpl(final long id, final String name, final SCatchMessageEventTriggerDefinition catchMessageEventTriggerDefinition) {
        super(id, name);
        this.trigger = new SCatchMessageEventTriggerDefinitionImpl(catchMessageEventTriggerDefinition);
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.RECEIVE_TASK;
    }

    @Override
    public SCatchMessageEventTriggerDefinition getTrigger() {
        return trigger;
    }

}
