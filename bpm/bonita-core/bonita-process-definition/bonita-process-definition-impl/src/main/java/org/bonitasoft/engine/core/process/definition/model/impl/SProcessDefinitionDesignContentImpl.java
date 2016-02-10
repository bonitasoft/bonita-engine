/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.core.process.definition.model.impl;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDesignContent;

/**
 * @author Baptiste Mesta
 */
public class SProcessDefinitionDesignContentImpl implements SProcessDefinitionDesignContent {

    private long tenantId;
    private long id;
    private String content;

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getDiscriminator() {
        return SProcessDefinitionDesignContent.class.getName();
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
