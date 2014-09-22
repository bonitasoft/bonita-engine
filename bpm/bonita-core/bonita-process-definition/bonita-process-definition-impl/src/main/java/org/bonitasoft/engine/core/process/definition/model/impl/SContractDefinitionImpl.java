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

import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;

/**
 * @author Matthieu Chaffotte
 */
public class SContractDefinitionImpl extends SBaseElementImpl implements SContractDefinition {

    private static final long serialVersionUID = -5281686322739618159L;

    private final List<SSimpleInputDefinition> simpleInputs;

    private final List<SComplexInputDefinition> complexInputs;

    private final List<SConstraintDefinition> constraints;

    public SContractDefinitionImpl() {
        super();
        simpleInputs = new ArrayList<SSimpleInputDefinition>();
        complexInputs = new ArrayList<SComplexInputDefinition>();
        constraints = new ArrayList<SConstraintDefinition>();
    }

    public SContractDefinitionImpl(final ContractDefinition contract) {
        this();
        for (final SimpleInputDefinition input : contract.getSimpleInputs()) {
            simpleInputs.add(new SSimpleInputDefinitionImpl(input));
        }
        for (final ComplexInputDefinition input : contract.getComplexInputs()) {
            complexInputs.add(new SComplexInputDefinitionImpl(input));
        }
        for (final ConstraintDefinition rule : contract.getConstraints()) {
            constraints.add(new SConstraintDefinitionImpl(rule));
        }
    }

    @Override
    public List<SSimpleInputDefinition> getSimpleInputs() {
        return simpleInputs;
    }

    public void addSimpleInput(final SSimpleInputDefinition input) {
        simpleInputs.add(input);
    }

    public void addComplexInput(final SComplexInputDefinition input) {
        complexInputs.add(input);
    }

    @Override
    public List<SConstraintDefinition> getConstraints() {
        return constraints;
    }

    public void addRule(final SConstraintDefinition rule) {
        constraints.add(rule);
    }



    @Override
    public List<SComplexInputDefinition> getComplexInputs() {
        return complexInputs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (complexInputs == null ? 0 : complexInputs.hashCode());
        result = prime * result + (constraints == null ? 0 : constraints.hashCode());
        result = prime * result + (simpleInputs == null ? 0 : simpleInputs.hashCode());
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
        if (complexInputs == null) {
            if (other.complexInputs != null) {
                return false;
            }
        } else if (!complexInputs.equals(other.complexInputs)) {
            return false;
        }
        if (constraints == null) {
            if (other.constraints != null) {
                return false;
            }
        } else if (!constraints.equals(other.constraints)) {
            return false;
        }
        if (simpleInputs == null) {
            if (other.simpleInputs != null) {
                return false;
            }
        } else if (!simpleInputs.equals(other.simpleInputs)) {
            return false;
        }
        return true;
    }

}
