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

import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Celine Souchet
 */
public class STenantUpdateBuilderImpl implements STenantUpdateBuilder {

    protected final EntityUpdateDescriptor descriptor;

    public STenantUpdateBuilderImpl(final EntityUpdateDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public STenantUpdateBuilder setName(final String name) {
        descriptor.addField(STenantUpdateBuilderFactory.NAME, name);
        return this;
    }

    @Override
    public STenantUpdateBuilder setDescription(final String description) {
        descriptor.addField(STenantUpdateBuilderFactory.DESCRIPTION, description);
        return this;
    }

    @Override
    public STenantUpdateBuilder setIconPath(final String iconPath) {
        descriptor.addField(STenantUpdateBuilderFactory.ICON_PATH, iconPath);
        return this;
    }

    @Override
    public STenantUpdateBuilder setIconName(final String iconName) {
        descriptor.addField(STenantUpdateBuilderFactory.ICON_NAME, iconName);
        return this;
    }

    @Override
    public STenantUpdateBuilder setStatus(final String status) {
        descriptor.addField(STenantUpdateBuilderFactory.STATUS, status);
        return this;
    }

    @Override
    public STenantUpdateBuilder setSecurityActivated(final boolean securityActivated) {
        descriptor.addField(STenantUpdateBuilderFactory.SECURITY_ACTIVATED, securityActivated);
        return this;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

}
