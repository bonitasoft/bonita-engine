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
package org.bonitasoft.engine.core.process.instance.api.exceptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * Thrown when the {@link org.bonitasoft.engine.core.process.definition.model.SContractDefinition} is not fulfilled.
 *
 * @author Emmanuel Duchastenier
 * @since 7.0
 */
public class SContractViolationException extends SBonitaException {

    private final List<String> explanations;
    private final String simpleMessage;

    /**
     * Constructs an <code>SContractViolationException</code> with the specified detail message and the explanations.
     *
     * @param message the specified detail message
     * @param explanations the explanations
     */
    public SContractViolationException(final String message, final List<String> explanations) {
        super(message + ": " + explanations);
        this.simpleMessage = message;
        if (explanations == null) {
            this.explanations = Collections.emptyList();
        } else {
            this.explanations = new ArrayList<>(explanations);
        }
    }

    public SContractViolationException(String message, Exception e) {
        super(message, e);
        this.simpleMessage = message;
        this.explanations = Collections.emptyList();
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
