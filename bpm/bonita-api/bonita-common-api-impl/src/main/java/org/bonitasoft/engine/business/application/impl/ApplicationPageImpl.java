/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

import org.bonitasoft.engine.bpm.internal.BaseElementImpl;
import org.bonitasoft.engine.business.application.ApplicationPage;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationPageImpl extends BaseElementImpl implements ApplicationPage {

    private static final long serialVersionUID = -8043272410231723583L;
    private final long applicationId;
    private final long pageId;
    private final String token;

    public ApplicationPageImpl(final long applicationId, final long pageId, final String token) {
        this.applicationId = applicationId;
        this.pageId = pageId;
        this.token = token;
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
    public String getToken() {
        return token;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (applicationId ^ applicationId >>> 32);
        result = prime * result + (int) (pageId ^ pageId >>> 32);
        result = prime * result + (token == null ? 0 : token.hashCode());
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
        final ApplicationPageImpl other = (ApplicationPageImpl) obj;
        if (applicationId != other.applicationId) {
            return false;
        }
        if (pageId != other.pageId) {
            return false;
        }
        if (token == null) {
            if (other.token != null) {
                return false;
            }
        } else if (!token.equals(other.token)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ApplicationPageImpl [applicationId=" + applicationId + ", pageId=" + pageId + ", token=" + token + ", getId()=" + getId() + "]";
    }

}
