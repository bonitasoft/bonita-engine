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
package org.bonitasoft.engine.recorder.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Celine Souchet
 */
public class DeleteAllRecord {

    private final Class<? extends PersistentObject> entityClass;

    private final List<FilterOption> filters;

    public DeleteAllRecord(final Class<? extends PersistentObject> entityClass, final List<FilterOption> filters) {
        this.entityClass = entityClass;
        this.filters = filters;
    }

    public Class<? extends PersistentObject> getEntityClass() {
        return entityClass;
    }

    public List<FilterOption> getFilters() {
        if (filters == null) {
            return Collections.unmodifiableList(new ArrayList<FilterOption>());
        }
        return Collections.unmodifiableList(filters);
    }

}
