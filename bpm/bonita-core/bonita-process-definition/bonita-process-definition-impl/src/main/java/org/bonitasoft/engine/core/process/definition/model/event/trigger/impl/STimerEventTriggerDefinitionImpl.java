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
package org.bonitasoft.engine.core.process.definition.model.event.trigger.impl;

import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.STimerEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.STimerType;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 */
public class STimerEventTriggerDefinitionImpl extends SEventTriggerDefinitionImpl implements STimerEventTriggerDefinition {

    private static final long serialVersionUID = 1240658984583267877L;

    private final STimerType timerType;

    private final SExpression timerExpression;

    public STimerEventTriggerDefinitionImpl(final TimerEventTriggerDefinition timerTrigger) {
        timerType = STimerType.valueOf(timerTrigger.getTimerType().name());
        final Expression expression = timerTrigger.getTimerExpression();
        timerExpression = ServerModelConvertor.convertExpression(expression);
    }

    public STimerEventTriggerDefinitionImpl(final STimerType timerType, final SExpression timerExpression) {
        this.timerType = timerType;
        this.timerExpression = timerExpression;
    }

    @Override
    public STimerType getTimerType() {
        return timerType;
    }

    @Override
    public SExpression getTimerExpression() {
        return timerExpression;
    }

    @Override
    public SEventTriggerType getEventTriggerType() {
        return SEventTriggerType.TIMER;
    }

}
