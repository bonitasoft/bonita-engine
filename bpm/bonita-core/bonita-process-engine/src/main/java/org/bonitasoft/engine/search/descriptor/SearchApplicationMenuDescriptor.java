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

import org.bonitasoft.engine.business.application.ApplicationMenuSearchDescriptor;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 */
public class SearchApplicationMenuDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> keys;

    private final Map<Class<? extends PersistentObject>, Set<String>> allFields;

    SearchApplicationMenuDescriptor() {
        keys = new HashMap<String, FieldDescriptor>(6);
        keys.put(ApplicationMenuSearchDescriptor.ID, new FieldDescriptor(SApplicationMenu.class, SApplicationMenu.ID));
        keys.put(ApplicationMenuSearchDescriptor.APPLICATION_PAGE_ID,
                new FieldDescriptor(SApplicationMenu.class, SApplicationMenu.APPLICATION_PAGE_ID));
        keys.put(ApplicationMenuSearchDescriptor.DISPLAY_NAME,
                new FieldDescriptor(SApplicationMenu.class, SApplicationMenu.DISPLAY_NAME));
        keys.put(ApplicationMenuSearchDescriptor.INDEX,
                new FieldDescriptor(SApplicationMenu.class, SApplicationMenu.INDEX));
        keys.put(ApplicationMenuSearchDescriptor.APPLICATION_ID,
                new FieldDescriptor(SApplicationMenu.class, SApplicationMenu.APPLICAITON_ID));
        keys.put(ApplicationMenuSearchDescriptor.PARENT_ID,
                new FieldDescriptor(SApplicationMenu.class, SApplicationMenu.PARENT_ID));

        allFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);

        final Set<String> pageFields = new HashSet<String>(1);
        pageFields.add(SApplicationMenu.DISPLAY_NAME);
        allFields.put(SApplicationMenu.class, pageFields);
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
