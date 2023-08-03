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
import org.bonitasoft.engine.business.application.ApplicationMenu;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationMenuImpl extends BaseElementImpl implements ApplicationMenu {

    private static final long serialVersionUID = 5080525289831930498L;
    private final String displayName;
    private final Long applicationPageId;
    private final long applicationId;
    private Long parentId;
    private final int index;

    public ApplicationMenuImpl(final String displayName, long applicationId, final Long applicationPageId,
            final int index) {
        this.displayName = displayName;
        this.applicationId = applicationId;
        this.applicationPageId = applicationPageId;
        this.index = index;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Long getApplicationPageId() {
        return applicationPageId;
    }

    @Override
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(final Long parentId) {
        this.parentId = parentId;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public long getApplicationId() {
        return applicationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ApplicationMenuImpl that = (ApplicationMenuImpl) o;
        return getApplicationId() == that.getApplicationId() && getIndex() == that.getIndex()
                && Objects.equals(getDisplayName(), that.getDisplayName())
                && Objects.equals(getApplicationPageId(), that.getApplicationPageId())
                && Objects.equals(getParentId(), that.getParentId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getDisplayName(), getApplicationPageId(), getApplicationId(),
                getParentId(), getIndex());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApplicationMenuImpl.class.getSimpleName() + "[", "]")
                .add("displayName='" + displayName + "'")
                .add("applicationPageId=" + applicationPageId)
                .add("applicationId=" + applicationId)
                .add("parentId=" + parentId)
                .add("index=" + index)
                .toString();
    }

}
