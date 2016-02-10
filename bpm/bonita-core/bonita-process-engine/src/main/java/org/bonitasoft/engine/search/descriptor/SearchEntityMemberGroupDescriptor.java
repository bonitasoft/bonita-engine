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
import org.bonitasoft.engine.entitymember.EntityMemberSearchDescriptor;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.builder.SGroupBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class SearchEntityMemberGroupDescriptor extends SearchEntityMemberDescriptor {

    private final Map<Class<? extends PersistentObject>, Set<String>> entityMemberAllFields;

    public SearchEntityMemberGroupDescriptor() {
        super();
        putField(EntityMemberSearchDescriptor.DISPLAY_NAME_PART1, new FieldDescriptor(SGroup.class, BuilderFactory.get(SGroupBuilderFactory.class).getNameKey()));
        putField(EntityMemberSearchDescriptor.DISPLAY_NAME_PART2, new FieldDescriptor(SGroup.class, BuilderFactory.get(SGroupBuilderFactory.class).getParentPathKey()));

        entityMemberAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> groupFields = new HashSet<String>(2);
        groupFields.add(BuilderFactory.get(SGroupBuilderFactory.class).getNameKey());
        groupFields.add(BuilderFactory.get(SGroupBuilderFactory.class).getParentPathKey());
        entityMemberAllFields.put(SGroup.class, groupFields);
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return entityMemberAllFields;
    }

}
