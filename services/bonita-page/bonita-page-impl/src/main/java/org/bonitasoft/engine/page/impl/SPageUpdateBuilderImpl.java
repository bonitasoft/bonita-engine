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
package org.bonitasoft.engine.page.impl;

import org.bonitasoft.engine.page.SPageUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

public class SPageUpdateBuilderImpl implements SPageUpdateBuilder {

    private final EntityUpdateDescriptor descriptor;

    public SPageUpdateBuilderImpl(final EntityUpdateDescriptor descriptor) {
        super();
        this.descriptor = descriptor;
    }

    @Override
    public SPageUpdateBuilder updateName(final String value) {
        descriptor.addField(SPageFields.PAGE_NAME, value);
        return this;
    }

    @Override
    public SPageUpdateBuilder updateDescription(final String value) {
        descriptor.addField(SPageFields.PAGE_DESCRIPTION, value);
        return this;
    }

    @Override
    public SPageUpdateBuilder updateDisplayName(final String value) {
        descriptor.addField(SPageFields.PAGE_DISPLAY_NAME, value);
        return this;
    }

    @Override
    public SPageUpdateBuilder updateLastModificationDate(final long currentTimeMillis) {
        descriptor.addField(SPageFields.PAGE_LAST_MODIFICATION_DATE, currentTimeMillis);
        return this;
    }

    @Override
    public SPageUpdateBuilder updateLastUpdatedBy(final long userId) {
        descriptor.addField(SPageFields.PAGE_LAST_UPDATED_BY, userId);
        return this;
    }

    @Override
    public SPageUpdateBuilder updateContentName(final String value) {
        descriptor.addField(SPageFields.PAGE_CONTENT_NAME, value);
        return this;
    }

    @Override
    public SPageUpdateBuilder updateContentType(String contentType) {
        descriptor.addField(SPageFields.PAGE_CONTENT_TYPE, contentType);
        return this;
    }

    @Override
    public SPageUpdateBuilder updateProcessDefinitionId(Long processDefinitionId) {
        descriptor.addField(SPageFields.PAGE_PROCESS_DEFINITION_ID, processDefinitionId);
        return this;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

}
