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
package org.bonitasoft.engine.expression.impl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bonitasoft.engine.bpm.internal.BaseElementImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public class ExpressionImpl extends BaseElementImpl implements Expression {

    private static final long serialVersionUID = 1663953453575781859L;

    private String name;

    private String content;

    private String expressionType;

    private String returnType;

    private String interpreter;

    private List<Expression> dependencies = Collections.emptyList();

    public ExpressionImpl() {
        this(Math.abs(UUID.randomUUID().getMostSignificantBits()));
    }

    public ExpressionImpl(long id) {
        setId(id);
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
        if (dependencies == null) {
            return Collections.emptyList();
        }
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
        final int maxLen = 5;
        final StringBuilder builder = new StringBuilder();
        builder.append("ExpressionImpl [name=");
        builder.append(name);
        builder.append(", content=");
        builder.append(content);
        builder.append(", expressionType=");
        builder.append(expressionType);
        builder.append(", returnType=");
        builder.append(returnType);
        builder.append(", interpreter=");
        builder.append(interpreter);
        builder.append(", dependencies=");
        builder.append(dependencies != null ? dependencies.subList(0, Math.min(dependencies.size(), maxLen)) : null);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ExpressionImpl))
            return false;
        if (!super.equals(o))
            return false;

        ExpressionImpl that = (ExpressionImpl) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        if (content != null ? !content.equals(that.content) : that.content != null)
            return false;
        if (expressionType != null ? !expressionType.equals(that.expressionType) : that.expressionType != null)
            return false;
        if (returnType != null ? !returnType.equals(that.returnType) : that.returnType != null)
            return false;
        if (interpreter != null ? !interpreter.equals(that.interpreter) : that.interpreter != null)
            return false;
        return !(dependencies != null ? !dependencies.equals(that.dependencies) : that.dependencies != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (expressionType != null ? expressionType.hashCode() : 0);
        result = 31 * result + (returnType != null ? returnType.hashCode() : 0);
        result = 31 * result + (interpreter != null ? interpreter.hashCode() : 0);
        result = 31 * result + (dependencies != null ? dependencies.hashCode() : 0);
        return result;
    }
}
