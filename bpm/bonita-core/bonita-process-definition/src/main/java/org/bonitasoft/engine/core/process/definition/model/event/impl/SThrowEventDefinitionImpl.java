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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.ThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowSignalEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SThrowEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowSignalEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SThrowMessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SThrowSignalEventTriggerDefinitionImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class SThrowEventDefinitionImpl extends SEventDefinitionImpl implements SThrowEventDefinition {

    private static final long serialVersionUID = -3682882065277857160L;

    private final List<SThrowMessageEventTriggerDefinition> sMessageEventTriggerDefinitions;

    private final List<SThrowSignalEventTriggerDefinition> sSignalEventTriggerDefinitions;

    public SThrowEventDefinitionImpl(final ThrowEventDefinition eventDefinition,
            final Map<String, STransitionDefinition> transitionsMap) {
        super(eventDefinition, transitionsMap);

        final List<ThrowMessageEventTriggerDefinition> messageEventTriggerDefinitions = eventDefinition.getMessageEventTriggerDefinitions();
        sMessageEventTriggerDefinitions = new ArrayList<SThrowMessageEventTriggerDefinition>(messageEventTriggerDefinitions.size());
        for (final ThrowMessageEventTriggerDefinition throwMessageEventTriggerDefinition : messageEventTriggerDefinitions) {
            addMessageEventTriggerDefinition(new SThrowMessageEventTriggerDefinitionImpl(throwMessageEventTriggerDefinition));
        }

        final List<ThrowSignalEventTriggerDefinition> signalEventTriggerDefinitions = eventDefinition.getSignalEventTriggerDefinitions();
        sSignalEventTriggerDefinitions = new ArrayList<SThrowSignalEventTriggerDefinition>(signalEventTriggerDefinitions.size());
        for (final ThrowSignalEventTriggerDefinition throwSignalEventTriggerDefinition : signalEventTriggerDefinitions) {
            addSignalEventTriggerDefinition(new SThrowSignalEventTriggerDefinitionImpl(throwSignalEventTriggerDefinition.getSignalName()));
        }
    }

    public SThrowEventDefinitionImpl(final long id, final String name) {
        super(id, name);
        sMessageEventTriggerDefinitions = new ArrayList<SThrowMessageEventTriggerDefinition>(5);
        sSignalEventTriggerDefinitions = new ArrayList<SThrowSignalEventTriggerDefinition>(1);
    }

    @Override
    public List<SThrowMessageEventTriggerDefinition> getMessageEventTriggerDefinitions() {
        return Collections.unmodifiableList(sMessageEventTriggerDefinitions);
    }

    public void addMessageEventTriggerDefinition(final SThrowMessageEventTriggerDefinition messageEventTriggerDefinition) {
        addEventTriggerDefinition(messageEventTriggerDefinition);
        sMessageEventTriggerDefinitions.add(messageEventTriggerDefinition);
    }

    @Override
    public SThrowMessageEventTriggerDefinition getMessageEventTriggerDefinition(final String messageName) {
        final Iterator<SThrowMessageEventTriggerDefinition> iterator = sMessageEventTriggerDefinitions.iterator();
        boolean found = false;
        SThrowMessageEventTriggerDefinition messageTrigger = null;
        while (iterator.hasNext() && !found) {
            final SThrowMessageEventTriggerDefinition sThrowMessageEventTriggerDefinition = iterator.next();
            if (sThrowMessageEventTriggerDefinition.getMessageName().equals(messageName)) {
                found = true;
                messageTrigger = sThrowMessageEventTriggerDefinition;
            }
        }
        return messageTrigger;
    }

    @Override
    public List<SThrowSignalEventTriggerDefinition> getSignalEventTriggerDefinitions() {
        return Collections.unmodifiableList(sSignalEventTriggerDefinitions);
    }

    public void addSignalEventTriggerDefinition(final SThrowSignalEventTriggerDefinition signalEventTriggerDefinition) {
        addEventTriggerDefinition(signalEventTriggerDefinition);
        sSignalEventTriggerDefinitions.add(signalEventTriggerDefinition);
    }

}
