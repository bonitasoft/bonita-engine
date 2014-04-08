/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.operation.LeftOperandType;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class LeftOperandImpl implements LeftOperand {

    private static final long serialVersionUID = -6718721963287359848L;

    private String name;

    private boolean external;

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
        return external;
    }

    public void setExternal(final boolean external) {
        this.external = external;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (external ? 1231 : 1237);
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
        final LeftOperandImpl other = (LeftOperandImpl) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (external != other.external) {
            return false;
        }
        return true;
    }

    /**
     * @deprecated As of 6.0 use {@link Operation#getType()} instead
     */
    @Override
    @Deprecated
    public LeftOperandType getType() {
        return null;
    }

}
