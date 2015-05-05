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
package org.bonitasoft.engine.bpm.contract.validation.builder;

import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.bonitasoft.engine.core.process.definition.model.impl.SInputDefinitionImpl;

public class SSimpleInputDefinitionBuilder {

    private String name = "aName";
    private String description = "a description";
    private boolean multiple = false;
    private SType type;

    public SSimpleInputDefinitionBuilder(final SType type) {
        this.type = type;
    }

    public static SSimpleInputDefinitionBuilder aSimpleInput(final SType type) {
        return new SSimpleInputDefinitionBuilder(type);
    }

    public static SSimpleInputDefinitionBuilder aSimpleInput() {
        return new SSimpleInputDefinitionBuilder(SType.TEXT);
    }

    public SInputDefinition build() {
        return new SInputDefinitionImpl(name, type, description, multiple);
    }

    public SSimpleInputDefinitionBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public SSimpleInputDefinitionBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    public SSimpleInputDefinitionBuilder withMultiple(final boolean multiple) {
        this.multiple = multiple;
        return this;
    }

    public SSimpleInputDefinitionBuilder withType(final SType type) {
        this.type = type;
        return this;
    }
}
