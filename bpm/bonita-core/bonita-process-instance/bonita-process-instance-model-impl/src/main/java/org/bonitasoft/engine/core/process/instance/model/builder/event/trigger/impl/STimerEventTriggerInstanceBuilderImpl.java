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
package org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.impl;

import org.bonitasoft.engine.core.process.definition.model.event.trigger.STimerType;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.STimerEventTriggerInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.STimerEventTriggerInstanceImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class STimerEventTriggerInstanceBuilderImpl extends SEventTriggerInstanceBuilderImpl implements STimerEventTriggerInstanceBuilder {

    private static final String TIMER_VALUE_KEY = "timerValue";

    private static final String TIMER_TYPE_KEY = "timerType";

    private STimerEventTriggerInstanceImpl entity;

    @Override
    public STimerEventTriggerInstanceBuilder createNewTimerEventTriggerInstance(final long eventInstanceId, final STimerType timerType, final long timerValue) {
        this.entity = new STimerEventTriggerInstanceImpl(eventInstanceId, timerType, timerValue);
        return this;
    }

    @Override
    public STimerEventTriggerInstance done() {
        return this.entity;
    }

    @Override
    public String getTimerTypeKey() {
        return TIMER_TYPE_KEY;
    }

    @Override
    public String getTimerValueKey() {
        return TIMER_VALUE_KEY;
    }

}
