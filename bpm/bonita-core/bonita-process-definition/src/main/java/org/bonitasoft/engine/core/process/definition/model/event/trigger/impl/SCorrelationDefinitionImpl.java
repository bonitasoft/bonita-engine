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
package org.bonitasoft.engine.core.process.definition.model.event.trigger.impl;

import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCorrelationDefinition;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Baptiste Mesta
 */
public class SCorrelationDefinitionImpl implements SCorrelationDefinition {

    private static final long serialVersionUID = -8249911809919027354L;

    private SExpression key;

    private SExpression value;

    public SCorrelationDefinitionImpl(final SExpression key, final SExpression value) {
        super();
        this.key = key;
        this.value = value;
    }

    @Override
    public SExpression getKey() {
        return key;
    }

    @Override
    public SExpression getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (key == null ? 0 : key.hashCode());
        result = prime * result + (value == null ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SCorrelationDefinitionImpl other = (SCorrelationDefinitionImpl) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    public void setKey(final SExpression key) {
        this.key = key;
    }

    public void setValue(final SExpression value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "SCorrelationDefinitionImpl [key=" + key + ", value=" + value + "]";
    }

}
