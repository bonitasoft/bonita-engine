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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

/**
 * @author Elias Ricken de Medeiros
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TimerEventTriggerDefinitionImpl implements TimerEventTriggerDefinition {

    private static final long serialVersionUID = -1000995843357026775L;
    @XmlAttribute(name = "type")
    private final TimerType timerType;
    @XmlElement(type = ExpressionImpl.class,name = "expression")
    private final Expression timerValue;

    public TimerEventTriggerDefinitionImpl(final TimerType timerType, final Expression timerExpression) {
        this.timerType = timerType;
        timerValue = timerExpression;
    }

    public TimerEventTriggerDefinitionImpl() {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimerEventTriggerDefinitionImpl that = (TimerEventTriggerDefinitionImpl) o;
        return Objects.equals(timerType, that.timerType) &&
                Objects.equals(timerValue, that.timerValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timerType, timerValue);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("timerType", timerType)
                .append("timerValue", timerValue)
                .toString();
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }

}
