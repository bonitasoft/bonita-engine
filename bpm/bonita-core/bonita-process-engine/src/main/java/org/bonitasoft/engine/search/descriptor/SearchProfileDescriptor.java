/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
