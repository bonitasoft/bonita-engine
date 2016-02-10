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
package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;

/**
 * @author Vincent Elcrin
 */
public class DummySCustomUserInfoValue implements SCustomUserInfoValue {

    private static final long serialVersionUID = 1L;

    public static final String MESSAGE = "This is a dummy object!";

    private long id = 1L;

    private long userId = -1L;

    private final long definitionId;

    private String value = "";

    public DummySCustomUserInfoValue(long id) {
        this(id, 1L, 1L, "");
    }

    public DummySCustomUserInfoValue(long id, long definitionId, long userId, String value) {
        this.id = id;
        this.definitionId = definitionId;
        this.userId = userId;
        this.value = value;
    }

    @Override
    public long getDefinitionId() {
        return definitionId;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getDiscriminator() {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public void setId(long id) {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public void setTenantId(long id) {
        throw new UnsupportedOperationException(MESSAGE);
    }
}
