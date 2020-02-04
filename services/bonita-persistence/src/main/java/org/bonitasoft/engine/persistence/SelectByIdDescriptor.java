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
package org.bonitasoft.engine.persistence;

import static java.util.Collections.emptyMap;
import static org.bonitasoft.engine.persistence.QueryOptions.ONE_RESULT;

import java.util.Map;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class SelectByIdDescriptor<T extends PersistentObject> extends AbstractSelectDescriptor<T> {

    private final long id;
    private boolean readonly;

    public SelectByIdDescriptor(final Class<T> entityType, final long id) {
        this(entityType, id, false);
    }

    public SelectByIdDescriptor(final Class<T> entityType, final long id, final boolean readonly) {
        super(null, entityType, entityType);
        this.id = id;
        this.readonly = readonly;
    }

    public long getId() {
        return id;
    }

    boolean isReadOnly() {
        return readonly;
    }

    @Override
    public Map<String, Object> getInputParameters() {
        return emptyMap();
    }

    @Override
    public int getStartIndex() {
        return 0;
    }

    @Override
    public int getPageSize() {
        return 1;
    }

    @Override
    public boolean hasAFilter() {
        return false;
    }

    @Override
    public QueryOptions getQueryOptions() {
        return ONE_RESULT;
    }

    @Override
    public boolean hasOrderByParameters() {
        return false;
    }
}
