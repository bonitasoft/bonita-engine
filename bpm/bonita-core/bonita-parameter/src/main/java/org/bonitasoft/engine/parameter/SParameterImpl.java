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
package org.bonitasoft.engine.parameter;

import java.util.Objects;

/**
 * @author Matthieu Chaffotte
 */
public class SParameterImpl implements SParameter {

    private String name;
    private String value;
    private long processDefinitionId;
    private long id;
    private long tenantId;

    public SParameterImpl() {
    }

    public SParameterImpl(String name, String value, long processDefinitionId) {
        this.name = name;
        this.value = value;
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public String getDiscriminator() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SParameterImpl that = (SParameterImpl) o;
        return Objects.equals(processDefinitionId, that.processDefinitionId) &&
                Objects.equals(id, that.id) &&
                Objects.equals(tenantId, that.tenantId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, processDefinitionId, id, tenantId);
    }
}
