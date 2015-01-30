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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.business.application.model.impl.SApplicationMenuImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationMenuBuilder extends PersistentObjectBuilder<SApplicationMenuImpl, ApplicationMenuBuilder> {

    public static ApplicationMenuBuilder anApplicationMenu() {
        return new ApplicationMenuBuilder();
    }

    private String displayName;
    private long applicationId;
    private Long applicationPageId;
    private int index;
    private Long parentId;

    @Override
    SApplicationMenuImpl _build() {
        final SApplicationMenuImpl menu = new SApplicationMenuImpl(displayName, applicationId, applicationPageId, index);
        menu.setParentId(parentId);
        return menu;
    }

    public ApplicationMenuBuilder withDisplayName(final String displayName) {
        this.displayName = displayName;
        return this;
    }

    public ApplicationMenuBuilder withApplicationId(final Long applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public ApplicationMenuBuilder withApplicationPageId(final Long applicationPageId) {
        this.applicationPageId = applicationPageId;
        return this;
    }

    public ApplicationMenuBuilder withIndex(final int index) {
        this.index = index;
        return this;
    }

    public ApplicationMenuBuilder withParentId(final long parentId) {
        this.parentId = parentId;
        return this;
    }

    @Override
    ApplicationMenuBuilder getThisBuilder() {
        return this;
    }
}
