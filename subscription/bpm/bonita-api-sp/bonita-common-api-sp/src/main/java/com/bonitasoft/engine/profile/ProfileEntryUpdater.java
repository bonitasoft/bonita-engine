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
public class ProfileEntryUpdater implements Serializable {

    private static final long serialVersionUID = 728214104237982027L;

    public enum ProfileEntryUpdateField {
        NAME, DESCRIPTION, PARENT_ID, PROFILE_ID, INDEX, TYPE, PAGE, CUSTOM;
    }

    private final Map<ProfileEntryUpdateField, Serializable> fields;

    public ProfileEntryUpdater() {
        fields = new HashMap<ProfileEntryUpdateField, Serializable>(3);
    }

    public void name(final String name) {
        fields.put(ProfileEntryUpdateField.NAME, name);
    }

    public void description(final String description) {
        fields.put(ProfileEntryUpdateField.DESCRIPTION, description);
    }

    public void parentId(final long parentId) {
        fields.put(ProfileEntryUpdateField.PARENT_ID, parentId);
    }

    public void profileId(final long profileId) {
        fields.put(ProfileEntryUpdateField.PROFILE_ID, profileId);
    }

    public void index(final long index) {
        fields.put(ProfileEntryUpdateField.INDEX, index);
    }

    public void type(final String type) {
        fields.put(ProfileEntryUpdateField.TYPE, type);
    }

    public void page(final String page) {
        fields.put(ProfileEntryUpdateField.PAGE, page);
    }

    public void custom(final Boolean custom) {
        fields.put(ProfileEntryUpdateField.CUSTOM, custom);
    }

    public Map<ProfileEntryUpdateField, Serializable> getFields() {
        return fields;
    }

}
