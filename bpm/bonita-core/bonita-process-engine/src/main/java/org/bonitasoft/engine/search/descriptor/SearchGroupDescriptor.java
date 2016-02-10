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
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.identity.GroupSearchDescriptor;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.builder.SGroupBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Matthieu Chaffotte
 */
public class SearchGroupDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> groupKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> groupAllFields;

    SearchGroupDescriptor() {
        groupKeys = new HashMap<String, FieldDescriptor>(4);
        groupKeys.put(GroupSearchDescriptor.ID, new FieldDescriptor(SGroup.class, BuilderFactory.get(SGroupBuilderFactory.class).getIdKey()));
        groupKeys.put(GroupSearchDescriptor.NAME, new FieldDescriptor(SGroup.class, BuilderFactory.get(SGroupBuilderFactory.class).getNameKey()));
        groupKeys.put(GroupSearchDescriptor.PARENT_PATH, new FieldDescriptor(SGroup.class, BuilderFactory.get(SGroupBuilderFactory.class).getParentPathKey()));
        groupKeys.put(GroupSearchDescriptor.DISPLAY_NAME, new FieldDescriptor(SGroup.class, BuilderFactory.get(SGroupBuilderFactory.class).getDisplayNameKey()));

        groupAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> groupFields = new HashSet<String>(6);
        groupFields.add(BuilderFactory.get(SGroupBuilderFactory.class).getNameKey());
        groupFields.add(BuilderFactory.get(SGroupBuilderFactory.class).getDisplayNameKey());
        groupFields.add(BuilderFactory.get(SGroupBuilderFactory.class).getDescriptionKey());
        groupFields.add(BuilderFactory.get(SGroupBuilderFactory.class).getIconNameKey());
        groupFields.add(BuilderFactory.get(SGroupBuilderFactory.class).getIconPathKey());
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
