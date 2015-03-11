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
package org.bonitasoft.engine.form;

import java.util.Date;

import org.bonitasoft.engine.bpm.BaseElement;

/**
 * @author Baptiste Mesta
 */
public class FormMapping implements BaseElement {

    private static final long serialVersionUID = 1L;

    private long id;
    private long processDefinitionId;
    private String task;
    private String form;
    private boolean external;
    private FormMappingType type;
    private long lastUpdatedBy;
    private Date lastUpdateDate;

    public FormMapping() {
    }

    public FormMapping(long processDefinitionId, boolean external, FormMappingType type, String form, String task) {
        this.processDefinitionId = processDefinitionId;
        this.task = task;
        this.form = form;
        this.external = external;
        this.type = type;
    }

    public FormMapping(long processDefinitionId, FormMappingType type, boolean external, String form) {
        this.type = type;
        this.external = external;
        this.processDefinitionId = processDefinitionId;
        this.form = form;
    }

    public FormMapping(FormMappingType type, boolean external, long processDefinitionId) {
        this.type = type;
        this.external = external;
        this.processDefinitionId = processDefinitionId;
    }

    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public FormMappingType getType() {
        return type;
    }

    public void setType(FormMappingType type) {
        this.type = type;
    }

    public long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FormMapping that = (FormMapping) o;

        if (external != that.external) return false;
        if (id != that.id) return false;
        if (lastUpdatedBy != that.lastUpdatedBy) return false;
        if (processDefinitionId != that.processDefinitionId) return false;
        if (form != null ? !form.equals(that.form) : that.form != null) return false;
        if (lastUpdateDate != null ? !lastUpdateDate.equals(that.lastUpdateDate) : that.lastUpdateDate != null)
            return false;
        if (task != null ? !task.equals(that.task) : that.task != null) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (processDefinitionId ^ (processDefinitionId >>> 32));
        result = 31 * result + (task != null ? task.hashCode() : 0);
        result = 31 * result + (form != null ? form.hashCode() : 0);
        result = 31 * result + (external ? 1 : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (int) (lastUpdatedBy ^ (lastUpdatedBy >>> 32));
        result = 31 * result + (lastUpdateDate != null ? lastUpdateDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FormMapping{" +
                "id=" + id +
                ", processDefinitionId=" + processDefinitionId +
                ", task='" + task + '\'' +
                ", form='" + form + '\'' +
                ", external=" + external +
                ", type=" + type +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", lastUpdateDate=" + lastUpdateDate +
                '}';
    }
}
