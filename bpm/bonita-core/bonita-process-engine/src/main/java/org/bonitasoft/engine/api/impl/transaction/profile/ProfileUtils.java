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

import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.builder.SProfileBuilderFactory;
import org.bonitasoft.engine.profile.model.SProfile;

public class ProfileUtils {

    public static HashMap<String, Serializable> profileToMap(final Profile profile) {
        final HashMap<String, Serializable> profielMap = new HashMap<String, Serializable>(4);
        profielMap.put(SProfileBuilderFactory.ID, profile.getId());
        profielMap.put(SProfileBuilderFactory.NAME, profile.getName());
        profielMap.put(SProfileBuilderFactory.DESCRIPTION, profile.getDescription());
        profielMap.put(SProfileBuilderFactory.ICON_PATH, profile.getIconPath());
        return profielMap;
    }

    public static HashMap<String, Serializable> sProfileToMap(final SProfile profile) {
        final HashMap<String, Serializable> profielMap = new HashMap<String, Serializable>(4);
        profielMap.put(SProfileBuilderFactory.ID, profile.getId());
        profielMap.put(SProfileBuilderFactory.NAME, profile.getName());
        profielMap.put(SProfileBuilderFactory.DESCRIPTION, profile.getDescription());
        profielMap.put(SProfileBuilderFactory.ICON_PATH, profile.getIconPath());
        return profielMap;
    }

    // Need to define concrete ArrayList here (instead of List) because it needs to be Serializable:
    public static List<HashMap<String, Serializable>> profilesToMap(final List<Profile> profiles) {
        final ArrayList<HashMap<String, Serializable>> profileMaps = new ArrayList<HashMap<String, Serializable>>(profiles.size());
        for (final Profile profile : profiles) {
            profileMaps.add(profileToMap(profile));
        }
        return profileMaps;
    }

    public static List<HashMap<String, Serializable>> sProfilesToMap(final List<SProfile> profiles) {
        final ArrayList<HashMap<String, Serializable>> profileMaps = new ArrayList<HashMap<String, Serializable>>(profiles.size());
        for (final SProfile profile : profiles) {
            profileMaps.add(sProfileToMap(profile));
        }
        return profileMaps;
    }

}
