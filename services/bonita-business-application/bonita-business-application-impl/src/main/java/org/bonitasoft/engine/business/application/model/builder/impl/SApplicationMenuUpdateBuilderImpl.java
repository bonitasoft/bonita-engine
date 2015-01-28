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
package org.bonitasoft.engine.business.application.model.builder.impl;

import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros
 */
public class SApplicationMenuUpdateBuilderImpl implements SApplicationMenuUpdateBuilder {

    private EntityUpdateDescriptor descriptor;

    public SApplicationMenuUpdateBuilderImpl() {
        descriptor = new EntityUpdateDescriptor();
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SApplicationMenuUpdateBuilder updateDisplayName(String displayName) {
        descriptor.addField(SApplicationMenuFields.DISPLAY_NAME, displayName);
        return this;
    }

    @Override
    public SApplicationMenuUpdateBuilder updateApplicationPageId(Long applicationPageId) {
        descriptor.addField(SApplicationMenuFields.APPLICATION_PAGE_ID, applicationPageId);
        return this;
    }

    @Override
    public SApplicationMenuUpdateBuilder updateIndex(int index) {
        descriptor.addField(SApplicationMenuFields.INDEX, index);
        return this;
    }

    @Override
    public SApplicationMenuUpdateBuilder updateParentId(Long parentId) {
        descriptor.addField(SApplicationMenuFields.PARENT_ID, parentId);
        return this;
    }
}
