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
package org.bonitasoft.engine.core.category.model.builder.impl;

import org.bonitasoft.engine.core.category.model.SCategory;
import org.bonitasoft.engine.core.category.model.builder.SCategoryBuilder;
import org.bonitasoft.engine.core.category.model.builder.SCategoryBuilderFactory;
import org.bonitasoft.engine.core.category.model.impl.SCategoryImpl;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SCategoryBuilderFactoryImpl implements SCategoryBuilderFactory {

    static final String ID = "id";

    static final String NAME = "name";

    static final String DESCRIPTION = "description";

    static final String CREATOR = "creator";

    static final String CREATION_DATE = "creationDate";

    static final String LAST_UPDATE_DATE = "lastUpdateDate";

    @Override
    public SCategoryBuilder createNewInstance(final String name, final long creator) {
        final SCategoryImpl category = new SCategoryImpl(name);
        category.setCreator(creator);
        final long now = System.currentTimeMillis();
        category.setCreationDate(now);
        category.setLastUpdateDate(now);
        return new SCategoryBuilderImpl(category);
    }

    @Override
    public SCategoryBuilder createNewInstance(final SCategory originalCategory) {
        final SCategoryImpl category = new SCategoryImpl(originalCategory);
        final long now = System.currentTimeMillis();
        category.setCreationDate(now);
        category.setLastUpdateDate(now);
        return new SCategoryBuilderImpl(category);
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
    public String getDescriptionKey() {
        return DESCRIPTION;
    }

    @Override
    public String getCreatorKey() {
        return CREATOR;
    }

    @Override
    public String getCreationDateKey() {
        return CREATION_DATE;
    }

    @Override
    public String getLastUpdateDateKey() {
        return LAST_UPDATE_DATE;
    }

}
