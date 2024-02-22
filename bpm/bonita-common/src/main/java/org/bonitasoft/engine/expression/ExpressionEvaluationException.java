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
package org.bonitasoft.engine.expression;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class ExpressionEvaluationException extends BonitaException {

    private static final long serialVersionUID = 7295745453567432910L;

    private String expressionName;

    /**
     * @param cause
     * @param expressionName
     *        The expression's name that failed on the evaluation.
     */
    public ExpressionEvaluationException(final Throwable cause, final String expressionName) {
        super(cause);
        this.expressionName = expressionName;
    }

    public ExpressionEvaluationException(Throwable cause) {
        super(cause);
    }

    /**
     * Return empty or null, when the context of evaluation is wrong.
     *
     * @return The expression's name that failed on the evaluation.
     */
    public String getExpressionName() {
        return expressionName;
    }

}
