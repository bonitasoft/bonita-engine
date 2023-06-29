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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.bpm.internal.LongToStringAdapter;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
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

    public ExpressionImpl() {
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getExpressionType() {
        return expressionType;
    }

    @Override
    public String getReturnType() {
        return returnType;
    }

    @Override
    public String getInterpreter() {
        return interpreter;
    }

    @Override
    public List<Expression> getDependencies() {
        return dependencies;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public void setExpressionType(final String expressionType) {
        this.expressionType = expressionType;
    }

    public void setReturnType(final String returnType) {
        this.returnType = returnType;
    }

    public void setInterpreter(final String interpreter) {
        this.interpreter = interpreter;
    }

    public void setDependencies(final List<Expression> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .append("name", name)
                .append("content", content)
                .append("expressionType", expressionType)
                .append("returnType", returnType)
                .append("interpreter", interpreter)
                .append("dependencies", dependencies)
                .toString();
    }

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
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ExpressionImpl))
            return false;
        ExpressionImpl that = (ExpressionImpl) o;
        return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(content, that.content) &&
                Objects.equals(expressionType, that.expressionType) &&
                Objects.equals(returnType, that.returnType) &&
                Objects.equals(interpreter, that.interpreter) &&
                Objects.equals(dependencies, that.dependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, content, expressionType, returnType, interpreter, dependencies);
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
