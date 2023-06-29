/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.Arrays;

import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.InputContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;

/**
 * @author Baptiste Mesta
 */
public abstract class InputContainerDefinitionBuilder extends FlowElementContainerBuilder {

    public InputContainerDefinitionBuilder(FlowElementContainerDefinitionImpl container,
            ProcessDefinitionBuilder processDefinitionBuilder) {
        super(container, processDefinitionBuilder);
    }

    public InputContainerDefinitionBuilder addInput(final String name, final Type type, final String description) {
        return addInput(name, type, description, false);
    }

    public InputContainerDefinitionBuilder addInput(final String name, final Type type, final String description,
            final boolean multiple) {
        final InputDefinitionImpl input = new InputDefinitionImpl(name, type, description, multiple);
        getInputContainerDefinition().addInput(input);
        return this;
    }

    public ContractInputDefinitionBuilder addComplexInput(final String name, final String description) {
        return addComplexInput(name, description, false);
    }

    public ContractInputDefinitionBuilder addComplexInput(final String name, final String description,
            final boolean multiple) {
        final InputDefinitionImpl input = new InputDefinitionImpl(name, description, multiple);
        getInputContainerDefinition().addInput(input);
        return new ContractInputDefinitionBuilder(getProcessBuilder(), getContainer(), input);
    }

    public InputContainerDefinitionBuilder addFileInput(final String name, final String description) {
        return addFileInput(name, description, false);
    }

    public InputContainerDefinitionBuilder addFileInput(final String name, final String description, boolean multiple) {
        final InputDefinitionImpl nameInput = new InputDefinitionImpl(InputDefinition.FILE_INPUT_FILENAME, Type.TEXT,
                "Name of the file");
        final InputDefinitionImpl contentInput = new InputDefinitionImpl(InputDefinition.FILE_INPUT_CONTENT,
                Type.BYTE_ARRAY, "Content of the file");
        final InputDefinitionImpl fileInput = new InputDefinitionImpl(name, description, multiple, Type.FILE,
                Arrays.<InputDefinition> asList(nameInput, contentInput));
        getInputContainerDefinition().addInput(fileInput);
        return this;
    }

    protected abstract InputContainerDefinitionImpl getInputContainerDefinition();
}
