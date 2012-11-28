/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.platform;

import java.util.Date;

/**
 * @author Elias Ricken de Medeiros
 */
public class TenantImpl implements Tenant {

    private static final long serialVersionUID = 1714893701989215808L;

    private long tenantId;

    private String name;

    private String description;

    private String iconName;

    private String iconPath;

    private String state;

    private Date creationDate;

    private boolean defaultTenant;

    public TenantImpl() {
        super();
    }

    public TenantImpl(final String name, final String description, final String iconName, final String iconPath, final String state, final Date creationDate,
            final boolean defaultTenant) {
        super();
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.iconPath = iconPath;
        this.state = state;
        this.creationDate = creationDate;
        this.defaultTenant = defaultTenant;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getIconName() {
        return iconName;
    }

    @Override
    public String getIconPath() {
        return iconPath;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public long getId() {
        return tenantId;
    }

    @Override
    public boolean isDefaultTenant() {
        return defaultTenant;
    }

    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setIconName(final String iconName) {
        this.iconName = iconName;
    }

    public void setIconPath(final String iconPath) {
        this.iconPath = iconPath;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setDefaultTenant(final boolean defaultTenant) {
        this.defaultTenant = defaultTenant;
    }

}
