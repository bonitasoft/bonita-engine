/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package com.bonitasoft.engine.business.application.impl;

import com.bonitasoft.engine.business.application.SApplicationBuilder;
import com.bonitasoft.engine.business.application.SApplicationBuilderFactory;
import com.bonitasoft.engine.business.application.SApplicationState;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationBuilderFactoryImpl implements SApplicationBuilderFactory {

    @Override
    public SApplicationBuilder createNewInstance(final String name, final String version, final String path, final long createdBy) {
        final long currentDate = System.currentTimeMillis();
        return new SApplicationBuilderImpl(new SApplicationImpl(name, version, path, currentDate, createdBy, SApplicationState.DEACTIVATED.name()));
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
    public String getVersionKey() {
        return "version";
    }

    @Override
    public String getPathKey() {
        return "path";
    }

    @Override
    public String getDescriptionKey() {
        return "description";
    }

    @Override
    public String getIconPathKey() {
        return "iconPath";
    }

    @Override
    public String getCreationDateKey() {
        return "creationDate";
    }

    @Override
    public String getCreatedByKey() {
        return "createdBy";
    }

    @Override
    public String getLastUpdatedDateKey() {
        return "lastUpdateDate";
    }

    @Override
    public String getUpdatedByKey() {
        return "updatedBy";
    }

    @Override
    public String getStateKey() {
        return "state";
    }

}
