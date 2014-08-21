/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.contract;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Matthieu Chaffotte
 */
public class ContractViolationException extends BonitaException {

    private static final long serialVersionUID = -5733414795158022044L;

    private List<String> explanations;

    public ContractViolationException(final String message, final List<String> explanations) {
        super(message);
        if (explanations == null) {
            this.explanations = new ArrayList<String>();
        }
        this.explanations = new ArrayList<String>(explanations);
    }

    public List<String> getExplanations() {
        return explanations;
    }

}
