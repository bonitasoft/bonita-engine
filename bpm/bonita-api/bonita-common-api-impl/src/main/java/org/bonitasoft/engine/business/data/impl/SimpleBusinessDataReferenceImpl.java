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
package org.bonitasoft.engine.business.data.impl;

import java.util.Objects;

import org.bonitasoft.engine.business.data.SimpleBusinessDataReference;

/**
 * @author Matthieu Chaffotte
 */
public class SimpleBusinessDataReferenceImpl extends BusinessDataReferenceImpl implements SimpleBusinessDataReference {

    private static final long serialVersionUID = -434357449996998735L;

    private final Long storageId;

    public SimpleBusinessDataReferenceImpl(final String name, final String type, final Long storageId) {
        super(name, type);
        this.storageId = storageId;
    }

    @Override
    public Long getStorageId() {
        return storageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SimpleBusinessDataReferenceImpl that = (SimpleBusinessDataReferenceImpl) o;
        return Objects.equals(storageId, that.storageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), storageId);
    }
}
