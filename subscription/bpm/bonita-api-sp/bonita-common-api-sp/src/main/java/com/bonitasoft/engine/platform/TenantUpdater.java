/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.platform;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Updater for tenants, it is used by {@link com.bonitasoft.engine.api.PlatformAPI#updateTenant(long, TenantUpdater)}
 *
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @since 6.0.0
 */
public class TenantUpdater implements Serializable {

    private static final long serialVersionUID = -3608167879596202047L;

    public enum TenantField {
        NAME, DESCRIPTION, ICON_NAME, ICON_PATH, STATUS, USERNAME, PASSWOWRD;
    }

    private final Map<TenantField, Serializable> fields;

    public TenantUpdater() {
        fields = new HashMap<TenantField, Serializable>(TenantField.values().length);
    }

    public void setName(final String name) {
        fields.put(TenantField.NAME, name);
    }

    public void setDescription(final String description) {
        fields.put(TenantField.DESCRIPTION, description);
    }

    public void setIconName(final String iconName) {
        fields.put(TenantField.ICON_NAME, iconName);
    }

    public void setIconPath(final String iconPath) {
        fields.put(TenantField.ICON_PATH, iconPath);
    }

    public void setStatus(final String status) {
        fields.put(TenantField.STATUS, status);
    }

    public void setUsername(final String username) {
        fields.put(TenantField.USERNAME, username);
    }

    public void setPassword(final String password) {
        fields.put(TenantField.PASSWOWRD, password);
    }

    public Map<TenantField, Serializable> getFields() {
        return fields;
    }

}
