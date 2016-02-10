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

import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoValueBuilder;
import org.bonitasoft.engine.identity.model.impl.SCustomUserInfoValueImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SCustomUserInfoValueBuilderImpl implements SCustomUserInfoValueBuilder {

    private final SCustomUserInfoValueImpl entity;
    
    public SCustomUserInfoValueBuilderImpl(final SCustomUserInfoValueImpl entity) {
        super();
        this.entity = entity;
    }

    public SCustomUserInfoValueBuilderImpl setId(final long id) {
        entity.setId(id);
        return this;
    }

    @Override
    public SCustomUserInfoValue done() {
        return entity;
    }

    @Override
    public SCustomUserInfoValueBuilder setDefinitionId(final long definitionId) {
        entity.setDefinitionId(definitionId);
        return this;
    }

    @Override
    public SCustomUserInfoValueBuilder setUserId(final long userId) {
        entity.setUserId(userId);
        return this;
    }

    @Override
    public SCustomUserInfoValueBuilder setValue(final String value) {
        entity.setValue(value);
        return this;
    }

}
