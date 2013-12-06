/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.SProfileMetadataDefinition;
import org.bonitasoft.engine.identity.model.builder.SProfileMetadataDefinitionBuilder;
import org.bonitasoft.engine.identity.model.impl.SProfileMetadataDefinitionImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SProfileMetadataDefinitionBuilderImpl implements SProfileMetadataDefinitionBuilder {

    private final SProfileMetadataDefinitionImpl entity;
    
    public SProfileMetadataDefinitionBuilderImpl(final SProfileMetadataDefinitionImpl entity) {
        super();
        this.entity = entity;
    }

    @Override
    public SProfileMetadataDefinitionBuilder setName(final String name) {
        entity.setName(name);
        return this;
    }

    @Override
    public SProfileMetadataDefinitionBuilder setDisplayName(final String displayName) {
        entity.setDisplayName(displayName);
        return this;
    }

    @Override
    public SProfileMetadataDefinitionBuilder setDescription(final String description) {
        entity.setDescription(description);
        return this;
    }

    @Override
    public SProfileMetadataDefinitionBuilder setId(final long id) {
        entity.setId(id);
        return this;
    }

    @Override
    public SProfileMetadataDefinition done() {
        return entity;
    }

}
