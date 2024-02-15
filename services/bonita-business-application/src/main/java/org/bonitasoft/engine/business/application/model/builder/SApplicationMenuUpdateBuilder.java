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
package org.bonitasoft.engine.business.application.model.builder;

import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros
 */
public class SApplicationMenuUpdateBuilder {

    protected EntityUpdateDescriptor descriptor;

    public SApplicationMenuUpdateBuilder() {
        descriptor = new EntityUpdateDescriptor();
    }

    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    public SApplicationMenuUpdateBuilder updateDisplayName(String displayName) {
        descriptor.addField(SApplicationMenu.DISPLAY_NAME, displayName);
        return this;
    }

    public SApplicationMenuUpdateBuilder updateApplicationPageId(Long applicationPageId) {
        descriptor.addField(SApplicationMenu.APPLICATION_PAGE_ID, applicationPageId);
        return this;
    }

    public SApplicationMenuUpdateBuilder updateIndex(int index) {
        descriptor.addField(SApplicationMenu.INDEX, index);
        return this;
    }

    public SApplicationMenuUpdateBuilder updateParentId(Long parentId) {
        descriptor.addField(SApplicationMenu.PARENT_ID, parentId);
        return this;
    }

}
