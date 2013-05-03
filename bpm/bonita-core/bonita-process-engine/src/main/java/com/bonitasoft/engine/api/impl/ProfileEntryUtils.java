package com.bonitasoft.engine.api.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.profile.builder.SProfileEntryBuilder;
import org.bonitasoft.engine.profile.model.SProfileEntry;

public class ProfileEntryUtils {

    public static Map<String, Serializable> profileEntryToMap(final SProfileEntry profileEntry) {
        final Map<String, Serializable> profileEntryMap = new HashMap<String, Serializable>();
        profileEntryMap.put(SProfileEntryBuilder.ID, profileEntry.getId());
        profileEntryMap.put(SProfileEntryBuilder.PARENT_ID, profileEntry.getParentId());
        profileEntryMap.put(SProfileEntryBuilder.PROFILE_ID, profileEntry.getProfileId());
        profileEntryMap.put(SProfileEntryBuilder.INDEX, profileEntry.getIndex());
        profileEntryMap.put(SProfileEntryBuilder.NAME, profileEntry.getName());
        profileEntryMap.put(SProfileEntryBuilder.DESCRIPTION, profileEntry.getDescription());
        profileEntryMap.put(SProfileEntryBuilder.TYPE, profileEntry.getType());
        profileEntryMap.put(SProfileEntryBuilder.PAGE, profileEntry.getPage());
        return profileEntryMap;
    }

    public static ArrayList<Map<String, Serializable>> profileEntriesToMap(final List<SProfileEntry> profileEntries) {
        final ArrayList<Map<String, Serializable>> profileEntryMaps = new ArrayList<Map<String, Serializable>>();
        for (final SProfileEntry profileEntrie : profileEntries) {
            profileEntryMaps.add(profileEntryToMap(profileEntrie));
        }
        return profileEntryMaps;
    }
}
