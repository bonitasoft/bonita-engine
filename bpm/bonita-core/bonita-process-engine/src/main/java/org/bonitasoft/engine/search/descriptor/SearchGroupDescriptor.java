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

import org.bonitasoft.engine.identity.GroupSearchDescriptor;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Matthieu Chaffotte
 */
public class SearchGroupDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> groupKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> groupAllFields;

    SearchGroupDescriptor() {
        groupKeys = new HashMap<>(4);
        groupKeys.put(GroupSearchDescriptor.ID, new FieldDescriptor(SGroup.class, SGroup.ID));
        groupKeys.put(GroupSearchDescriptor.NAME, new FieldDescriptor(SGroup.class, SGroup.NAME));
        groupKeys.put(GroupSearchDescriptor.PARENT_PATH, new FieldDescriptor(SGroup.class, SGroup.PARENT_PATH));
        groupKeys.put(GroupSearchDescriptor.DISPLAY_NAME,
                new FieldDescriptor(SGroup.class, SGroup.DISPLAY_NAME));

        groupAllFields = new HashMap<>(1);
        final Set<String> groupFields = new HashSet<>(3);
        groupFields.add(SGroup.NAME);
        groupFields.add(SGroup.DISPLAY_NAME);
        groupFields.add(SGroup.DESCRIPTION);
        groupAllFields.put(SGroup.class, groupFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return groupKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return groupAllFields;
    }

}
