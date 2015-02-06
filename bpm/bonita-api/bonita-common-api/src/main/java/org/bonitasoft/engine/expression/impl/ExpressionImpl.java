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

import org.bonitasoft.engine.expression.Expression;

/**
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public class ExpressionImpl implements Expression {

    private static final long serialVersionUID = 1663953453575781859L;

    private String name;

    private String content;

    private String expressionType;

    private String returnType;

    private String interpreter;

    private List<Expression> dependencies = Collections.emptyList();

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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (content == null ? 0 : content.hashCode());
        result = prime * result + (dependencies == null ? 0 : dependencies.hashCode());
        result = prime * result + (expressionType == null ? 0 : expressionType.hashCode());
        result = prime * result + (interpreter == null ? 0 : interpreter.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (returnType == null ? 0 : returnType.hashCode());
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
        final ExpressionImpl other = (ExpressionImpl) obj;
        if (content == null) {
            if (other.content != null) {
                return false;
            }
        } else if (!content.equals(other.content)) {
            return false;
        }
        if (dependencies == null) {
            if (other.dependencies != null) {
                return false;
            }
        } else if (!dependencies.equals(other.dependencies)) {
            return false;
        }
        if (expressionType == null) {
            if (other.expressionType != null) {
                return false;
            }
        } else if (!expressionType.equals(other.expressionType)) {
            return false;
        }
        if (interpreter == null) {
            if (other.interpreter != null) {
                return false;
            }
        } else if (!interpreter.equals(other.interpreter)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (returnType == null) {
            if (other.returnType != null) {
                return false;
            }
        } else if (!returnType.equals(other.returnType)) {
            return false;
        }
        return true;
    }

}
