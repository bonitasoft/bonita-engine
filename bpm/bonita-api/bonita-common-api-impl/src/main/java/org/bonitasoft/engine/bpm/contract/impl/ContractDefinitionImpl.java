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
package org.bonitasoft.engine.bpm.contract.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.InputDefinition;

/**
 * @author Matthieu Chaffotte
 * @author Laurent Leseigneur
 */
public class ContractDefinitionImpl implements ContractDefinition {

    private static final long serialVersionUID = 786706819903231008L;

    private final List<ConstraintDefinition> constraints;

    private final List<InputDefinition> inputs;

    public ContractDefinitionImpl() {
        inputs = new ArrayList<>();
        constraints = new ArrayList<>();
    }

    public void addInput(final InputDefinition input) {
        inputs.add(input);
    }

    public void addConstraint(final ConstraintDefinition constraint) {
        constraints.add(constraint);
    }

    @Override
    public List<ConstraintDefinition> getConstraints() {
        return constraints;
    }

    @Override
    public List<InputDefinition> getInputs() {
        return inputs;
    }

    @Override
    public String toString() {
        return "ContractDefinitionImpl{" +
                "constraints=" + constraints +
                ", inputs=" + inputs +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContractDefinitionImpl that = (ContractDefinitionImpl) o;
        return Objects.equals(constraints, that.constraints) &&
                Objects.equals(inputs, that.inputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constraints, inputs);
    }
}
