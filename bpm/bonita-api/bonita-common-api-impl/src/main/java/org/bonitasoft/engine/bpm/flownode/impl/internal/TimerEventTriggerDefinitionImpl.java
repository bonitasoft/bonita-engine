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

import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Elias Ricken de Medeiros
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TimerEventTriggerDefinitionImpl implements TimerEventTriggerDefinition {

    private static final long serialVersionUID = -1000995843357026775L;

    private final TimerType timerType;
    @XmlElement(type = ExpressionImpl.class)
    private final Expression timerValue;

    public TimerEventTriggerDefinitionImpl(final TimerType timerType, final Expression timerExpression) {
        this.timerType = timerType;
        timerValue = timerExpression;
    }

    public TimerEventTriggerDefinitionImpl(){
        this.timerType = TimerType.CYCLE;
        timerValue = new ExpressionImpl();
    }
    @Override
    public TimerType getTimerType() {
        return timerType;
    }

    @Override
    public Expression getTimerExpression() {
        return timerValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (timerType == null ? 0 : timerType.hashCode());
        result = prime * result + (timerValue == null ? 0 : timerValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimerEventTriggerDefinitionImpl other = (TimerEventTriggerDefinitionImpl) obj;
        if (timerType != other.timerType) {
            return false;
        }
        if (timerValue == null) {
            if (other.timerValue != null) {
                return false;
            }
        } else if (!timerValue.equals(other.timerValue)) {
            return false;
        }
        return true;
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }

}
