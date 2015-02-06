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
package org.bonitasoft.engine.bpm.userfilter.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bonitasoft.engine.bpm.internal.NamedElementImpl;
import org.bonitasoft.engine.bpm.userfilter.UserFilterDefinition;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Baptiste Mesta
 */
public class UserFilterDefinitionImpl extends NamedElementImpl implements UserFilterDefinition {

    private static final long serialVersionUID = -6045216424839658552L;

    private final String filterId;

    private final String version;

    private final Map<String, Expression> inputs = new HashMap<String, Expression>();

    public UserFilterDefinitionImpl(final String name, final String filterId, final String version) {
        super(name);
        this.filterId = filterId;
        this.version = version;
    }

    @Override
    public String getUserFilterId() {
        return filterId;
    }

    @Override
    public Map<String, Expression> getInputs() {
        return inputs;
    }

    public void addInput(final String name, final Expression expression) {
        inputs.put(name, expression);
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (filterId == null ? 0 : filterId.hashCode());
        result = prime * result + (inputs == null ? 0 : inputs.hashCode());
        result = prime * result + (version == null ? 0 : version.hashCode());
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
        final UserFilterDefinitionImpl other = (UserFilterDefinitionImpl) obj;
        if (filterId == null) {
            if (other.filterId != null) {
                return false;
            }
        } else if (!filterId.equals(other.filterId)) {
            return false;
        }
        if (inputs == null) {
            if (other.inputs != null) {
                return false;
            }
        } else if (!inputs.equals(other.inputs)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final int maxLen = 5;
        final StringBuilder builder = new StringBuilder();
        builder.append("UserFilterDefinitionImpl [filterId=");
        builder.append(filterId);
        builder.append(", version=");
        builder.append(version);
        builder.append(", inputs=");
        builder.append(inputs != null ? toString(inputs.entrySet(), maxLen) : null);
        builder.append("]");
        return builder.toString();
    }

    private String toString(final Collection<?> collection, final int maxLen) {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (final Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(iterator.next());
        }
        builder.append("]");
        return builder.toString();
    }

}
