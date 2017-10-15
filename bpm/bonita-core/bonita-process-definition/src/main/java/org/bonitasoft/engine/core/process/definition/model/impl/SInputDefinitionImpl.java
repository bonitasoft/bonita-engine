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

import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;

/**
 * @author Matthieu Chaffotte
 */
public class SInputDefinitionImpl extends SNamedElementImpl implements SInputDefinition {

    private static final long serialVersionUID = -5021740296501498639L;
    protected final List<SInputDefinition> inputDefinitions;
    protected final String description;
    protected final SType type;
    protected final boolean multiple;

    public SInputDefinitionImpl(final String name, final String description) {
        this(name, null, description, false, null);
    }

    public SInputDefinitionImpl(final String name, final SType type, final String description, final boolean multiple, final List<SInputDefinition> inputDefinitions) {
        super(name);
        this.description = description;
        this.multiple = multiple;
        this.type = type;
        this.inputDefinitions = new ArrayList<>();
        if (inputDefinitions != null) {
            this.inputDefinitions.addAll(inputDefinitions);
        }
    }

    public SInputDefinitionImpl(final String name, final SType type, final String description) {
        this(name, type, description, false);
    }

    public SInputDefinitionImpl(final String name, final SType type, final String description, final boolean multiple) {
        this(name, type, description, multiple, null);
    }

    public SInputDefinitionImpl(final InputDefinition input) {
        this(input.getName(), convertTypeToSType(input.getType()), input.getDescription(), input.isMultiple(), null);
        convertAndAddInputDefinitions(input);
    }

    public SInputDefinitionImpl(String name, String description, boolean multiple, List<SInputDefinition> inputDefinitions) {
        this(name, null, description, multiple, inputDefinitions);
    }

    protected static SType convertTypeToSType(final Type type2) {
        if (type2 == null) {
            return null;
        }
        return SType.valueOf(type2.toString());
    }

    private void convertAndAddInputDefinitions(final InputDefinition input) {
        for (final InputDefinition inputDefinition : input.getInputs()) {
            inputDefinitions.add(new SInputDefinitionImpl(inputDefinition));
        }
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
    public boolean hasChildren() {
        return !inputDefinitions.isEmpty();
    }

    @Override
    public List<SInputDefinition> getInputDefinitions() {
        return inputDefinitions;
    }

    @Override
    public boolean isMultiple() {
        return multiple;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SInputDefinitionImpl that = (SInputDefinitionImpl) o;
        return Objects.equals(multiple, that.multiple) &&
                Objects.equals(inputDefinitions, that.inputDefinitions) &&
                Objects.equals(description, that.description) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), inputDefinitions, description, type, multiple);
    }

    @Override
    public String toString() {
        return "SInputDefinitionImpl{" +
                "inputDefinitions=" + inputDefinitions +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", multiple=" + multiple +
                "} " + super.toString();
    }
}
