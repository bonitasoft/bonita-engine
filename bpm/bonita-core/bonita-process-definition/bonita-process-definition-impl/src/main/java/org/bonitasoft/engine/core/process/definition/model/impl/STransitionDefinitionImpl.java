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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Matthieu Chaffotte
 * @author Zhao Na
 * @author Celine Souchet
 */
public class STransitionDefinitionImpl extends SNamedElementImpl implements STransitionDefinition {

    private static final long serialVersionUID = -5150684543828946974L;

    private final long source;

    private final long target;

    private SExpression condition;

    public STransitionDefinitionImpl(final TransitionDefinition transition) {
        this(transition.getName(), transition.getSource(), transition.getTarget());
        final Expression exp = transition.getCondition();
        if (transition.getCondition() != null) {
            final SExpression sExpression = ServerModelConvertor.convertExpression(exp);
            condition = sExpression;
        }
    }

    public STransitionDefinitionImpl(final String name) {
        this(name, -1, -1);
    }

    public STransitionDefinitionImpl(final String name, final long source, final long target) {
        super(name);
        this.source = source;
        this.target = target;
    }

    @Override
    public long getSource() {
        return source;
    }

    @Override
    public long getTarget() {
        return target;
    }

    public void setCondition(final SExpression condition) {
        this.condition = condition;
    }

    @Override
    public SExpression getCondition() {
        return condition;
    }

    @Override
    public boolean hasCondition() {
        return condition != null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (condition == null ? 0 : condition.hashCode());
        result = prime * result + (int) (source ^ source >>> 32);
        result = prime * result + (int) (target ^ target >>> 32);
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
        final STransitionDefinitionImpl other = (STransitionDefinitionImpl) obj;
        if (condition == null) {
            if (other.condition != null) {
                return false;
            }
        } else if (!condition.equals(other.condition)) {
            return false;
        }
        if (source != other.source) {
            return false;
        }
        if (target != other.target) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "["+getName()+"]";
    }
}
