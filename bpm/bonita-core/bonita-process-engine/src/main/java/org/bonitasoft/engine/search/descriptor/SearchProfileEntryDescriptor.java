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
import org.bonitasoft.engine.profile.ProfileEntrySearchDescriptor;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilderFactory;
import org.bonitasoft.engine.profile.model.SProfileEntry;

/**
 * @author Celine Souchet
 */
public class SearchProfileEntryDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> profileAllFields;

    public SearchProfileEntryDescriptor() {
        searchEntityKeys = new HashMap<String, FieldDescriptor>(7);
        searchEntityKeys.put(ProfileEntrySearchDescriptor.ID, new FieldDescriptor(SProfileEntry.class, SProfileEntryBuilderFactory.ID));
        searchEntityKeys.put(ProfileEntrySearchDescriptor.NAME, new FieldDescriptor(SProfileEntry.class, SProfileEntryBuilderFactory.NAME));
        searchEntityKeys.put(ProfileEntrySearchDescriptor.PROFILE_ID, new FieldDescriptor(SProfileEntry.class, SProfileEntryBuilderFactory.PROFILE_ID));
        searchEntityKeys.put(ProfileEntrySearchDescriptor.PARENT_ID, new FieldDescriptor(SProfileEntry.class, SProfileEntryBuilderFactory.PARENT_ID));
        searchEntityKeys.put(ProfileEntrySearchDescriptor.INDEX, new FieldDescriptor(SProfileEntry.class, SProfileEntryBuilderFactory.INDEX));
        searchEntityKeys.put(ProfileEntrySearchDescriptor.PAGE, new FieldDescriptor(SProfileEntry.class, SProfileEntryBuilderFactory.PAGE));
        searchEntityKeys.put(ProfileEntrySearchDescriptor.CUSTOM, new FieldDescriptor(SProfileEntry.class, SProfileEntryBuilderFactory.CUSTOM));

        profileAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> fields = new HashSet<String>(3);
        fields.add(SProfileEntryBuilderFactory.NAME);
        profileAllFields.put(SProfileEntry.class, fields);
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
