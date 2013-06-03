/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import org.bonitasoft.engine.entitymember.EntityMemberSearchDescriptor;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMappingBuilders;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.builder.GroupBuilder;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.identity.model.builder.RoleBuilder;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class SearchEntityMemberRoleAndGroupDescriptor extends SearchEntityMemberDescriptor {

    private final Map<Class<? extends PersistentObject>, Set<String>> entityMemberAllFields;

    public SearchEntityMemberRoleAndGroupDescriptor(final SExternalIdentityMappingBuilders builders, final IdentityModelBuilder identityModelBuilder) {
        super(builders, identityModelBuilder);
        final GroupBuilder groupBuilder = identityModelBuilder.getGroupBuilder();
        final RoleBuilder roleBuilder = identityModelBuilder.getRoleBuilder();
        putField(EntityMemberSearchDescriptor.DISPLAY_NAME_PART1, new FieldDescriptor(SRole.class, roleBuilder.getNameKey()));
        putField(EntityMemberSearchDescriptor.DISPLAY_NAME_PART2, new FieldDescriptor(SGroup.class, groupBuilder.getNameKey()));
        putField(EntityMemberSearchDescriptor.DISPLAY_NAME_PART3, new FieldDescriptor(SGroup.class, groupBuilder.getParentPathKey()));

        entityMemberAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(2);
        final Set<String> roleFields = new HashSet<String>(1);
        roleFields.add(roleBuilder.getNameKey());
        entityMemberAllFields.put(SRole.class, roleFields);
        final Set<String> groupFields = new HashSet<String>(2);
        groupFields.add(groupBuilder.getNameKey());
        groupFields.add(groupBuilder.getParentPathKey());
        entityMemberAllFields.put(SGroup.class, groupFields);
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return entityMemberAllFields;
    }

}
