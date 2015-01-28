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
package org.bonitasoft.engine.business.application.model.builder.impl;

import org.bonitasoft.engine.business.application.model.SApplicationState;
import org.bonitasoft.engine.business.application.model.builder.SApplicationBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationBuilderFactory;
import org.bonitasoft.engine.business.application.model.impl.SApplicationImpl;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationBuilderFactoryImpl implements SApplicationBuilderFactory {

    @Override
    public SApplicationBuilder createNewInstance(final String token, final String displayName, final String version, final long createdBy) {
        final long currentDate = System.currentTimeMillis();
        return new SApplicationBuilderImpl(new SApplicationImpl(token, displayName, version, currentDate, createdBy, SApplicationState.ACTIVATED.name()));
    }

    @Override
    public String getIdKey() {
        return SApplicationFields.ID;
    }

    @Override
    public String getTokenKey() {
        return SApplicationFields.TOKEN;
    }

    @Override
    public String getDisplayNameKey() {
        return SApplicationFields.DISPLAY_NAME;
    }

    @Override
    public String getVersionKey() {
        return SApplicationFields.VERSION;
    }

    @Override
    public String getDescriptionKey() {
        return SApplicationFields.DESCRIPTION;
    }

    @Override
    public String getIconPathKey() {
        return SApplicationFields.ICON_PATH;
    }

    @Override
    public String getCreationDateKey() {
        return SApplicationFields.CREATION_DATE;
    }

    @Override
    public String getCreatedByKey() {
        return SApplicationFields.CREATED_BY;
    }

    @Override
    public String getLastUpdatedDateKey() {
        return SApplicationFields.LAST_UPDATE_DATE;
    }

    @Override
    public String getUpdatedByKey() {
        return SApplicationFields.UPDATED_BY;
    }

    @Override
    public String getStateKey() {
        return SApplicationFields.STATE;
    }

    @Override
    public String getProfileIdKey() {
        return SApplicationFields.PROFILE_ID;
    }

}
