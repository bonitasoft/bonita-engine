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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;

/**
 * @author Matthieu Chaffotte
 */
public class SContractDefinitionImpl extends SBaseElementImpl implements SContractDefinition {

    private static final long serialVersionUID = -5281686322739618159L;

    private final List<SInputDefinition> inputDefinitions;

    private final List<SConstraintDefinition> constraints;

    public SContractDefinitionImpl() {
        super();
        inputDefinitions = new ArrayList<>();
        constraints = new ArrayList<>();
    }

    public SContractDefinitionImpl(final ContractDefinition contract) {
        this();
        for (final InputDefinition input : contract.getInputs()) {
            inputDefinitions.add(new SInputDefinitionImpl(input));
        }
        for (final ConstraintDefinition rule : contract.getConstraints()) {
            constraints.add(new SConstraintDefinitionImpl(rule));
        }
    }

    @Override
    public List<SInputDefinition> getInputDefinitions() {
        return inputDefinitions;
    }

    public void addInput(final SInputDefinition input) {
        inputDefinitions.add(input);
    }

    @Override
    public List<SConstraintDefinition> getConstraints() {
        return constraints;
    }

    public void addConstraint(final SConstraintDefinition rule) {
        constraints.add(rule);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SContractDefinitionImpl that = (SContractDefinitionImpl) o;
        return Objects.equals(inputDefinitions, that.inputDefinitions) &&
                Objects.equals(constraints, that.constraints);
    }

    @Override
    public String toString() {
        return "SContractDefinitionImpl{" +
                "inputDefinitions=" + inputDefinitions +
                ", constraints=" + constraints +
                "} " + super.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), inputDefinitions, constraints);
    }
}
