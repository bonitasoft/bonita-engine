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
package com.bonitasoft.engine.business.application.model.builder.impl;

import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.business.application.impl.SApplicationFields;
import com.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationUpdateBuilderImpl implements SApplicationUpdateBuilder {

    private final EntityUpdateDescriptor descriptor;

    public SApplicationUpdateBuilderImpl(final EntityUpdateDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SApplicationUpdateBuilder updateHomePageId(final long applicationPageId) {
        descriptor.addField(SApplicationFields.HOME_PAGE_ID, applicationPageId);
        return this;
    }

}
