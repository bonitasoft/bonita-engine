/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static org.bonitasoft.engine.expression.ExpressionBuilder.getNonNullCopy;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

/**
 * @author Elias Ricken de Medeiros
 */
@EqualsAndHashCode
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
public class TimerEventTriggerDefinitionImpl implements TimerEventTriggerDefinition {

    private static final long serialVersionUID = -1000995843357026775L;

    @Getter
    @XmlAttribute(name = "type")
    private final TimerType timerType;
    @XmlElement(type = ExpressionImpl.class, name = "expression")
    private final Expression timerValue;

    public TimerEventTriggerDefinitionImpl(final TimerType timerType, final Expression timerExpression) {
        this.timerType = timerType;
        timerValue = getNonNullCopy(timerExpression);
    }

    public TimerEventTriggerDefinitionImpl() {
        this.timerType = TimerType.CYCLE;
        timerValue = new ExpressionImpl();
    }

    @Override
    public Expression getTimerExpression() {
        return timerValue;
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }

}
