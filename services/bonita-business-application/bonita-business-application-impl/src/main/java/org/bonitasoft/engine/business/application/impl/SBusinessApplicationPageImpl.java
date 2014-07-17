/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.business.application.impl;

import org.bonitasoft.engine.persistence.PersistentObjectId;

import org.bonitasoft.engine.business.application.SBusinessApplicationPage;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SBusinessApplicationPageImpl extends PersistentObjectId implements SBusinessApplicationPage {

    private static final long serialVersionUID = -5213352950815372458L;

    private final long applicationId;

    private final long pageId;

    public SBusinessApplicationPageImpl(final long applicationId, final long pageId) {
        super();
        this.applicationId = applicationId;
        this.pageId = pageId;
    }

    @Override
    public String getDiscriminator() {
        return SBusinessApplicationPage.class.getName();
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
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (applicationId ^ applicationId >>> 32);
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
        final SBusinessApplicationPageImpl other = (SBusinessApplicationPageImpl) obj;
        if (applicationId != other.applicationId) {
            return false;
        }
        if (pageId != other.pageId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SBusinessApplicationPageImpl [applicationId=" + applicationId + ", pageId=" + pageId + ", id=" + getId() + ", tenantId="
                + getTenantId() + "]";
    }

}
