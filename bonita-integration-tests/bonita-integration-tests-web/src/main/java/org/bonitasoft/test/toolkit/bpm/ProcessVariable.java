/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.test.toolkit.bpm;

import java.util.Date;

import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;

/**
 * @author Colin PUY
 */
public class ProcessVariable {

    private final String name;

    private final Class<?> clazz;

    private final Expression defaultValue;

    public static ProcessVariable aStringVariable(String name, String defaultValue) throws InvalidExpressionException {
        return new ProcessVariable(name, String.class,
                new ExpressionBuilder().createConstantStringExpression(defaultValue));
    }

    public static ProcessVariable aLongVariable(String name, long defaultValue) throws InvalidExpressionException {
        return new ProcessVariable(name, Long.class,
                new ExpressionBuilder().createConstantLongExpression(defaultValue));
    }

    public static ProcessVariable aDateVariable(String name, String defaultValue) throws InvalidExpressionException {
        return new ProcessVariable(name, Date.class,
                new ExpressionBuilder().createConstantDateExpression(defaultValue));
    }

    public ProcessVariable(String name, Class<?> clazz, Expression defaultValue) {
        this.name = name;
        this.clazz = clazz;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return clazz.getName();
    }

    public Expression getDefaultValue() {
        return defaultValue;
    }

    public static ProcessVariable createLongVariable(long value) throws InvalidExpressionException {
        return new ProcessVariable("aLongVariable", Long.class,
                new ExpressionBuilder().createConstantLongExpression(value));
    }

    public static ProcessVariable createIntVariable(int value) throws InvalidExpressionException {
        return new ProcessVariable("aIntVariable", Integer.class,
                new ExpressionBuilder().createConstantIntegerExpression(value));
    }

    public static ProcessVariable createStringVariable(String value) throws InvalidExpressionException {
        return new ProcessVariable("aStringVariable", String.class,
                new ExpressionBuilder().createConstantStringExpression(value));
    }

    public static ProcessVariable createBooleanVariable(Boolean value) throws InvalidExpressionException {
        return new ProcessVariable("aBooleanVariable", Boolean.class,
                new ExpressionBuilder().createConstantBooleanExpression(value));
    }

    public static ProcessVariable createDoubleVariable(Double value) throws InvalidExpressionException {
        return new ProcessVariable("aDoubleVariable", Double.class,
                new ExpressionBuilder().createConstantDoubleExpression(value));
    }

}
