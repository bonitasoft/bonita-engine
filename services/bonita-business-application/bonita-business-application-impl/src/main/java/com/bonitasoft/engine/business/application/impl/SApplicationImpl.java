/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.impl;

import org.bonitasoft.engine.persistence.PersistentObjectId;

import com.bonitasoft.engine.business.application.SApplication;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationImpl extends PersistentObjectId implements SApplication {

    private static final long serialVersionUID = 4993767054990446857L;

    private String name;

    private String description;

    private String version;

    private String path;

    public SApplicationImpl() {
        super();
    }

    public SApplicationImpl(final String name, final String version, final String path) {
        super();
        this.name = name;
        this.version = version;
        this.path = path;
    }

    @Override
    public String getDiscriminator() {
        return SApplication.class.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (path == null ? 0 : path.hashCode());
        result = prime * result + (version == null ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SApplicationImpl other = (SApplicationImpl) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SBusinessApplicationImpl [name=" + name + ", description=" + description + ", version=" + version + ", path=" + path + ", id=" + getId()
                + ", tenantId=" + getTenantId() + "]";
    }

}
