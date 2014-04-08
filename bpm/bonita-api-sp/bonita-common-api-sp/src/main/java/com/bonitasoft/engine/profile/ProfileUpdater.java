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

/**
 * @author Celine Souchet
 */
public class ProfileUpdater implements Serializable {

    private static final long serialVersionUID = 728214104237982027L;

    public enum ProfileField {
        NAME, DESCRIPTION, ICON_PATH, PAGE, CUSTOM;
    }

    private final Map<ProfileField, Serializable> fields;

    public ProfileUpdater() {
        fields = new HashMap<ProfileField, Serializable>(3);
    }

    public void name(final String name) {
        fields.put(ProfileField.NAME, name);
    }

    public void description(final String description) {
        fields.put(ProfileField.DESCRIPTION, description);
    }

    public void iconPath(final String iconPath) {
        fields.put(ProfileField.ICON_PATH, iconPath);
    }

    public Map<ProfileField, Serializable> getFields() {
        return fields;
    }

}
