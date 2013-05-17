/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
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
 */
public class TenantCreator {

    public enum TenantField {
        NAME, DESCRIPTION, ICON_NAME, ICON_PATH, DEFAULT_TENANT, USERNAME, PASSWORD;
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

    // public TenantCreator setCreatedBy(final String createdBy) {
    // fields.put(TenantField.CREATED_BY, createdBy);
    // return this;
    // }

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
