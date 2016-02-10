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
package org.bonitasoft.engine.platform.model.builder.impl;

import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.platform.model.impl.STenantImpl;

/**
 * @author Charles Souillard
 * @author Yanyan Liu
 */
public class STenantBuilderImpl implements STenantBuilder {

    private final STenantImpl object;
    
    public STenantBuilderImpl(final STenantImpl object) {
        super();
        this.object = object;
    }

    @Override
    public STenant done() {
        return this.object;
    }

    @Override
    public STenantBuilder setDescription(final String description) {
        this.object.setDescription(description);
        return this;
    }

    @Override
    public STenantBuilder setIconName(final String iconName) {
        this.object.setIconName(iconName);
        return this;
    }

    @Override
    public STenantBuilder setIconPath(final String iconPath) {
        this.object.setIconPath(iconPath);
        return this;
    }

    @Override
    public STenantBuilder setDefaultTenant(final boolean defaultTenant) {
        this.object.setDefaultTenant(defaultTenant);
        return this;
    }

}
