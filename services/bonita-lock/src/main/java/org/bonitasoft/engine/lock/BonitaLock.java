/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.lock;

import java.util.Objects;

public class BonitaLock {

    private final String objectType;
    private final long objectToLockId;

    public BonitaLock(final String objectType, final long objectToLockId) {
        super();
        this.objectType = objectType;
        this.objectToLockId = objectToLockId;
    }

    public String getObjectType() {
        return objectType;
    }

    public long getObjectToLockId() {
        return objectToLockId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BonitaLock that = (BonitaLock) o;
        return objectToLockId == that.objectToLockId &&
                Objects.equals(objectType, that.objectType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, objectToLockId);
    }

    @Override
    public String toString() {
        return "BonitaLock[" + objectType + ":" + objectToLockId + "]";
    }

}
