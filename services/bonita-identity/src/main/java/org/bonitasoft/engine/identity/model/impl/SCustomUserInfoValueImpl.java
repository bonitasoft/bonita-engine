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
package org.bonitasoft.engine.identity.model.impl;

import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;

/**
 * @author Anthony Birembaut
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class SCustomUserInfoValueImpl extends SPersistentObjectImpl implements SCustomUserInfoValue {

    private static final long serialVersionUID = 1L;

    protected long userId;

    protected long definitionId;

    protected String value;

    public SCustomUserInfoValueImpl() {
        super();
    }

    @Override
    public String getDiscriminator() {
        return SCustomUserInfoValue.class.getName();
    }

    @Override
    public long getUserId() {
        return userId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    @Override
    public long getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(final long definitionId) {
        this.definitionId = definitionId;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (definitionId ^ definitionId >>> 32);
        result = prime * result + (int) (userId ^ userId >>> 32);
        result = prime * result + (value == null ? 0 : value.hashCode());
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
        final SCustomUserInfoValueImpl other = (SCustomUserInfoValueImpl) obj;
        if (definitionId != other.definitionId) {
            return false;
        }
        if (userId != other.userId) {
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

    @Override
    public String toString() {
        return "SCustomUserInfoValueImpl [definitionId=" + definitionId + ", userId=" + userId + ", value=" + value + ", getId()=" + getId() + "]";
    }

}
