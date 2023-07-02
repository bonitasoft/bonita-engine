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
package org.bonitasoft.engine.expression.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bonitasoft.engine.bpm.internal.LongToStringAdapter;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
public class ExpressionImpl implements Expression {

    private static final long serialVersionUID = 1663953453575781859L;

    @XmlID
    @XmlJavaTypeAdapter(type = long.class, value = LongToStringAdapter.class)
    @XmlAttribute
    private long id;

    @XmlAttribute
    private String name;
    @XmlElement
    private String content;
    @XmlAttribute
    private String expressionType;
    @XmlAttribute
    private String returnType;
    @XmlAttribute
    private String interpreter;

    @XmlElement(type = ExpressionImpl.class, name = "expression")
    private List<Expression> dependencies = new ArrayList<>();

    private long generateId() {
        return Math.abs(UUID.randomUUID().getLeastSignificantBits());
    }

    @Override
    public Expression copy() {
        try {
            final ExpressionImpl clone = (ExpressionImpl) clone();
            clone.setId(generateId());
            final List<Expression> depList = new ArrayList<>(getDependencies().size());
            for (Expression expression : getDependencies()) {
                depList.add(expression.copy());
            }
            clone.setDependencies(depList);
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }

    @Override
    public boolean isEquivalent(Expression e) {
        // Ignore the ID field to determine equivalence:
        if (this == e)
            return true;
        if (e == null || getClass() != e.getClass())
            return false;
        return Objects.equals(name, e.getName()) &&
                Objects.equals(content, e.getContent()) &&
                Objects.equals(expressionType, e.getExpressionType()) &&
                Objects.equals(returnType, e.getReturnType()) &&
                Objects.equals(interpreter, e.getInterpreter()) &&
                Objects.equals(dependencies, e.getDependencies());
    }

}
