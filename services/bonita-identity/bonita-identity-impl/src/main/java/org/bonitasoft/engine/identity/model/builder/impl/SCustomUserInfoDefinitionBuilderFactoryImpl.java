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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionBuilder;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionBuilderFactory;
import org.bonitasoft.engine.identity.model.impl.SCustomUserInfoDefinitionImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SCustomUserInfoDefinitionBuilderFactoryImpl implements SCustomUserInfoDefinitionBuilderFactory {

    static final String ID = "id";

    static final String NAME = "name";

    static final String DESCRIPTION = "description";

    static final String DISPLAY_NAME = "displayName";

    
    @Override
    public SCustomUserInfoDefinitionBuilder createNewInstance() {
        final SCustomUserInfoDefinitionImpl entity = new SCustomUserInfoDefinitionImpl();
        return new SCustomUserInfoDefinitionBuilderImpl(entity);
    }

    @Override
    public String getIdKey() {
        return ID;
    }

    @Override
    public String getNameKey() {
        return NAME;
    }

    @Override
    public String getDisplayNameKey() {
        return DISPLAY_NAME;
    }

    @Override
    public String getDescriptionKey() {
        return DESCRIPTION;
    }

}
