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

import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilderFactory;
import org.bonitasoft.engine.platform.model.impl.SPlatformImpl;

/**
 * @author Charles Souillard
 */
public class SPlatformBuilderFactoryImpl implements SPlatformBuilderFactory {

    @Override
    public SPlatformBuilder createNewInstance(final String version, final String previousVersion, final String initialVersion, final String createdBy,
            final long created) {
        final SPlatformImpl object = new SPlatformImpl(version, previousVersion, initialVersion, createdBy, created);
        return new SPlatformBuilderImpl(object);
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
    public String getIdKey() {
        return "id";
    }

    @Override
    public String getInitialVersionKey() {
        return "initialVersion";
    }

    @Override
    public String getPreviousVersionKey() {
        return "previousVersion";
    }

    @Override
    public String getVersionKey() {
        return "version";
    }

}
