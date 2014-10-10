/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class ApplicationUpdater implements Serializable {

    private static final long serialVersionUID = 4565052647320534796L;

    private final Map<ApplicationField, Serializable> fields;

    public ApplicationUpdater() {
        fields = new HashMap<ApplicationField, Serializable>(8);
    }

    public Map<ApplicationField, Serializable> getFields() {
        return fields;
    }

    public ApplicationUpdater setToken(final String token) {
        fields.put(ApplicationField.TOKEN, token);
        return this;
    }

    public ApplicationUpdater setDisplayName(final String displayName) {
        fields.put(ApplicationField.DISPLAY_NAME, displayName);
        return this;
    }

    public ApplicationUpdater setVersion(final String version) {
        fields.put(ApplicationField.VERSION, version);
        return this;
    }

    public ApplicationUpdater setDescription(final String description) {
        fields.put(ApplicationField.DESCRIPTION, description);
        return this;
    }

    public ApplicationUpdater setIconPath(final String iconPath) {
        fields.put(ApplicationField.ICON_PATH, iconPath);
        return this;
    }

    public ApplicationUpdater setState(final String state) {
        fields.put(ApplicationField.STATE, state);
        return this;
    }

    public ApplicationUpdater setProfileId(final Long profileId) {
        fields.put(ApplicationField.PROFILE_ID, profileId);
        return this;
    }

    /**
     * Has this updater at least one field to update
     *
     * @return true if there is at least one field to update
     */
    public boolean hasFields() {
        return !getFields().isEmpty();
    }
}
