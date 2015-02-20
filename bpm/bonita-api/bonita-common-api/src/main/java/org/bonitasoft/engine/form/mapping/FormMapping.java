/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.engine.form.mapping;

import java.io.Serializable;

/**
 * @author Baptiste Mesta
 */
public class FormMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    private long processDefinitionId;
    private String task;
    private String page;
    private boolean external;
    private FormMappingType type;

    public FormMapping(long processDefinitionId, boolean external, FormMappingType type, String page, String task) {
        this.processDefinitionId = processDefinitionId;
        this.task = task;
        this.page = page;
        this.external = external;
        this.type = type;
    }

    public FormMapping(long processDefinitionId, FormMappingType type, boolean external, String page) {
        this.type = type;
        this.external = external;
        this.processDefinitionId = processDefinitionId;
        this.page = page;
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

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
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

    @Override
    public String toString() {
        return "FormMapping{" +
                "processDefinitionId=" + processDefinitionId +
                ", task='" + task + '\'' +
                ", page='" + page + '\'' +
                ", external=" + external +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FormMapping)) return false;

        FormMapping that = (FormMapping) o;

        if (external != that.external) return false;
        if (processDefinitionId != that.processDefinitionId) return false;
        if (page != null ? !page.equals(that.page) : that.page != null) return false;
        if (task != null ? !task.equals(that.task) : that.task != null) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (processDefinitionId ^ (processDefinitionId >>> 32));
        result = 31 * result + (task != null ? task.hashCode() : 0);
        result = 31 * result + (page != null ? page.hashCode() : 0);
        result = 31 * result + (external ? 1 : 0);
        result = 31 * result + type.hashCode();
        return result;
    }
}
