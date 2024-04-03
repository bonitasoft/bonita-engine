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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;

/**
 * @author Elias Ricken de Medeiros
 */
public class CustomUserInfoValueBuilder
        extends PersistentObjectBuilder<SCustomUserInfoValue, CustomUserInfoValueBuilder> {

    private long infoDefId;
    private long userId;
    private String value;

    public static CustomUserInfoValueBuilder aCustomUserInfoValue() {
        return new CustomUserInfoValueBuilder();
    }

    @Override
    CustomUserInfoValueBuilder getThisBuilder() {
        return this;
    }

    @Override
    SCustomUserInfoValue _build() {
        SCustomUserInfoValue infoValueImpl = new SCustomUserInfoValue();
        infoValueImpl.setUserId(userId);
        infoValueImpl.setDefinitionId(infoDefId);
        infoValueImpl.setValue(value);
        return infoValueImpl;
    }

    public CustomUserInfoValueBuilder withCustomUserInfoDefinitionId(long infoDefId) {
        this.infoDefId = infoDefId;
        return this;
    }

    public CustomUserInfoValueBuilder withUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public CustomUserInfoValueBuilder withValue(String value) {
        this.value = value;
        return this;
    }

}
