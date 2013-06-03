/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.profile.builder.impl;

import org.bonitasoft.engine.profile.builder.SProfileBuilder;
import org.bonitasoft.engine.profile.builder.SProfileUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Celine Souchet
 */
public class SProfileUpdateBuilderImpl implements SProfileUpdateBuilder {

    protected final EntityUpdateDescriptor descriptor;

    public SProfileUpdateBuilderImpl() {
        descriptor = new EntityUpdateDescriptor();
    }

    @Override
    public SProfileUpdateBuilder setDescription(final String description) {
        descriptor.addField(SProfileBuilder.DESCRIPTION, description);
        return this;
    }

    @Override
    public SProfileUpdateBuilder setIconPath(final String iconPath) {
        descriptor.addField(SProfileBuilder.ICON_PATH, iconPath);
        return this;
    }

    @Override
    public SProfileUpdateBuilder setName(final String name) {
        descriptor.addField(SProfileBuilder.NAME, name);
        return this;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

}
