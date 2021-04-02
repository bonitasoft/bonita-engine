/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.business.application.ApplicationSearchDescriptor;
import org.bonitasoft.engine.business.application.model.AbstractSApplication;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 */
public class SearchApplicationDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> keys;

    private final Map<Class<? extends PersistentObject>, Set<String>> allFields;

    SearchApplicationDescriptor() {
        keys = new HashMap<>(13);
        keys.put(ApplicationSearchDescriptor.ID,
                new FieldDescriptor(SApplication.class, AbstractSApplication.ID));
        keys.put(ApplicationSearchDescriptor.TOKEN,
                new FieldDescriptor(SApplication.class, AbstractSApplication.TOKEN));
        keys.put(ApplicationSearchDescriptor.DISPLAY_NAME,
                new FieldDescriptor(SApplication.class, AbstractSApplication.DISPLAY_NAME));
        keys.put(ApplicationSearchDescriptor.VERSION,
                new FieldDescriptor(SApplication.class, AbstractSApplication.VERSION));
        keys.put(ApplicationSearchDescriptor.ICON_PATH,
                new FieldDescriptor(SApplication.class, AbstractSApplication.ICON_PATH));
        keys.put(ApplicationSearchDescriptor.CREATION_DATE,
                new FieldDescriptor(SApplication.class, AbstractSApplication.CREATION_DATE));
        keys.put(ApplicationSearchDescriptor.CREATED_BY,
                new FieldDescriptor(SApplication.class, AbstractSApplication.CREATED_BY));
        keys.put(ApplicationSearchDescriptor.LAST_UPDATE_DATE,
                new FieldDescriptor(SApplication.class, AbstractSApplication.LAST_UPDATE_DATE));
        keys.put(ApplicationSearchDescriptor.UPDATED_BY,
                new FieldDescriptor(SApplication.class, AbstractSApplication.UPDATED_BY));
        keys.put(ApplicationSearchDescriptor.STATE,
                new FieldDescriptor(SApplication.class, AbstractSApplication.STATE));
        keys.put(ApplicationSearchDescriptor.PROFILE_ID,
                new FieldDescriptor(SApplication.class, AbstractSApplication.PROFILE_ID));
        keys.put(ApplicationSearchDescriptor.LAYOUT_ID,
                new FieldDescriptor(SApplication.class, AbstractSApplication.LAYOUT_ID));
        keys.put(ApplicationSearchDescriptor.THEME_ID,
                new FieldDescriptor(SApplication.class, AbstractSApplication.THEME_ID));

        allFields = new HashMap<>(1);

        final Set<String> pageFields = new HashSet<>(5);
        pageFields.add(AbstractSApplication.TOKEN);
        pageFields.add(AbstractSApplication.DISPLAY_NAME);
        pageFields.add(AbstractSApplication.VERSION);
        pageFields.add(AbstractSApplication.ICON_PATH);
        pageFields.add(AbstractSApplication.STATE);
        allFields.put(SApplication.class, pageFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return keys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return allFields;
    }

}
