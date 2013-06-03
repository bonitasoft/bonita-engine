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
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class SearchEntityMemberUserDescriptor extends SearchEntityMemberDescriptor {

    private final Map<Class<? extends PersistentObject>, Set<String>> entityMemberAllFields;

    public SearchEntityMemberUserDescriptor(final SExternalIdentityMappingBuilders builders, final IdentityModelBuilder identityModelBuilder) {
        super(builders, identityModelBuilder);
        final SUserBuilder userBuilder = identityModelBuilder.getUserBuilder();
        putField(EntityMemberSearchDescriptor.DISPLAY_NAME_PART1, new FieldDescriptor(SUser.class, userBuilder.getFirstNameKey()));
        putField(EntityMemberSearchDescriptor.DISPLAY_NAME_PART2, new FieldDescriptor(SUser.class, userBuilder.getLastNameKey()));
        putField(EntityMemberSearchDescriptor.DISPLAY_NAME_PART3, new FieldDescriptor(SUser.class, userBuilder.getUserNameKey()));

        entityMemberAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(3);
        final Set<String> userFields = new HashSet<String>(3);
        userFields.add(userBuilder.getFirstNameKey());
        userFields.add(userBuilder.getLastNameKey());
        userFields.add(userBuilder.getUserNameKey());
        entityMemberAllFields.put(SUser.class, userFields);
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return entityMemberAllFields;
    }

}
