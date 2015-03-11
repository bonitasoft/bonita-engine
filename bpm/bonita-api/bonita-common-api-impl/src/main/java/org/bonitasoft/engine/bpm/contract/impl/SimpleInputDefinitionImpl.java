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

import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;

/**
 * @author Laurent Leseigneur
 */
public class SimpleInputDefinitionImpl extends InputDefinitionImpl implements SimpleInputDefinition {

    private static final long serialVersionUID = 7373361824507460819L;


    private final Type type;

    public SimpleInputDefinitionImpl(final String name, final Type type, final String description, final boolean multiple) {
        super(name, description, multiple);
        this.type = type;

    }

    public SimpleInputDefinitionImpl(final String name, final Type type, final String description) {
        super(name, description, false);
        this.type = type;

    }

    @Override
    public Type getType() {
        return type;
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
        switch (NaiveEqualityResult.checkEquality(this, obj)) {
            case RETURN_FALSE:
                return false;
            case RETURN_TRUE:
                return true;
            case CONTINUE:
            default:
                break;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final SimpleInputDefinitionImpl other = (SimpleInputDefinitionImpl) obj;
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SimpleInputDefinitionImpl [type=" + type + ", getDescription()=" + getDescription() + ", getName()=" + getName() + ", isMultiple()="
                + isMultiple() + ", toString()=" + super.toString() + "]";
    }


}
