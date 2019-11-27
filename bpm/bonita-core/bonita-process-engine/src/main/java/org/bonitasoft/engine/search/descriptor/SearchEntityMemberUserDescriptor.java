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

import org.bonitasoft.engine.entitymember.EntityMemberSearchDescriptor;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class SearchEntityMemberUserDescriptor extends SearchEntityMemberDescriptor {

    private final Map<Class<? extends PersistentObject>, Set<String>> entityMemberAllFields;

    public SearchEntityMemberUserDescriptor() {
        super();
        putField(EntityMemberSearchDescriptor.DISPLAY_NAME_PART1, new FieldDescriptor(SUser.class, SUser.FIRST_NAME));
        putField(EntityMemberSearchDescriptor.DISPLAY_NAME_PART2, new FieldDescriptor(SUser.class, SUser.LAST_NAME));
        putField(EntityMemberSearchDescriptor.DISPLAY_NAME_PART3, new FieldDescriptor(SUser.class, SUser.USER_NAME));

        entityMemberAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(3);
        final Set<String> userFields = new HashSet<String>(3);
        userFields.add(SUser.FIRST_NAME);
        userFields.add(SUser.LAST_NAME);
        userFields.add(SUser.USER_NAME);
        entityMemberAllFields.put(SUser.class, userFields);
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return entityMemberAllFields;
    }

}
