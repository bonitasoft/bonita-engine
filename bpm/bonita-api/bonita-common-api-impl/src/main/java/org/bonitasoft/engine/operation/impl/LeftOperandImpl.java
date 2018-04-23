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
package org.bonitasoft.engine.operation.impl;

import org.bonitasoft.engine.operation.LeftOperand;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class LeftOperandImpl implements LeftOperand {

    private static final long serialVersionUID = -6718721963287359848L;
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String type;

    public LeftOperandImpl() {
        // default when not set
        type = LeftOperand.TYPE_DATA;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @deprecated As of 6.0 replaced by {@link #setName(String)}
     */
    @Deprecated
    public void setDataName(final String dataName) {
        name = dataName;
    }

    /**
     * @deprecated As of 6.0 replaced by {@link #getName()}
     */
    @Deprecated
    @Override
    public String getDataName() {
        return name;
    }

    @Override
    public boolean isExternal() {
        return LeftOperand.TYPE_EXTERNAL_DATA.equals(type);
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
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
        LeftOperandImpl other = (LeftOperandImpl) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
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

    /**
     * @deprecated As of 6.0 use {@link org.bonitasoft.engine.operation.Operation#getType()} instead
     */
    @Deprecated
    @Override
    public String toString() {
        return "LeftOperandImpl [name=" + name + ", type=" + type + "]";
    }

}
