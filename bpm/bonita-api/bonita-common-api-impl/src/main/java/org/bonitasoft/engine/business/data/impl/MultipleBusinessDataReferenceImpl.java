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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bonitasoft.engine.business.data.MultipleBusinessDataReference;


/**
 * @author Matthieu Chaffotte
 */
public class MultipleBusinessDataReferenceImpl extends BusinessDataReferenceImpl implements MultipleBusinessDataReference {

    private static final long serialVersionUID = -8221290488745270659L;

    private final List<Long> storageIds;

    public MultipleBusinessDataReferenceImpl(final String name, final String type, final List<Long> storageIds) {
        super(name, type);
        this.storageIds = new ArrayList<Long>();
        for (final Long storageId : storageIds) {
            this.storageIds.add(storageId);
        }
    }

    @Override
    public List<Long> getStorageIds() {
        return storageIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MultipleBusinessDataReferenceImpl that = (MultipleBusinessDataReferenceImpl) o;
        return Objects.equals(storageIds, that.storageIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), storageIds);
    }
}
