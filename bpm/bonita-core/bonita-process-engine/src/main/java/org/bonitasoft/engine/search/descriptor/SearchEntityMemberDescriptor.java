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
import java.util.Map;

import org.bonitasoft.engine.entitymember.EntityMemberSearchDescriptor;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMapping;
import org.bonitasoft.engine.identity.model.SUser;

/**
 * @author Emmanuel Duchastenier
 */
public abstract class SearchEntityMemberDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> fieldDescriptorMap;

    public SearchEntityMemberDescriptor() {
        fieldDescriptorMap = new HashMap<String, FieldDescriptor>(4);
        fieldDescriptorMap.put(EntityMemberSearchDescriptor.EXTERNAL_ID,
                new FieldDescriptor(SExternalIdentityMapping.class, SExternalIdentityMapping.EXTERNAL_ID_KEY));
        fieldDescriptorMap.put(EntityMemberSearchDescriptor.USER_ID,
                new FieldDescriptor(SExternalIdentityMapping.class, SExternalIdentityMapping.USER_ID_KEY));
        fieldDescriptorMap.put(EntityMemberSearchDescriptor.GROUP_ID,
                new FieldDescriptor(SExternalIdentityMapping.class, SExternalIdentityMapping.GROUP_ID_KEY));
        fieldDescriptorMap.put(EntityMemberSearchDescriptor.ROLE_ID,
                new FieldDescriptor(SExternalIdentityMapping.class, SExternalIdentityMapping.ROLE_ID_KEY));
        fieldDescriptorMap.put(EntityMemberSearchDescriptor.USER_NAME,
                new FieldDescriptor(SUser.class, SUser.USER_NAME));
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return fieldDescriptorMap;
    }

    protected void putField(final String fieldName, final FieldDescriptor descriptor) {
        fieldDescriptorMap.put(fieldName, descriptor);
    }

}
