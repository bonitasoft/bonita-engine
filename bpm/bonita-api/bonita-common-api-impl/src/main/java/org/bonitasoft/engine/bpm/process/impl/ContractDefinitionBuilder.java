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
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.InputContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.DesignProcessDefinitionImpl;

/**
 * @author Matthieu Chaffotte
 * @author Laurent Leseigneur
 */
public class ContractDefinitionBuilder extends InputContainerDefinitionBuilder {

    private final ContractDefinitionImpl contract;

    public ContractDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final UserTaskDefinitionImpl activity) {
        super(container, processDefinitionBuilder);
        contract = new ContractDefinitionImpl();
        activity.setContract(contract);
    }

    public ContractDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final DesignProcessDefinitionImpl container) {
        super((FlowElementContainerDefinitionImpl) container.getProcessContainer(), processDefinitionBuilder);
        contract = new ContractDefinitionImpl();
        container.setContract(contract);
    }

    public ContractDefinitionBuilder addConstraint(final String name, final String expression, final String explanation, final String... inputNames) {
        final ConstraintDefinitionImpl constraintDefinition = new ConstraintDefinitionImpl(name, expression, explanation);
        for (final String inputName : inputNames) {
            constraintDefinition.addInputName(inputName);
        }
        contract.addConstraint(constraintDefinition);
        return this;
    }

    @Override
    protected InputContainerDefinitionImpl getInputContainerDefinition() {
        return contract;
    }

    @Override
    public ContractDefinitionBuilder addFileInput(String name, String description) {
        return (ContractDefinitionBuilder) super.addFileInput(name, description);
    }

    @Override
    public ContractDefinitionBuilder addFileInput(String name, String description, boolean multiple) {
        return (ContractDefinitionBuilder) super.addFileInput(name, description, multiple);
    }

    @Override
    public ContractDefinitionBuilder addInput(String name, Type type, String description) {
        return (ContractDefinitionBuilder) super.addInput(name, type, description);
    }

    @Override
    public ContractDefinitionBuilder addInput(String name, Type type, String description, boolean multiple) {
        return (ContractDefinitionBuilder) super.addInput(name, type, description, multiple);
    }

    @Override
    public ContractInputDefinitionBuilder addComplexInput(String name, String description) {
        return super.addComplexInput(name, description);
    }

    @Override
    public ContractInputDefinitionBuilder addComplexInput(String name, String description, boolean multiple) {
        return super.addComplexInput(name, description, multiple);
    }
}
