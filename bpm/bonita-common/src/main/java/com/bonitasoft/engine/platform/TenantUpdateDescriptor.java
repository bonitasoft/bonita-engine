/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.platform;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matthieu Chaffotte
 */
public class TenantUpdateDescriptor implements Serializable {

    private static final long serialVersionUID = 728214104237982027L;

    public enum TenantField {
        NAME, DESCRIPTION, ICON_NAME, ICON_PATH, STATUS, USERNAME, PASSWOWRD;
    }

    private final Map<TenantField, Serializable> fields;

    public TenantUpdateDescriptor() {
        fields = new HashMap<TenantField, Serializable>();
    }

    public void updateName(final String name) {
        fields.put(TenantField.NAME, name);
    }

    public void updateDescription(final String description) {
        fields.put(TenantField.DESCRIPTION, description);
    }

    public void updateIconName(final String iconName) {
        fields.put(TenantField.ICON_NAME, iconName);
    }

    public void updateIconPath(final String iconPath) {
        fields.put(TenantField.ICON_PATH, iconPath);
    }

    public void updateStatus(final String status) {
        fields.put(TenantField.STATUS, status);
    }

    public void updateUsername(final String username) {
        fields.put(TenantField.USERNAME, username);
    }

    public void updatePassword(final String password) {
        fields.put(TenantField.PASSWOWRD, password);
    }

    public Map<TenantField, Serializable> getFields() {
        return fields;
    }

}
