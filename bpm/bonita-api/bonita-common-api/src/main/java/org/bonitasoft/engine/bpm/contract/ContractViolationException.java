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
package org.bonitasoft.engine.bpm.contract;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * Thrown when the {@link ContractDefinition} is not fulfilled.
 *
 * @author Matthieu Chaffotte
 * @since 7.0
 */
public class ContractViolationException extends BonitaException {

    private static final long serialVersionUID = -5733414795158022044L;

    private final List<String> explanations;
    private final String simpleMessage;

    /**
     * Constructs an <code>ContractViolationException</code> with the specified detail message and the explanations.
     *
     * @param message      the specified detail message
     * @param explanations the explanations
     */
    public ContractViolationException(final String message, final List<String> explanations) {
        super(message + ": " + explanations);
        this.simpleMessage = message;
        if (explanations == null) {
            this.explanations = new ArrayList<>();
        } else {
            this.explanations = new ArrayList<>(explanations);
        }
    }

    /**
     * Returns the explanations of why the contract is not fulfilled.
     *
     * @return the explanations
     */
    public List<String> getExplanations() {
        return explanations;
    }

    public String getSimpleMessage() {
        return simpleMessage;
    }
}
