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
package org.bonitasoft.engine.core.document.model.impl;

import org.bonitasoft.engine.core.document.model.SDocumentMapping;

/**
 * @author Baptiste Mesta
 */
public class SDocumentMappingImpl implements SDocumentMapping {

    private static final long serialVersionUID = 1L;

    private long id;
    private long tenantId;
    private long processInstanceId;
    private long documentId;
    private String name;
    private String description;
    private String version;
    private int index;


    public SDocumentMappingImpl() {
    }

    public SDocumentMappingImpl(long documentId, long processInstanceId, String name) {
        this.documentId = documentId;
        this.processInstanceId = processInstanceId;
        this.name = name;
    }

    @Override
    public String getDiscriminator() {
        return SDocumentMappingImpl.class.getName();
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SDocumentMappingImpl that = (SDocumentMappingImpl) o;

        if (documentId != that.documentId) return false;
        if (id != that.id) return false;
        if (index != that.index) return false;
        if (processInstanceId != that.processInstanceId) return false;
        if (tenantId != that.tenantId) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (tenantId ^ (tenantId >>> 32));
        result = 31 * result + (int) (processInstanceId ^ (processInstanceId >>> 32));
        result = 31 * result + (int) (documentId ^ (documentId >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + index;
        return result;
    }

    @Override
    public String toString() {
        return "SDocumentMappingImpl{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", processInstanceId=" + processInstanceId +
                ", documentId=" + documentId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", version='" + version + '\'' +
                ", index=" + index +
                '}';
    }
}
