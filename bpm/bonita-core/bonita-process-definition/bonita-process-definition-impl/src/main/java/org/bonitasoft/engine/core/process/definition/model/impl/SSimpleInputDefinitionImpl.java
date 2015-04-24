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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.Objects;

import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;

/**
 * @author Matthieu Chaffotte
 */
public class SSimpleInputDefinitionImpl extends SNamedElementImpl implements SSimpleInputDefinition {

    private static final long serialVersionUID = -4947430801791009535L;

    private final String description;

    private final SType type;

    private final boolean multiple;

    public SSimpleInputDefinitionImpl(final String name, final SType type, final String description) {
        this(name, type, description, false);
    }

    public SSimpleInputDefinitionImpl(final String name, final SType type, final String description, final boolean multiple) {
        super(name);
        this.type = type;
        this.description = description;
        this.multiple = multiple;
    }

    public SSimpleInputDefinitionImpl(final InputDefinition input) {
        this(input.getName(), convertTypeToSType(input.getType()), input.getDescription(), input.isMultiple());
    }

    private static SType convertTypeToSType(final Type type2) {
        return SType.valueOf(type2.toString());
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public SType getType() {
        return type;
    }

    @Override
    public boolean isMultiple() {
        return multiple;
    }

    @Override
    public String toString() {
        return "SSimplInputDefinitionImpl{" +
                "description='" + description + '\'' +
                ", type=" + type +
                ", multiple=" + multiple +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SSimpleInputDefinitionImpl that = (SSimpleInputDefinitionImpl) o;
        return Objects.equals(multiple, that.multiple) &&
                Objects.equals(description, that.description) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), description, type, multiple);
    }
}
