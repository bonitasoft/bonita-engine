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

import org.bonitasoft.engine.bpm.flownode.CatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.ErrorEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.SignalEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SCatchEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCatchErrorEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCatchSignalEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.STimerEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SCatchErrorEventTriggerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SCatchMessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SCatchSignalEventTriggerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.STimerEventTriggerDefinitionImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class SCatchEventDefinitionImpl extends SEventDefinitionImpl implements SCatchEventDefinition {

    private static final long serialVersionUID = -2803848099720886033L;

    private final List<STimerEventTriggerDefinition> timerEventTriggers;

    private final List<SCatchMessageEventTriggerDefinition> messageEventTriggers;

    private final List<SCatchSignalEventTriggerDefinition> signalEventTriggers;

    private final List<SCatchErrorEventTriggerDefinition> errorEventTriggers;

    private boolean isInterrupting = true;

    public SCatchEventDefinitionImpl(final CatchEventDefinition eventDefinition,
            final Map<String, STransitionDefinition> transitionsMap) {
        super(eventDefinition, transitionsMap);
        isInterrupting = eventDefinition.isInterrupting();
        timerEventTriggers = new ArrayList<STimerEventTriggerDefinition>(eventDefinition.getTimerEventTriggerDefinitions().size());
        for (final TimerEventTriggerDefinition timerTrigger : eventDefinition.getTimerEventTriggerDefinitions()) {
            addTimerEventTrigger(new STimerEventTriggerDefinitionImpl(timerTrigger));
        }
        messageEventTriggers = new ArrayList<SCatchMessageEventTriggerDefinition>(eventDefinition.getMessageEventTriggerDefinitions().size());
        for (final CatchMessageEventTriggerDefinition catchMessageTrigger : eventDefinition.getMessageEventTriggerDefinitions()) {
            addMessageEventTrigger(new SCatchMessageEventTriggerDefinitionImpl(catchMessageTrigger));
        }
        signalEventTriggers = new ArrayList<SCatchSignalEventTriggerDefinition>(eventDefinition.getSignalEventTriggerDefinitions().size());
        for (final SignalEventTriggerDefinition signalTrigger : eventDefinition.getSignalEventTriggerDefinitions()) {
            addSignalEventTrigger(new SCatchSignalEventTriggerDefinitionImpl(signalTrigger.getSignalName()));
        }

        errorEventTriggers = new ArrayList<SCatchErrorEventTriggerDefinition>(eventDefinition.getErrorEventTriggerDefinitions().size());
        for (final ErrorEventTriggerDefinition errorTrigger : eventDefinition.getErrorEventTriggerDefinitions()) {
            addErrorEventTrigger(new SCatchErrorEventTriggerDefinitionImpl(errorTrigger.getErrorCode()));
        }
    }

    public SCatchEventDefinitionImpl(final long id, final String name) {
        super(id, name);
        timerEventTriggers = new ArrayList<STimerEventTriggerDefinition>(1);
        messageEventTriggers = new ArrayList<SCatchMessageEventTriggerDefinition>(5);
        signalEventTriggers = new ArrayList<SCatchSignalEventTriggerDefinition>(1);
        errorEventTriggers = new ArrayList<SCatchErrorEventTriggerDefinition>(1);
    }

    @Override
    public List<STimerEventTriggerDefinition> getTimerEventTriggerDefinitions() {
        return Collections.unmodifiableList(timerEventTriggers);
    }

    public void addTimerEventTrigger(final STimerEventTriggerDefinition timerEventTrigger) {
        timerEventTriggers.add(timerEventTrigger);
        addEventTriggerDefinition(timerEventTrigger);
    }

    @Override
    public List<SCatchMessageEventTriggerDefinition> getMessageEventTriggerDefinitions() {
        return Collections.unmodifiableList(messageEventTriggers);
    }

    public void addMessageEventTrigger(final SCatchMessageEventTriggerDefinition messageEventTrigger) {
        messageEventTriggers.add(messageEventTrigger);
        addEventTriggerDefinition(messageEventTrigger);
    }

    @Override
    public SCatchMessageEventTriggerDefinition getMessageEventTriggerDefinition(final String messageName) {
        final Iterator<SCatchMessageEventTriggerDefinition> iterator = messageEventTriggers.iterator();
        boolean found = false;
        SCatchMessageEventTriggerDefinition messageTrigger = null;
        while (iterator.hasNext() && !found) {
            final SCatchMessageEventTriggerDefinition sCatchMessageEventTriggerDefinition = iterator.next();
            if (sCatchMessageEventTriggerDefinition.getMessageName().equals(messageName)) {
                found = true;
                messageTrigger = sCatchMessageEventTriggerDefinition;
            }

        }
        return messageTrigger;
    }

    @Override
    public List<SCatchSignalEventTriggerDefinition> getSignalEventTriggerDefinitions() {
        return Collections.unmodifiableList(signalEventTriggers);
    }

    public void addSignalEventTrigger(final SCatchSignalEventTriggerDefinition signalEventTrigger) {
        signalEventTriggers.add(signalEventTrigger);
        addEventTriggerDefinition(signalEventTrigger);
    }

    public void setInterrupting(final boolean isInterrupting) {
        this.isInterrupting = isInterrupting;
    }

    @Override
    public boolean isInterrupting() {
        return isInterrupting;
    }

    @Override
    public List<SCatchErrorEventTriggerDefinition> getErrorEventTriggerDefinitions() {
        return Collections.unmodifiableList(errorEventTriggers);
    }

    @Override
    public SCatchErrorEventTriggerDefinition getErrorEventTriggerDefinition(final String errorCode) {
        final Iterator<SCatchErrorEventTriggerDefinition> iterator = errorEventTriggers.iterator();
        boolean found = false;
        SCatchErrorEventTriggerDefinition trigger = null;
        while (iterator.hasNext() && !found) {
            final SCatchErrorEventTriggerDefinition currentTrigger = iterator.next();
            if (currentTrigger.getErrorCode() == null && errorCode == null || currentTrigger.getErrorCode() != null
                    && currentTrigger.getErrorCode().equals(errorCode)) {
                found = true;
                trigger = currentTrigger;
            }

        }
        return trigger;
    }

    public void addErrorEventTrigger(final SCatchErrorEventTriggerDefinition errorEventTrigger) {
        errorEventTriggers.add(errorEventTrigger);
        addEventTriggerDefinition(errorEventTrigger);
    }

}
