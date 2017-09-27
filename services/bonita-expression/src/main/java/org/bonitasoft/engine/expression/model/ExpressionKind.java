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
package org.bonitasoft.engine.expression.model;

import java.io.Serializable;

/**
 * Used to identify a kind of expression
 * e.g.
 * for constant kind is : [ type = TYPE_CONSTANT, interpreter = null ]
 * for groovy kind is : [ type = TYPE_READ_SCRIPT, interpreter = GROOVY ]
 * 
 * @author Baptiste Mesta
 */
public class ExpressionKind implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String NONE = "NONE";

    private String interpreter = NONE;

    private String type;

    public ExpressionKind() {
    }

    public ExpressionKind(final String type) {
        this.type = type;
        interpreter = NONE;
    }

    public ExpressionKind(final String type, final String interpreter) {
        this.type = type;
        this.interpreter = interpreter == null || interpreter.isEmpty() ? NONE : interpreter;
    }

    public String getExpressionType() {
        return type;
    }

    public String getInterpreter() {
        return interpreter;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setInterpreter(final String interpreter) {
        this.interpreter = interpreter == null || interpreter.isEmpty() ? NONE : interpreter;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (interpreter == null ? 0 : interpreter.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
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
        final ExpressionKind other = (ExpressionKind) obj;
        if (interpreter == null) {
            if (other.interpreter != null) {
                return false;
            }
        } else if (!interpreter.equals(other.interpreter)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ExpressionKind [interpreter=");
        builder.append(interpreter);
        builder.append(", type=");
        builder.append(type);
        builder.append("]");
        return builder.toString();
    }
}
