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
package org.bonitasoft.engine.bpm.data.impl;

import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public class DataDefinitionImpl extends NamedElementImpl implements DataDefinition {

    private static final long serialVersionUID = -4126105713210029929L;

    private String description;

    private String type;

    private boolean transientData;

    private String className;

    private Expression defaultValueExpression;

    public DataDefinitionImpl(final String name, final Expression defaultValueExpression) {
        super(name);
        this.defaultValueExpression = defaultValueExpression;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getClassName() {
        return className;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public boolean isTransientData() {
        return transientData;
    }

    @Override
    public Expression getDefaultValueExpression() {
        return defaultValueExpression;
    }

    public void setTransientData(final boolean transientData) {
        this.transientData = transientData;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setDefaultValueExpression(final Expression defaultValueExpression) {
        this.defaultValueExpression = defaultValueExpression;
    }

    public void setClassName(final String className) {
        this.className = className;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (className == null ? 0 : className.hashCode());
        result = prime * result + (defaultValueExpression == null ? 0 : defaultValueExpression.hashCode());
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (transientData ? 1231 : 1237);
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
        final DataDefinitionImpl other = (DataDefinitionImpl) obj;
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!className.equals(other.className)) {
            return false;
        }
        if (defaultValueExpression == null) {
            if (other.defaultValueExpression != null) {
                return false;
            }
        } else if (!defaultValueExpression.equals(other.defaultValueExpression)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (transientData != other.transientData) {
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
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("DataDefinitionImpl [name=");
        builder.append(getName());
        builder.append(", description=");
        builder.append(description);
        builder.append(", type=");
        builder.append(type);
        builder.append(", transientData=");
        builder.append(transientData);
        builder.append(", className=");
        builder.append(className);
        builder.append(", defaultValueExpression=");
        builder.append(defaultValueExpression);
        builder.append("]");
        return builder.toString();
    }

}
