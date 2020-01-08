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
package org.bonitasoft.engine.test.persistence.builder;

import java.util.Random;

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.persistence.PersistentObject;

public abstract class PersistentObjectBuilder<T extends PersistentObject, B extends PersistentObjectBuilder<T, B>> {

    public static final long DEFAULT_TENANT_ID = 1L;

    protected long id = new Random().nextLong();
    protected long tenantId;

    protected T persistentObject;

    protected B thisBuilder;

    public PersistentObjectBuilder() {
        thisBuilder = getThisBuilder();
    }

    public T build() {
        persistentObject = _build();
        fill(persistentObject);
        return persistentObject;
    }

    protected T fill(T persistent) {
        persistent.setId(id);
        if (!isTenantIdSet(persistent)) {
            persistent.setTenantId(DEFAULT_TENANT_ID);
        }
        return persistent;
    }

    private boolean isTenantIdSet(T persistent) {
        Long tenantId = null;
        try {
            tenantId = ClassReflector.invokeGetter(persistent, "getTenantId");
        } catch (final Exception ignored) {
            //not set
        }
        return tenantId != null && tenantId > 0;
    }

    abstract B getThisBuilder();

    abstract T _build();

}
