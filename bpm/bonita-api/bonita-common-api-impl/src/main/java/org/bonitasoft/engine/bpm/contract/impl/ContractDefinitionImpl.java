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
package org.bonitasoft.engine.bpm.contract.impl;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.InputDefinition;

/**
 * @author Matthieu Chaffotte
 */
public class ContractDefinitionImpl implements ContractDefinition {

    private static final long serialVersionUID = 786706819903231008L;

    private final List<InputDefinition> inputs;

    public ContractDefinitionImpl() {
        inputs = new ArrayList<InputDefinition>();
    }

    @Override
    public List<InputDefinition> getInputs() {
        return inputs;
    }

    public void addInput(final InputDefinition input) {
        inputs.add(input);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (inputs == null ? 0 : inputs.hashCode());
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
        final ContractDefinitionImpl other = (ContractDefinitionImpl) obj;
        if (inputs == null) {
            if (other.inputs != null) {
                return false;
            }
        } else if (!inputs.equals(other.inputs)) {
            return false;
        }
        return true;
    }

}
