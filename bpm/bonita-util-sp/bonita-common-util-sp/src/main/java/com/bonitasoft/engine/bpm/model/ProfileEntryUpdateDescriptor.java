/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Celine Souchet
 */
public class ProfileEntryUpdateDescriptor implements Serializable {

    private static final long serialVersionUID = 728214104237982027L;

    public enum ProfileEntryField {
        NAME, DESCRIPTION, PARENT_ID, PROFILE_ID, INDEX, TYPE, PAGE;
    }

    private final Map<ProfileEntryField, Serializable> fields;

    public ProfileEntryUpdateDescriptor() {
        fields = new HashMap<ProfileEntryField, Serializable>(3);
    }

    public void name(final String name) {
        fields.put(ProfileEntryField.NAME, name);
    }

    public void description(final String description) {
        fields.put(ProfileEntryField.DESCRIPTION, description);
    }

    public void parentId(final long parentId) {
        fields.put(ProfileEntryField.PARENT_ID, parentId);
    }

    public void profileId(final long profileId) {
        fields.put(ProfileEntryField.PROFILE_ID, profileId);
    }

    public void index(final long index) {
        fields.put(ProfileEntryField.INDEX, index);
    }

    public void type(final String type) {
        fields.put(ProfileEntryField.TYPE, type);
    }

    public void page(final String page) {
        fields.put(ProfileEntryField.PAGE, page);
    }

    public Map<ProfileEntryField, Serializable> getFields() {
        return fields;
    }

}
