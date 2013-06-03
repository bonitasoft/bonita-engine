/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.SProfileMetadataValue;
import org.bonitasoft.engine.identity.model.builder.ProfileMetadataValueBuilder;
import org.bonitasoft.engine.identity.model.impl.SProfileMetadataValueImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ProfileMetadataValueBuilderImpl implements ProfileMetadataValueBuilder {

    private SProfileMetadataValueImpl entity;

    static final String ID = "id";

    static final String USER_NAME = "userName";

    static final String METADATA_NAME = "metadataName";

    static final String VALUE = "value";

    public ProfileMetadataValueBuilderImpl setId(final long id) {
        entity.setId(id);
        return this;
    }

    @Override
    public ProfileMetadataValueBuilderImpl createNewInstance() {
        entity = new SProfileMetadataValueImpl();
        return this;
    }

    @Override
    public SProfileMetadataValue done() {
        return entity;
    }

    @Override
    public ProfileMetadataValueBuilder setMetadataName(final String metadataName) {
        entity.setMetadataId(metadataName);
        return this;
    }

    @Override
    public ProfileMetadataValueBuilder setUserName(final String userName) {
        entity.setUserId(userName);
        return this;
    }

    @Override
    public ProfileMetadataValueBuilder setValue(final String value) {
        entity.setValue(value);
        return this;
    }

    @Override
    public String getIdKey() {
        return ID;
    }

    @Override
    public String getUserNameKey() {
        return USER_NAME;
    }

    @Override
    public String getMetadataNameKey() {
        return METADATA_NAME;
    }

    @Override
    public String getValueKey() {
        return VALUE;
    }

}
