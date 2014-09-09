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

import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;

/**
 * @author Matthieu Chaffotte
 */
public class InputDefinitionImpl implements InputDefinition {

    private static final long serialVersionUID = 2836592506382887928L;

    private final Type type;

    private final String description;

    private final String name;

    public InputDefinitionImpl(final String name, final Type type, final String description) {
        this.description = description;
        this.name = name;
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
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
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (type == null ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InputDefinitionImpl other = (InputDefinitionImpl) obj;
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "InputDefinitionImpl [type=" + type + ", description=" + description + ", name=" + name + "]";
    }

}
