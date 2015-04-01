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
package org.bonitasoft.engine.core.form.impl;

import java.util.Objects;

import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Baptiste Mesta
 */
public class SFormMappingImpl extends PersistentObjectId implements SFormMapping {

    private long processDefinitionId;
    private String task;
    private String pageMappingKey;
    private Integer type = null;
    private long lastUpdateDate;
    private long lastUpdatedBy;

    public SFormMappingImpl() {
    }

    public SFormMappingImpl(long processDefinitionId, Integer type, String task, String pageMappingKey) {
        this.processDefinitionId = processDefinitionId;
        this.task = task;
        this.pageMappingKey = pageMappingKey;
        this.type = type;
    }

    @Override
    public String getDiscriminator() {
        return getClass().getName();
    }

    public String getPageMappingKey() {
        return pageMappingKey;
    }

    public void setPageMappingKey(String pageMappingKey) {
        this.pageMappingKey = pageMappingKey;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Override
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SFormMappingImpl))
            return false;
        if (!super.equals(o))
            return false;
        SFormMappingImpl that = (SFormMappingImpl) o;
        return Objects.equals(processDefinitionId, that.processDefinitionId) &&
                Objects.equals(lastUpdateDate, that.lastUpdateDate) &&
                Objects.equals(lastUpdatedBy, that.lastUpdatedBy) &&
                Objects.equals(task, that.task) &&
                Objects.equals(pageMappingKey, that.pageMappingKey) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), processDefinitionId, task, pageMappingKey, type, lastUpdateDate, lastUpdatedBy);
    }

    @Override
    public String toString() {
        return "SFormMappingImpl(" +
                "processDefinitionId=" + processDefinitionId +
                ", task='" + task + '\'' +
                ", pageMappingKey='" + pageMappingKey + '\'' +
                ", type='" + type + '\'' +
                ", lastUpdateDate=" + lastUpdateDate +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ") " + super.toString();
    }
}
