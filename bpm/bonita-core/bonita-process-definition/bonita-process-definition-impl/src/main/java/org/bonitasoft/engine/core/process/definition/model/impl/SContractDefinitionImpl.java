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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;

/**
 * @author Matthieu Chaffotte
 */
public class SContractDefinitionImpl extends SBaseElementImpl implements SContractDefinition {

    private static final long serialVersionUID = -5281686322739618159L;

    private final List<SInputDefinition> inputs;

    public SContractDefinitionImpl() {
        super();
        inputs = new ArrayList<SInputDefinition>();
    }

    public SContractDefinitionImpl(final ContractDefinition contract) {
        super();
        inputs = new ArrayList<SInputDefinition>();
        for (final InputDefinition input : contract.getInputs()) {
            inputs.add(new SInputDefinitionImpl(input));
        }
    }

    @Override
    public List<SInputDefinition> getInputs() {
        return inputs;
    }

    public void addInput(final SInputDefinition input) {
        inputs.add(input);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (inputs == null ? 0 : inputs.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SContractDefinitionImpl other = (SContractDefinitionImpl) obj;
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
