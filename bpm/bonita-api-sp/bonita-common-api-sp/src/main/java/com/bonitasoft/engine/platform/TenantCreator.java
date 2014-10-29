/*******************************************************************************
 * Copyright (C) 2013-2014 Bonitasoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.platform;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Creation descriptor for <code>Tenant</code>s.
 * If not set, defaultTenant property is pre-set to false.
 * 
 * @author Emmanuel Duchastenier
 * @author Aurelien Pupier - This file need to be Serializable
 * @see com.bonitasoft.engine.platform.Tenant
 * @since 6.0.0
 */
public class TenantCreator implements Serializable {

    private static final long serialVersionUID = -6904933424057070693L;

    public enum TenantField {
        NAME, DESCRIPTION, ICON_NAME, ICON_PATH, DEFAULT_TENANT,

        /**
         * Username for the tenant technical user
         */
        USERNAME,

        /**
         * Password for the tenant technical user
         */
        PASSWORD;
    }

    private final Map<TenantField, Serializable> fields;

    public TenantCreator(final String tenantName, final String description, final String iconName, final String iconPath, final String username,
            final String password) {
        this(tenantName);
        setDescription(description);
        setIconName(iconName);
        setIconPath(iconPath);
        setUsername(username);
        setPassword(password);
    }

    public TenantCreator(final String name) {
        fields = new HashMap<TenantField, Serializable>(TenantField.values().length);
        fields.put(TenantField.NAME, name);
        fields.put(TenantField.DEFAULT_TENANT, false);
    }

    public TenantCreator setDescription(final String description) {
        fields.put(TenantField.DESCRIPTION, description);
        return this;
    }

    public TenantCreator setIconName(final String iconName) {
        fields.put(TenantField.ICON_NAME, iconName);
        return this;
    }

    public TenantCreator setIconPath(final String iconPath) {
        fields.put(TenantField.ICON_PATH, iconPath);
        return this;
    }

    public TenantCreator setDefaultTenant(final boolean defaultTenant) {
        fields.put(TenantField.DEFAULT_TENANT, defaultTenant);
        return this;
    }

    public TenantCreator setUsername(final String username) {
        fields.put(TenantField.USERNAME, username);
        return this;
    }

    public TenantCreator setPassword(final String password) {
        fields.put(TenantField.PASSWORD, password);
        return this;
    }

    public Map<TenantField, Serializable> getFields() {
        return Collections.unmodifiableMap(fields);
    }

}
