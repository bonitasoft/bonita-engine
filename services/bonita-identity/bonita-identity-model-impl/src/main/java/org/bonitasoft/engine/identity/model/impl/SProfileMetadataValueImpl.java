/**
 * Copyright (C) 2010 BonitaSoft S.A.
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

import org.bonitasoft.engine.identity.model.SProfileMetadataValue;

/**
 * @author Anthony Birembaut
 * @author Matthieu Chaffotte
 */
public class SProfileMetadataValueImpl extends SPersistentObjectImpl implements SProfileMetadataValue {

    private static final long serialVersionUID = 1L;

    protected String userName;

    protected String metadataName;

    protected String value;

    public SProfileMetadataValueImpl() {
        super();
    }

    @Override
    public String getDiscriminator() {
        return SProfileMetadataValue.class.getName();
    }

    @Override
    public String getUserName() {
        return this.userName;
    }

    public void setUserId(final String userName) {
        this.userName = userName;
    }

    @Override
    public String getMetadataName() {
        return this.metadataName;
    }

    public void setMetadataId(final String metadataName) {
        this.metadataName = metadataName;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((metadataName == null) ? 0 : metadataName.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        final SProfileMetadataValueImpl other = (SProfileMetadataValueImpl) obj;
        if (metadataName == null) {
            if (other.metadataName != null) {
                return false;
            }
        } else if (!metadataName.equals(other.metadataName)) {
            return false;
        }
        if (userName == null) {
            if (other.userName != null) {
                return false;
            }
        } else if (!userName.equals(other.userName)) {
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
        return "SProfileMetadataValueImpl [metadataId=" + this.metadataName + ", userId=" + this.userName + ", value=" + this.value + ", getId()="
                + this.getId() + "]";
    }

}
