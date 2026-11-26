/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

import org.bonitasoft.engine.resources.STenantResource;
import org.bonitasoft.engine.resources.STenantResourceState;
import org.bonitasoft.engine.resources.TenantResourceType;

public class TenantResourceBuilder extends PersistentObjectBuilder<STenantResource, TenantResourceBuilder> {

    private String name;

    private byte[] content;
    private TenantResourceType type;

    private long lastUpdatedBy;
    private long lastUpdateDate;
    private STenantResourceState state;

    public static TenantResourceBuilder aTenantResource() {
        return new TenantResourceBuilder();
    }

    @Override
    public STenantResource _build() {
        return new STenantResource(name, type, content, lastUpdatedBy, lastUpdateDate, state);
    }

    public TenantResourceBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public TenantResourceBuilder withType(final TenantResourceType type) {
        this.type = type;
        return this;
    }

    public TenantResourceBuilder withContent(final byte[] content) {
        this.content = content;
        return this;
    }

    public TenantResourceBuilder lastUpdatedBy(final long userId) {
        this.lastUpdatedBy = userId;
        return this;
    }

    public TenantResourceBuilder withState(STenantResourceState state) {
        this.state = state;
        return this;
    }

    public TenantResourceBuilder withLastUpdateDate(long date) {
        this.lastUpdateDate = date;
        return this;
    }

    @Override
    TenantResourceBuilder getThisBuilder() {
        return this;
    }

}
