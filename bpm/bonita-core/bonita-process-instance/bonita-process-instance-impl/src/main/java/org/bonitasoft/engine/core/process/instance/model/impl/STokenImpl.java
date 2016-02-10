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
package org.bonitasoft.engine.core.process.instance.model.impl;

import org.bonitasoft.engine.core.process.instance.model.SToken;

/**
 * @author Celine Souchet
 */
public class STokenImpl extends SPersistenceObjectImpl implements SToken {

    private static final long serialVersionUID = 2003206305995006614L;

    private long processInstanceId;

    public STokenImpl() {
        super();
    }

    public STokenImpl(final long processInstanceId) {
        super();
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String getDiscriminator() {
        return SToken.class.getName();
    }

    @Override
    public long getProcessInstanceId() {
        return processInstanceId;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (processInstanceId ^ processInstanceId >>> 32);
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
        final STokenImpl other = (STokenImpl) obj;
        if (processInstanceId != other.processInstanceId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "STokenImpl [processInstanceId=" + processInstanceId + "]";
    }

}
