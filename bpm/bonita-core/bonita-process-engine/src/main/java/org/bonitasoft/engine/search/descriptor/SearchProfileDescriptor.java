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

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.profile.ProfileSearchDescriptor;
import org.bonitasoft.engine.profile.builder.SProfileBuilderFactory;
import org.bonitasoft.engine.profile.model.SProfile;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SearchProfileDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> profileAllFields;

    public SearchProfileDescriptor() {
        searchEntityKeys = new HashMap<String, FieldDescriptor>(1);
        searchEntityKeys.put(ProfileSearchDescriptor.ID, new FieldDescriptor(SProfile.class, SProfileBuilderFactory.ID));
        searchEntityKeys.put(ProfileSearchDescriptor.NAME, new FieldDescriptor(SProfile.class, SProfileBuilderFactory.NAME));

        profileAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> fields = new HashSet<String>(3);
        fields.add(SProfileBuilderFactory.NAME);
        profileAllFields.put(SProfile.class, fields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return searchEntityKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return profileAllFields;
    }

}
