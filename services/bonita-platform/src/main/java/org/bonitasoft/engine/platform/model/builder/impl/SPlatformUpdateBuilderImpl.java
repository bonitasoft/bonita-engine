/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.platform.model.builder.impl;

import lombok.Builder;
import org.bonitasoft.engine.platform.model.builder.SPlatformUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Haroun EL ALAMI
 */
@Builder
public class SPlatformUpdateBuilderImpl implements SPlatformUpdateBuilder {

    protected final EntityUpdateDescriptor descriptor;

    public SPlatformUpdateBuilderImpl(final EntityUpdateDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public SPlatformUpdateBuilder setApplicationVersion(String version) {
        descriptor.addField(APPLICATION_VERSION, version);
        return this;
    }

    @Override
    public SPlatformUpdateBuilder setMaintenanceMessage(final String name) {
        descriptor.addField(MAINTENANCE_MESSAGE, name);
        return this;
    }

    @Override
    public SPlatformUpdateBuilder setMaintenanceMessageActive(final boolean state) {
        descriptor.addField(MAINTENANCE_MESSAGE_ACTIVE, state);
        return this;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

}
