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
package org.bonitasoft.engine.bpm.contract.impl;

import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Matthieu Chaffotte
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class InputDefinitionImpl extends InputContainerDefinitionImpl implements InputDefinition {

    private static final long serialVersionUID = 2836592506382887928L;
    @XmlAttribute
    protected final Type type;
    @XmlElement
    private final String description;
    @XmlAttribute
    private final String name;
    @XmlAttribute
    private final boolean multiple;

    public InputDefinitionImpl(final String name, final String description, final boolean multiple, Type type, final List<InputDefinition> inputDefinitions) {
        super(inputDefinitions == null ? new ArrayList<InputDefinition>() : inputDefinitions);
        this.description = description;
        this.name = name;
        this.multiple = multiple;
        this.type = type;
    }

    public InputDefinitionImpl(){
        super();
        this.description = null;
        this.name = null;
        this.multiple = false;
        this.type = Type.BYTE_ARRAY;
    }
    public InputDefinitionImpl(final String name, final String description, final boolean multiple) {
        this(name, description, multiple, null, new ArrayList<InputDefinition>());
    }

    public InputDefinitionImpl(final String name, final Type type, final String description, final boolean multiple) {
        this(name, description, multiple, type, new ArrayList<InputDefinition>());

    }

    public InputDefinitionImpl(final String name, final Type type, final String description) {
        this(name, description, false, type, new ArrayList<InputDefinition>());

    }

    public InputDefinitionImpl(final String name, final String description) {
        this(name, description, false, null, null);
    }

    public InputDefinitionImpl(final String name, final String description, final List<InputDefinition> inputDefinitions) {
        this(name, description, false, null, inputDefinitions);
    }

    public InputDefinitionImpl(final String name, final String description, final boolean multiple, final List<InputDefinition> inputDefinitions) {
        this(name, description, multiple, null, inputDefinitions);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isMultiple() {
        return multiple;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InputDefinitionImpl that = (InputDefinitionImpl) o;
        return Objects.equals(multiple, that.multiple) &&
                Objects.equals(description, that.description) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, name, multiple);
    }

    @Override
    public String toString() {
        return "InputDefinitionImpl{" +
                "description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", multiple=" + multiple +
                '}';
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean hasChildren() {
        return !getInputs().isEmpty();
    }

}
