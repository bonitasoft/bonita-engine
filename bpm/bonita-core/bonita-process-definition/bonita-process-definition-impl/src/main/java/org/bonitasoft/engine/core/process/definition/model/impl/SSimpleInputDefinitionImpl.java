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
package org.bonitasoft.engine.core.process.definition.model.impl;

import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
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

    public SSimpleInputDefinitionImpl(final SimpleInputDefinition input) {
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
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SSimpleInputDefinitionImpl other = (SSimpleInputDefinitionImpl) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isMultiple() {
        return multiple;
    }

}
