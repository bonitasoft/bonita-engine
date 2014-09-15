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

import java.util.List;

import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;

/**
 * @author Laurent Leseigneur
 */
public class ComplexInputDefinitionImpl extends InputDefinitionImpl implements ComplexInputDefinition {

    private static final long serialVersionUID = 2836592506382887928L;
    private final List<InputDefinition> inputDefinitions;

    public ComplexInputDefinitionImpl(final String name, final String description, final List<InputDefinition> inputDefinitions) {
        super(name, Type.COMPLEX, description);
        this.inputDefinitions = inputDefinitions;
    }

    public List<InputDefinition> getInputDefinitions() {
        return inputDefinitions;
    }

    @Override
    public String toString() {
        return "ComplexInputDefinitionImpl [inputDefinitions=" + inputDefinitions + ", getType()=" + getType() + ", getDescription()=" + getDescription()
                + ", getName()=" + getName() + ", toString()=" + super.toString() + ", getClass()=" + getClass() + "]";
    }

}
