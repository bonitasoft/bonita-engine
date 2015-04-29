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
package org.bonitasoft.engine.bpm.contract.validation.builder;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SInputDefinitionImpl;

public class SComplexInputDefinitionBuilder {

    private final List<SInputDefinition> inputDefinitions = new ArrayList<>();
    private String name = "aName";
    private String description = "a description";
    private boolean multiple = false;

    public static SComplexInputDefinitionBuilder aComplexInput() {
        return new SComplexInputDefinitionBuilder();
    }

    public SInputDefinition build() {
        return new SInputDefinitionImpl(name, description, multiple, inputDefinitions);
    }

    public SComplexInputDefinitionBuilder withInput(final SSimpleInputDefinitionBuilder... definitions) {
        for (final SSimpleInputDefinitionBuilder definition : definitions) {
            inputDefinitions.add(definition.build());
        }
        return this;
    }

    public SComplexInputDefinitionBuilder withInput(final SInputDefinition... definitions) {
        for (final SInputDefinition definition : definitions) {
            inputDefinitions.add(definition);
        }
        return this;
    }

    public SComplexInputDefinitionBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public SComplexInputDefinitionBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    public SComplexInputDefinitionBuilder withMultiple(final boolean multiple) {
        this.multiple = multiple;
        return this;
    }
}
