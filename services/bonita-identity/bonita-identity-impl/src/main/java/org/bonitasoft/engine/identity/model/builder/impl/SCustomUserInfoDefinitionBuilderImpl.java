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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionBuilder;
import org.bonitasoft.engine.identity.model.impl.SCustomUserInfoDefinitionImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SCustomUserInfoDefinitionBuilderImpl implements SCustomUserInfoDefinitionBuilder {

    private final SCustomUserInfoDefinitionImpl entity;
    
    public SCustomUserInfoDefinitionBuilderImpl(final SCustomUserInfoDefinitionImpl entity) {
        super();
        this.entity = entity;
    }

    @Override
    public SCustomUserInfoDefinitionBuilder setName(final String name) {
        entity.setName(name);
        return this;
    }

    @Override
    public SCustomUserInfoDefinitionBuilder setDescription(final String description) {
        entity.setDescription(description);
        return this;
    }

    @Override
    public SCustomUserInfoDefinitionBuilder setId(final long id) {
        entity.setId(id);
        return this;
    }

    @Override
    public SCustomUserInfoDefinition done() {
        return entity;
    }

}
