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
package org.bonitasoft.engine.archive.impl;

import org.bonitasoft.engine.archive.SArchiveDescriptor;

public class SArchiveDescriptorImpl implements SArchiveDescriptor {

    private final long oldestTime;

    private final long newestTime;

    private final String name;

    public SArchiveDescriptorImpl(final String name, final long oldestTime, final long newestTime) {
        this.name = name;
        this.oldestTime = oldestTime;
        this.newestTime = newestTime;
    }

    @Override
    public long getOldestTime() {
        return oldestTime;
    }

    @Override
    public long getNewestTime() {
        return newestTime;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (newestTime ^ newestTime >>> 32);
        result = prime * result + (int) (oldestTime ^ oldestTime >>> 32);
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
        final SArchiveDescriptorImpl other = (SArchiveDescriptorImpl) obj;
        if (newestTime != other.newestTime) {
            return false;
        }
        if (oldestTime != other.oldestTime) {
            return false;
        }
        return true;
    }

}
