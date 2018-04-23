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
package org.bonitasoft.engine.bpm.connector;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * Thrown when the evaluation of the condition of the connector instance is invalid.
 * 
 * The class InvalidEvaluationConnectorConditionException is a form of Throwable that indicates conditions that a reasonable application might want to catch.
 * The class InvalidEvaluationConnectorConditionException that is not also subclasses of {@link RuntimeException} are checked exceptions.
 * Checked exceptions need to be declared in a method or constructor's {@literal throws} clause if they can be thrown by the execution of the method or
 * constructor and propagate outside the method or constructor boundary.
 * 
 * @author Zhao Na
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class InvalidEvaluationConnectorConditionException extends BonitaException {

    private static final long serialVersionUID = -7035298849808114112L;

    /**
     * Constructs a new exception with the two conditions to compare
     * 
     * @param condition1
     *            The first condition
     * @param condition2
     *            The second condition
     */
    public InvalidEvaluationConnectorConditionException(final int condition1, final int condition2) {
        super(condition1 + " is not equal to " + condition2 + " .");
    }

}
