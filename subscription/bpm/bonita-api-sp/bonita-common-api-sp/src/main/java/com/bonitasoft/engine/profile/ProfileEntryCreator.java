/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.profile;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Celine Souchet
 */
public class ProfileEntryCreator implements Serializable {

    private static final long serialVersionUID = -1414989152963184543L;

    public enum ProfileEntryField {
        NAME, PROFILE_ID, DESCRIPTION, PARENT_ID, TYPE, PAGE, INDEX, CUSTOM;
    }

    private final Map<ProfileEntryField, Serializable> fields;

    public ProfileEntryCreator(final String name, final long profileId) {
        fields = new HashMap<ProfileEntryField, Serializable>(5);
        fields.put(ProfileEntryField.NAME, name);
        fields.put(ProfileEntryField.PROFILE_ID, profileId);
    }

    public ProfileEntryCreator(final long profileId) {
        fields = new HashMap<ProfileEntryField, Serializable>(5);
        fields.put(ProfileEntryField.PROFILE_ID, profileId);
    }

    public ProfileEntryCreator setName(final String name) {
        fields.put(ProfileEntryField.NAME, name);
        return this;
    }

    public ProfileEntryCreator setDescription(final String description) {
        fields.put(ProfileEntryField.DESCRIPTION, description);
        return this;
    }

    public ProfileEntryCreator setParentId(final long parentId) {
        fields.put(ProfileEntryField.PARENT_ID, parentId);
        return this;
    }

    public ProfileEntryCreator setType(final String type) {
        fields.put(ProfileEntryField.TYPE, type);
        return this;
    }

    public ProfileEntryCreator setPage(final String page) {
        fields.put(ProfileEntryField.PAGE, page);
        return this;
    }

    public ProfileEntryCreator setIndex(final long index) {
        fields.put(ProfileEntryField.INDEX, index);
        return this;
    }

    public ProfileEntryCreator setCustom(final Boolean custom) {
        fields.put(ProfileEntryField.CUSTOM, custom);
        return this;
    }

    public Map<ProfileEntryField, Serializable> getFields() {
        return fields;
    }

}
