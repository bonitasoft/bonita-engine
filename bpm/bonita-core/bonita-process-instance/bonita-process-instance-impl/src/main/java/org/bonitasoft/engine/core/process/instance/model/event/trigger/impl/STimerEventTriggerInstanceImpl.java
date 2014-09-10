/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model.event.trigger.impl;

import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.STimerType;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;

/**
 * @author Elias Ricken de Medeiros
 */
public class STimerEventTriggerInstanceImpl extends SEventTriggerInstanceImpl implements STimerEventTriggerInstance {

    private static final long serialVersionUID = -5079198882177095287L;

    private STimerType timerType;

    private long timerValue;

    public STimerEventTriggerInstanceImpl() {
    }

    public STimerEventTriggerInstanceImpl(final long eventInstanceId, final STimerType timerType, final long timerValue) {
        super(eventInstanceId);
        this.timerType = timerType;
        this.timerValue = timerValue;
    }

    @Override
    public String getDiscriminator() {
        return STimerEventTriggerInstance.class.getName();
    }

    @Override
    public STimerType getTimerType() {
        return timerType;
    }

    @Override
    public long getTimerValue() {
        return timerValue;
    }

    protected void setTimerType(final STimerType timerType) {
        this.timerType = timerType;
    }

    protected void setTimerValue(final long timerValue) {
        this.timerValue = timerValue;
    }

    @Override
    public SEventTriggerType getEventTriggerType() {
        return SEventTriggerType.TIMER;
    }

}
