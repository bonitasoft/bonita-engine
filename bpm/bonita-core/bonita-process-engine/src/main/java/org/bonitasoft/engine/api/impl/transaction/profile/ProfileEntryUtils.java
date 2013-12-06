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
package org.bonitasoft.engine.api.impl.transaction.profile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.profile.builder.SProfileEntryBuilderFactory;
import org.bonitasoft.engine.profile.model.SProfileEntry;

public class ProfileEntryUtils {

    /**
     * Profile entry attributes
     */
    public static enum ProfileEntryType {
        folder, link
    }

    public static HashMap<String, Serializable> profileEntryToMap(final SProfileEntry profileEntry) {
        final HashMap<String, Serializable> profileEntryMap = new HashMap<String, Serializable>();
        profileEntryMap.put(SProfileEntryBuilderFactory.ID, profileEntry.getId());
        profileEntryMap.put(SProfileEntryBuilderFactory.PARENT_ID, profileEntry.getParentId());
        profileEntryMap.put(SProfileEntryBuilderFactory.PROFILE_ID, profileEntry.getProfileId());
        profileEntryMap.put(SProfileEntryBuilderFactory.INDEX, profileEntry.getIndex());
        profileEntryMap.put(SProfileEntryBuilderFactory.NAME, profileEntry.getName());
        profileEntryMap.put(SProfileEntryBuilderFactory.DESCRIPTION, profileEntry.getDescription());
        profileEntryMap.put(SProfileEntryBuilderFactory.TYPE, profileEntry.getType());
        profileEntryMap.put(SProfileEntryBuilderFactory.PAGE, profileEntry.getPage());
        return profileEntryMap;
    }

    public static List<HashMap<String, Serializable>> profileEntriesToMap(final List<SProfileEntry> profileEntries) {
        final List<HashMap<String, Serializable>> profileEntryMaps = new ArrayList<HashMap<String, Serializable>>();
        for (final SProfileEntry profileEntrie : profileEntries) {
            profileEntryMaps.add(profileEntryToMap(profileEntrie));
        }
        return profileEntryMaps;
    }
}
