/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.model.impl;

import org.bonitasoft.engine.persistence.PersistentObjectId;

import com.bonitasoft.engine.business.application.model.SApplicationPage;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationPageImpl extends PersistentObjectId implements SApplicationPage {

    private static final long serialVersionUID = -5213352950815372458L;

    private long applicationId;

    private long pageId;

    private String name;

    public SApplicationPageImpl() {
    }

    public SApplicationPageImpl(final long applicationId, final long pageId, final String name) {
        super();
        this.applicationId = applicationId;
        this.pageId = pageId;
        this.name = name;
    }

    @Override
    public String getDiscriminator() {
        return SApplicationPage.class.getName();
    }

    @Override
    public long getApplicationId() {
        return applicationId;
    }

    @Override
    public long getPageId() {
        return pageId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (applicationId ^ applicationId >>> 32);
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (int) (pageId ^ pageId >>> 32);
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
        final SApplicationPageImpl other = (SApplicationPageImpl) obj;
        if (applicationId != other.applicationId) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (pageId != other.pageId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SApplicationPageImpl [applicationId=" + applicationId + ", pageId=" + pageId + ", name=" + name + ", getId()=" + getId() + ", getTenantId()="
                + getTenantId() + "]";
    }

}
