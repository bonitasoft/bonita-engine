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
package org.bonitasoft.engine.bpm.process.impl;

import java.util.List;

import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ComplexInputDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.SimpleInputDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskDefinitionImpl;

/**
 * @author Matthieu Chaffotte
 * @author Laurent Leseigneur
 */
public class ContractDefinitionBuilder extends FlowElementContainerBuilder {

    private final ContractDefinitionImpl contract;

    public ContractDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final UserTaskDefinitionImpl activity) {
        super(container, processDefinitionBuilder);
        contract = new ContractDefinitionImpl();
        activity.setContract(contract);
    }

    public ContractDefinitionBuilder addSimpleInput(final String name, final Type type, final String description) {
        final SimpleInputDefinition input = new SimpleInputDefinitionImpl(name, type, description);
        contract.addSimpleInput(input);
        return this;
    }

    public ContractDefinitionBuilder addComplexInput(final String name, final String description, final List<SimpleInputDefinition> simpleInputs,
            final List<ComplexInputDefinition> complexInputs) {
        final ComplexInputDefinitionImpl input = new ComplexInputDefinitionImpl(name, description, simpleInputs, complexInputs);
        contract.addComplexInput(input);
        return this;
    }

    public ContractDefinitionBuilder addConstraint(final String name, final String expression, final String explanation, final String... inputNames) {
        final ConstraintDefinitionImpl constraintDefinition = new ConstraintDefinitionImpl(name, expression, explanation);
        for (final String inputName : inputNames) {
            constraintDefinition.addInputName(inputName);
        }
        contract.addConstraint(constraintDefinition);
        return this;
    }

    public ContractDefinitionBuilder addMandatoryConstraint(final String inputName) {
        final StringBuilder expression = new StringBuilder().append(inputName).append("!=null");
        expression.append(" && !");
        expression.append(inputName);
        expression.append(".toString().isEmpty()");

        final ConstraintDefinitionImpl constraint = new ConstraintDefinitionImpl(inputName, expression.toString(), new StringBuilder().append("input ")
                .append(inputName).append(" is mandatory").toString());
        constraint.addInputName(inputName);
        contract.addConstraint(constraint);
        return this;
    }
}
