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

import org.bonitasoft.engine.bpm.flownode.EventDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowNodeDefinitionImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public abstract class SEventDefinitionImpl extends SFlowNodeDefinitionImpl implements SEventDefinition {

    private static final long serialVersionUID = -5019901548085906144L;

    private final List<SEventTriggerDefinition> eventTriggers;

    public SEventDefinitionImpl(final EventDefinition eventDefinition,
            final Map<String, STransitionDefinition> transitionsMap) {
        super(eventDefinition, transitionsMap);
        // initialize the list have an initial capacity of 1: most of time there will be zero or one event trigger
        eventTriggers = new ArrayList<SEventTriggerDefinition>(1);
    }

    public SEventDefinitionImpl(final long id, final String name) {
        super(id, name);
        eventTriggers = new ArrayList<SEventTriggerDefinition>(1);
    }

    @Override
    public List<SEventTriggerDefinition> getEventTriggers() {
        return Collections.unmodifiableList(eventTriggers);
    }

    protected void addEventTriggerDefinition(final SEventTriggerDefinition eventTrigger) {
        eventTriggers.add(eventTrigger);
    }

}
