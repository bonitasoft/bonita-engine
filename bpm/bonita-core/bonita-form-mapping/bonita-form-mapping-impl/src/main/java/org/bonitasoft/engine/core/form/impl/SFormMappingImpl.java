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

import org.bonitasoft.engine.core.form.SFormMapping;

/**
 * @author Baptiste Mesta
 */
public class SFormMappingImpl implements SFormMapping {

    private long processDefinitionId;
    private String task;
    private String form;
    private String target;
    private String type;
    private long id;
    private long tenantId;
    private long lastUpdateDate;
    private long lastUpdatedBy;

    public SFormMappingImpl() {
    }

    public SFormMappingImpl(long processDefinitionId, String task, String form, String target, String type) {
        this.processDefinitionId = processDefinitionId;
        this.task = task;
        this.form = form;
        this.target = target;
        this.type = type;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getDiscriminator() {
        return getClass().getName();
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public long getTenantId() {
        return tenantId;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
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

        SFormMappingImpl that = (SFormMappingImpl) o;

        if (id != that.id)
            return false;
        if (lastUpdateDate != that.lastUpdateDate)
            return false;
        if (lastUpdatedBy != that.lastUpdatedBy)
            return false;
        if (processDefinitionId != that.processDefinitionId)
            return false;
        if (tenantId != that.tenantId)
            return false;
        if (form != null ? !form.equals(that.form) : that.form != null)
            return false;
        if (task != null ? !task.equals(that.task) : that.task != null)
            return false;
        if (type != null ? !type.equals(that.type) : that.type != null)
            return false;
        if (target != null ? !target.equals(that.target) : that.target != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (processDefinitionId ^ (processDefinitionId >>> 32));
        result = 31 * result + (task != null ? task.hashCode() : 0);
        result = 31 * result + (form != null ? form.hashCode() : 0);
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (tenantId ^ (tenantId >>> 32));
        result = 31 * result + (int) (lastUpdateDate ^ (lastUpdateDate >>> 32));
        result = 31 * result + (int) (lastUpdatedBy ^ (lastUpdatedBy >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "SFormMappingImpl{" +
                "processDefinitionId=" + processDefinitionId +
                ", task='" + task + '\'' +
                ", form='" + form + '\'' +
                ", target='" + target + '\'' +
                ", type='" + type + '\'' +
                ", id=" + id +
                ", tenantId=" + tenantId +
                ", lastUpdateDate=" + lastUpdateDate +
                ", lastUpdatedBy=" + lastUpdatedBy +
                '}';
    }
}
