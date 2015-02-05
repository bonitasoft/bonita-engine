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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.CatchErrorEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchSignalEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerDefinition;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class CatchEventDefinitionImpl extends EventDefinitionImpl implements CatchEventDefinition {

    private static final long serialVersionUID = 250215494961033080L;

    private final List<TimerEventTriggerDefinition> timerEventTriggers;

    private final List<CatchMessageEventTriggerDefinition> messageEventTriggers;

    private final List<CatchSignalEventTriggerDefinition> signalEventTriggers;

    private final List<CatchErrorEventTriggerDefinition> errorEventTriggers;

    private boolean isInterrupting = true;

    public CatchEventDefinitionImpl(final String name) {
        super(name);
        timerEventTriggers = new ArrayList<TimerEventTriggerDefinition>(1);
        messageEventTriggers = new ArrayList<CatchMessageEventTriggerDefinition>(1);
        signalEventTriggers = new ArrayList<CatchSignalEventTriggerDefinition>(1);
        errorEventTriggers = new ArrayList<CatchErrorEventTriggerDefinition>(1);
    }

    public CatchEventDefinitionImpl(final long id, final String name) {
        super(id, name);
        timerEventTriggers = new ArrayList<TimerEventTriggerDefinition>(1);
        messageEventTriggers = new ArrayList<CatchMessageEventTriggerDefinition>(1);
        signalEventTriggers = new ArrayList<CatchSignalEventTriggerDefinition>(1);
        errorEventTriggers = new ArrayList<CatchErrorEventTriggerDefinition>(1);
    }

    @Override
    public List<TimerEventTriggerDefinition> getTimerEventTriggerDefinitions() {
        return Collections.unmodifiableList(timerEventTriggers);
    }

    public void addTimerEventTrigger(final TimerEventTriggerDefinition timerEventDefinition) {
        timerEventTriggers.add(timerEventDefinition);
        addEventTrigger(timerEventDefinition);
    }

    @Override
    public List<CatchMessageEventTriggerDefinition> getMessageEventTriggerDefinitions() {
        return Collections.unmodifiableList(messageEventTriggers);
    }

    public void addMessageEventTrigger(final CatchMessageEventTriggerDefinition messageEventTrigger) {
        messageEventTriggers.add(messageEventTrigger);
        addEventTrigger(messageEventTrigger);
    }

    @Override
    public List<CatchSignalEventTriggerDefinition> getSignalEventTriggerDefinitions() {
        return Collections.unmodifiableList(signalEventTriggers);
    }

    public void addSignalEventTrigger(final CatchSignalEventTriggerDefinition signalEventTrigger) {
        signalEventTriggers.add(signalEventTrigger);
        addEventTrigger(signalEventTrigger);
    }

    public void addErrorEventTrigger(final CatchErrorEventTriggerDefinition errorEventTrigger) {
        errorEventTriggers.add(errorEventTrigger);
        addEventTrigger(errorEventTrigger);
    }

    @Override
    public List<CatchErrorEventTriggerDefinition> getErrorEventTriggerDefinitions() {
        return Collections.unmodifiableList(errorEventTriggers);
    }

    public void setInterrupting(final boolean isInterrupting) {
        this.isInterrupting = isInterrupting;
    }

    @Override
    public boolean isInterrupting() {
        return isInterrupting;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (errorEventTriggers == null ? 0 : errorEventTriggers.hashCode());
        result = prime * result + (isInterrupting ? 1231 : 1237);
        result = prime * result + (messageEventTriggers == null ? 0 : messageEventTriggers.hashCode());
        result = prime * result + (signalEventTriggers == null ? 0 : signalEventTriggers.hashCode());
        result = prime * result + (timerEventTriggers == null ? 0 : timerEventTriggers.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CatchEventDefinitionImpl other = (CatchEventDefinitionImpl) obj;
        if (errorEventTriggers == null) {
            if (other.errorEventTriggers != null) {
                return false;
            }
        } else if (!errorEventTriggers.equals(other.errorEventTriggers)) {
            return false;
        }
        if (isInterrupting != other.isInterrupting) {
            return false;
        }
        if (messageEventTriggers == null) {
            if (other.messageEventTriggers != null) {
                return false;
            }
        } else if (!messageEventTriggers.equals(other.messageEventTriggers)) {
            return false;
        }
        if (signalEventTriggers == null) {
            if (other.signalEventTriggers != null) {
                return false;
            }
        } else if (!signalEventTriggers.equals(other.signalEventTriggers)) {
            return false;
        }
        if (timerEventTriggers == null) {
            if (other.timerEventTriggers != null) {
                return false;
            }
        } else if (!timerEventTriggers.equals(other.timerEventTriggers)) {
            return false;
        }
        return true;
    }

}
