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

import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.platform.model.impl.STenantImpl;

/**
 * @author Charles Souillard
 * @author Yanyan Liu
 */
public class STenantBuilderFactoryImpl implements STenantBuilderFactory {

    @Override
    public STenantBuilder createNewInstance(final String name, final String createdBy, final long created, final String status, final boolean defaultTenant) {
        final STenantImpl object = new STenantImpl(name, createdBy, created, status, defaultTenant);
        return new STenantBuilderImpl(object);
    }

    @Override
    public String getCreatedByKey() {
        return "createdBy";
    }

    @Override
    public String getCreatedKey() {
        return "created";
    }

    @Override
    public String getDescriptionKey() {
        return "description";
    }

    @Override
    public String getIdKey() {
        return "id";
    }

    @Override
    public String getNameKey() {
        return "name";
    }

    @Override
    public String getStatusKey() {
        return "status";
    }

    @Override
    public String getIconNameKey() {
        return "iconName";
    }

    @Override
    public String getIconPathKey() {
        return "iconPath";
    }

    @Override
    public String getDefaultTenantKey() {
        return "defaultTenant";
    }

}
