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

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.DesignProcessDefinitionImpl;

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

    public ContractDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final DesignProcessDefinitionImpl container) {
        super((FlowElementContainerDefinitionImpl) container.getProcessContainer(), processDefinitionBuilder);
        contract = new ContractDefinitionImpl();
        container.setContract(contract);
    }

    public ContractDefinitionBuilder addInput(final String name, final Type type, final String description) {
        return addInput(name, type, description, false);
    }

    public ContractDefinitionBuilder addInput(final String name, final Type type, final String description, final boolean multiple) {
        final InputDefinition input = new InputDefinitionImpl(name, type, description, multiple);
        contract.addInput(input);
        return this;
    }

    public ContractDefinitionBuilder addInput(final String name, final String description, final List<InputDefinition> inputDefinitions) {
        return addInput(name, description, false, inputDefinitions);
    }

    public ContractDefinitionBuilder addInput(final String name, final String description, final boolean multiple,
            final List<InputDefinition> inputDefinitions) {
        final InputDefinitionImpl input = new InputDefinitionImpl(name, description, multiple, inputDefinitions);
        contract.addInput(input);
        return this;
    }

    public ContractDefinitionBuilder addFileInput(final String name, final String description) {
        return addFileInput(name, description, false);
    }

    public ContractDefinitionBuilder addFileInput(final String name, final String description, boolean multiple) {
        final InputDefinitionImpl nameInput = new InputDefinitionImpl(InputDefinition.FILE_INPUT_FILENAME, Type.TEXT, "Name of the file");
        final InputDefinitionImpl contentInput = new InputDefinitionImpl(InputDefinition.FILE_INPUT_CONTENT, Type.BYTE_ARRAY, "Content of the file");
        final InputDefinitionImpl fileInput = new InputDefinitionImpl(name, description, multiple, Type.FILE,
                Arrays.<InputDefinition>asList(nameInput, contentInput));
        contract.addInput(fileInput);
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
}
