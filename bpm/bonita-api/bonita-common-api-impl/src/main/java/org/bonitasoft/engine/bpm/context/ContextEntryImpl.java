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
 */

package org.bonitasoft.engine.bpm.context;

import java.util.Objects;

import org.bonitasoft.engine.expression.Expression;

/**
 * @author Baptiste Mesta
 */
public class ContextEntryImpl implements ContextEntry {


    private String key;
    private Expression expression;

    public ContextEntryImpl() {
    }

    public ContextEntryImpl(String key, Expression expression) {
        this.key = key;
        this.expression = expression;
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContextEntryImpl)) return false;
        ContextEntryImpl that = (ContextEntryImpl) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, expression);
    }

    @Override
    public String toString() {
        return "ContextEntryImpl{" +
                "key='" + key + '\'' +
                ", expression='" + expression + '\'' +
                '}';
    }
}
