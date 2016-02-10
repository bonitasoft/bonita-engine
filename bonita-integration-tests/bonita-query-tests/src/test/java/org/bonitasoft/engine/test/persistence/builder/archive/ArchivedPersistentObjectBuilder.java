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
package org.bonitasoft.engine.test.persistence.builder.archive;

import java.util.Random;

import org.bonitasoft.engine.persistence.ArchivedPersistentObject;

public abstract class ArchivedPersistentObjectBuilder<T extends ArchivedPersistentObject, B extends ArchivedPersistentObjectBuilder<T, B>> {

    public static final long DEFAULT_TENANT_ID = 1L;

    protected long id = new Random().nextLong();

    protected T persistentObject;

    protected B thisBuilder;

    public ArchivedPersistentObjectBuilder() {
        thisBuilder = getThisBuilder();
    }

    public T build() {
        persistentObject = _build();
        fill(persistentObject);
        return persistentObject;
    }

    protected T fill(T persistent) {
        persistent.setId(id);
        persistent.setTenantId(DEFAULT_TENANT_ID);
        return persistent;
    }

    abstract B getThisBuilder();

    abstract T _build();
}
