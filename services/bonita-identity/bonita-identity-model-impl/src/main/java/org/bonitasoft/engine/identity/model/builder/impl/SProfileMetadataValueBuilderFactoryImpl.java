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

import org.bonitasoft.engine.identity.model.builder.SProfileMetadataValueBuilder;
import org.bonitasoft.engine.identity.model.builder.SProfileMetadataValueBuilderFactory;
import org.bonitasoft.engine.identity.model.impl.SProfileMetadataValueImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SProfileMetadataValueBuilderFactoryImpl implements SProfileMetadataValueBuilderFactory {

    static final String ID = "id";

    static final String USER_NAME = "userName";

    static final String METADATA_NAME = "metadataName";

    static final String VALUE = "value";

    @Override
    public SProfileMetadataValueBuilder createNewInstance() {
        final SProfileMetadataValueImpl entity = new SProfileMetadataValueImpl();
        return new SProfileMetadataValueBuilderImpl(entity);
    }

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
