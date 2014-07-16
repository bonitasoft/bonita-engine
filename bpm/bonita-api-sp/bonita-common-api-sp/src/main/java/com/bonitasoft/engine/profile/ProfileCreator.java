/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.profile.Profile;

/**
 * @author Celine Souchet
 */
public class ProfileCreator implements Serializable {

    private static final long serialVersionUID = -1414989152963184543L;

    public enum ProfileField {
        NAME, DESCRIPTION;
    }

    private final Map<ProfileField, Serializable> fields;

    public ProfileCreator(final String name) {
        fields = new HashMap<ProfileField, Serializable>(3);
        fields.put(ProfileField.NAME, name);
    }

    public ProfileCreator(final Profile profile) {
        fields = new HashMap<ProfileField, Serializable>(3);
        fields.put(ProfileField.DESCRIPTION, profile.getDescription());
    }

    public ProfileCreator setName(final String name) {
        fields.put(ProfileField.NAME, name);
        return this;
    }

    public ProfileCreator setDescription(final String description) {
        fields.put(ProfileField.DESCRIPTION, description);
        return this;
    }

    public Map<ProfileField, Serializable> getFields() {
        return fields;
    }

}
