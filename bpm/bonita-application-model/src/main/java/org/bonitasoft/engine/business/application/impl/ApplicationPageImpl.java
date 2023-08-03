/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.Objects;
import java.util.StringJoiner;

import org.bonitasoft.engine.bpm.internal.BaseElementImpl;
import org.bonitasoft.engine.business.application.ApplicationPage;

/**
 * @author Elias Ricken de Medeiros
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
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ApplicationPageImpl that = (ApplicationPageImpl) o;
        return getApplicationId() == that.getApplicationId() && getPageId() == that.getPageId()
                && Objects.equals(getToken(), that.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getApplicationId(), getPageId(), getToken());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApplicationPageImpl.class.getSimpleName() + "[", "]")
                .add("applicationId=" + applicationId)
                .add("pageId=" + pageId)
                .add("token='" + token + "'")
                .add("id=" + getId())
                .toString();
    }

}
