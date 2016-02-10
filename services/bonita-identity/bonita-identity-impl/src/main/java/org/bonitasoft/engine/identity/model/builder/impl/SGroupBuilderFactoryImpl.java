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

import org.bonitasoft.engine.identity.model.builder.SGroupBuilder;
import org.bonitasoft.engine.identity.model.builder.SGroupBuilderFactory;
import org.bonitasoft.engine.identity.model.impl.SGroupImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class SGroupBuilderFactoryImpl implements SGroupBuilderFactory {

    private static final String PARENT_PATH = "parentPath";

    static final String ID = "id";

    static final String NAME = "name";

    static final String DESCRIPTION = "description";

    static final String DISPLAY_NAME = "displayName";

    static final String ICON_NAME = "iconName";

    static final String ICON_PATH = "iconPath";

    static final String CREATED_BY = "createdBy";

    static final String CREATION_DATE = "creationDate";

    static final String LAST_UPDATE = "lastUpdate";

    @Override
    public SGroupBuilder createNewInstance() {
        final SGroupImpl group = new SGroupImpl();
        return new SGroupBuilderImpl(group);
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

    @Override
    public String getIconNameKey() {
        return ICON_NAME;
    }

    @Override
    public String getIconPathKey() {
        return ICON_PATH;
    }

    @Override
    public String getCreatedByKey() {
        return CREATED_BY;
    }

    @Override
    public String getCreationDateKey() {
        return CREATION_DATE;
    }

    @Override
    public String getLastUpdateKey() {
        return LAST_UPDATE;
    }

    @Override
    public String getParentPathKey() {
        return PARENT_PATH;
    }

}
