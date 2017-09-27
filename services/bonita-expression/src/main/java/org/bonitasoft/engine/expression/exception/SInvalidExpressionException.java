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
package org.bonitasoft.engine.expression.exception;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class SInvalidExpressionException extends SExpressionException {

    private static final long serialVersionUID = 5287292772348995383L;

    private final String expressionName;

    public SInvalidExpressionException(final String message, final Throwable cause, final String expressionName) {
        super(message, cause);
        this.expressionName = expressionName;
    }

    public SInvalidExpressionException(final String message, final String expressionName) {
        super(message);
        this.expressionName = expressionName;
    }

    public SInvalidExpressionException(final Throwable cause, final String expressionName) {
        super(cause);
        this.expressionName = expressionName;
    }

    /**
     * Return null or empty string, if the context of evaluation is wrong.
     * 
     * @return The expression's name that failed on the evaluation.
     */
    public String getExpressionName() {
        return expressionName;
    }
}
