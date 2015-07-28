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

import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TransitionDefinitionImpl extends NamedElementImpl implements TransitionDefinition {

    private static final long serialVersionUID = -5629473055955264480L;
    @XmlAttribute
    private long source;
    @XmlAttribute
    private long target;
    @XmlElement(type = ExpressionImpl.class)
    private Expression expression;

    public TransitionDefinitionImpl(final String name) {
        this(name, -1, -1);
    }

    public TransitionDefinitionImpl(final String name, final long source, final long target) {
        super(name);
        this.source = source;
        this.target = target;
    }

    public TransitionDefinitionImpl(){}
    @Override
    public long getSource() {
        return source;
    }

    @Override
    public long getTarget() {
        return target;
    }

    @Override
    public Expression getCondition() {
        return expression;
    }

    public void setTarget(final long target) {
        this.target = target;
    }

    public void setSource(final long source) {
        this.source = source;
    }

    public void setCondition(final Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("TransitionDefinitionImpl [name=");
        builder.append(getName());
        builder.append(", source=");
        builder.append(source);
        builder.append(", target=");
        builder.append(target);
        builder.append(", expression=");
        builder.append(expression);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (expression == null ? 0 : expression.hashCode());
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
        final TransitionDefinitionImpl other = (TransitionDefinitionImpl) obj;
        if (expression == null) {
            if (other.expression != null) {
                return false;
            }
        } else if (!expression.equals(other.expression)) {
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
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }

}
