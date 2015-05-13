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
package org.bonitasoft.engine.expression.model.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Baptiste Mesta
 */
public class SExpressionImpl implements SExpression {

    private static final long serialVersionUID = 1L;

    private String name;

    private String content;

    private String returnType;

    private List<SExpression> dependencies;

    private final ExpressionKind expressionKind = new ExpressionKind();

    public SExpressionImpl() {
        super();
    }

    public SExpressionImpl(final String name, final String content, final String expressionType, final String returnType, final String interpreter,
            final List<SExpression> dependencies) {
        this.name = name;
        this.content = content;
        expressionKind.setType(expressionType);
        this.returnType = returnType;
        expressionKind.setInterpreter(interpreter);
        this.dependencies = dependencies;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public void setExpressionType(final String expressionType) {
        expressionKind.setType(expressionType);
    }

    public void setReturnType(final String returnType) {
        this.returnType = returnType;
    }

    public void setInterpreter(final String interpreter) {
        expressionKind.setInterpreter(interpreter);
    }

    public void setDependencies(final List<SExpression> dependencies) {
        this.dependencies = dependencies;
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
        return expressionKind.getExpressionType();
    }

    @Override
    public String getReturnType() {
        return returnType;
    }

    @Override
    public String getInterpreter() {
        return expressionKind.getInterpreter();
    }

    @Override
    public List<SExpression> getDependencies() {
        if (dependencies == null) {
            return Collections.emptyList();
        }
        return dependencies;
    }

    @Override
    public boolean hasDependencies() {
        return !getDependencies().isEmpty();
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return expressionKind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, content, returnType, dependencies, expressionKind);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SExpressionImpl))
            return false;
        SExpressionImpl that = (SExpressionImpl) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(content, that.content) &&
                Objects.equals(returnType, that.returnType) &&
                Objects.equals(dependencies, that.dependencies) &&
                Objects.equals(expressionKind, that.expressionKind);
    }

    @Override
    public String toString() {
        final int maxLen = 5;
        final StringBuilder builder = new StringBuilder();
        builder.append("SExpressionImpl [name=");
        builder.append(name);
        builder.append(", content=");
        builder.append(content);
        builder.append(", returnType=");
        builder.append(returnType);
        builder.append(", dependencies=");
        builder.append(dependencies != null ? dependencies.subList(0, Math.min(dependencies.size(), maxLen)) : null);
        builder.append(", expressionKind=");
        builder.append(expressionKind);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int getDiscriminant() {
        return hashCode();
    }

}
