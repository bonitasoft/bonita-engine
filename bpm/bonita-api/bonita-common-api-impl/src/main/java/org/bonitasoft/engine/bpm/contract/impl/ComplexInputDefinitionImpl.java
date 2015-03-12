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
 **/
package org.bonitasoft.engine.bpm.contract.impl;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;

/**
 * @author Laurent Leseigneur
 */
public class ComplexInputDefinitionImpl extends InputDefinitionImpl implements ComplexInputDefinition {

    @Override
    public String toString() {
        return "ComplexInputDefinitionImpl [simpleInputDefinitions=" + simpleInputDefinitions + ", complexInputDefinitions=" + complexInputDefinitions
                + ", getSimpleInputs()=" + getSimpleInputs() + ", getComplexInputs()=" + getComplexInputs() + ", getDescription()=" + getDescription()
                + ", getName()=" + getName() + ", isMultiple()=" + isMultiple() + "]";
    }

    private static final long serialVersionUID = 2836592506382887928L;
    private final List<SimpleInputDefinition> simpleInputDefinitions;
    private final List<ComplexInputDefinition> complexInputDefinitions;

    public ComplexInputDefinitionImpl(final String name, final String description) {
        this(name, description, false, null, null);
    }

    public ComplexInputDefinitionImpl(final String name, final String description, final boolean multiple) {
        this(name, description, multiple, null, null);
    }

    public ComplexInputDefinitionImpl(final String name, final String description, final List<SimpleInputDefinition> simpleInputDefinitions,
            final List<ComplexInputDefinition> complexInputDefinitions) {
        this(name, description, false, simpleInputDefinitions, complexInputDefinitions);
    }

    public ComplexInputDefinitionImpl(final String name, final String description, final boolean multiple,
            final List<SimpleInputDefinition> simpleInputDefinitions,
            final List<ComplexInputDefinition> complexInputDefinitions) {
        super(name, description, multiple);
        this.simpleInputDefinitions = new ArrayList<SimpleInputDefinition>();
        this.complexInputDefinitions = new ArrayList<ComplexInputDefinition>();
        if (simpleInputDefinitions != null) {
            for (final SimpleInputDefinition simpleInputDefinition : simpleInputDefinitions) {
                this.simpleInputDefinitions.add(simpleInputDefinition);
            }
        }
        if (complexInputDefinitions != null) {
            for (final ComplexInputDefinition complexInputDefinition : complexInputDefinitions) {
                this.complexInputDefinitions.add(complexInputDefinition);
            }
        }
    }

    @Override
    public List<SimpleInputDefinition> getSimpleInputs() {
        return simpleInputDefinitions;
    }

    @Override
    public List<ComplexInputDefinition> getComplexInputs() {
        return complexInputDefinitions;
    }

}
