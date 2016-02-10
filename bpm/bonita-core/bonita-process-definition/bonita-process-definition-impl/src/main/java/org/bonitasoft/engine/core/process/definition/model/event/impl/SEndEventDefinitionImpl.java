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
package org.bonitasoft.engine.core.process.definition.model.event.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.EndEventDefinition;
import org.bonitasoft.engine.bpm.flownode.TerminateEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowErrorEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.STerminateEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowErrorEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.STerminateEventTriggerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SThrowErrorEventTriggerDefinitionImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SEndEventDefinitionImpl extends SThrowEventDefinitionImpl implements SEndEventDefinition {

    private static final long serialVersionUID = 6671309950777321636L;

    private STerminateEventTriggerDefinition sTerminateEventTriggerDefinition;

    private final List<SThrowErrorEventTriggerDefinition> sErrorEventTriggerDefinitions;

    public SEndEventDefinitionImpl(final EndEventDefinition eventDefinition,
            final Map<String, STransitionDefinition> transitionsMap) {
        super(eventDefinition, transitionsMap);
        final TerminateEventTriggerDefinition terminateEventTriggerDefinition = eventDefinition.getTerminateEventTriggerDefinition();
        if (terminateEventTriggerDefinition != null) {
            setTerminateEventTriggerDefinition(new STerminateEventTriggerDefinitionImpl());
        }
        final List<ThrowErrorEventTriggerDefinition> errorEventTriggerDefinitions = eventDefinition.getErrorEventTriggerDefinitions();
        sErrorEventTriggerDefinitions = new ArrayList<SThrowErrorEventTriggerDefinition>(errorEventTriggerDefinitions.size());
        for (final ThrowErrorEventTriggerDefinition throwErrorEventTriggerDefinition : errorEventTriggerDefinitions) {
            addErrorEventTriggerDefinition(new SThrowErrorEventTriggerDefinitionImpl(throwErrorEventTriggerDefinition.getErrorCode()));
        }
    }

    public SEndEventDefinitionImpl(final long id, final String name) {
        super(id, name);
        sErrorEventTriggerDefinitions = new ArrayList<SThrowErrorEventTriggerDefinition>(1);
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.END_EVENT;
    }

    @Override
    public STerminateEventTriggerDefinition getTerminateEventTriggerDefinition() {
        return sTerminateEventTriggerDefinition;
    }

    public void setTerminateEventTriggerDefinition(final STerminateEventTriggerDefinition sTerminateEventTriggerDefinition) {
        this.sTerminateEventTriggerDefinition = sTerminateEventTriggerDefinition;
        if (sTerminateEventTriggerDefinition != null) {
            addEventTriggerDefinition(sTerminateEventTriggerDefinition);
        }
    }

    @Override
    public List<SThrowErrorEventTriggerDefinition> getErrorEventTriggerDefinitions() {
        return Collections.unmodifiableList(sErrorEventTriggerDefinitions);
    }

    public void addErrorEventTriggerDefinition(final SThrowErrorEventTriggerDefinition errorEventTrigger) {
        sErrorEventTriggerDefinitions.add(errorEventTrigger);
        addEventTriggerDefinition(errorEventTrigger);
    }

}
