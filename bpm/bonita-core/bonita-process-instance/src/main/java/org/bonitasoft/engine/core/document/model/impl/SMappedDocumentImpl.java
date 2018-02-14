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
import org.bonitasoft.engine.core.document.model.SLightDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.archive.SADocumentMapping;

/**
 * @author Baptiste Mesta
 */
public class SMappedDocumentImpl implements SMappedDocument {

    private long id;
    private long tenantId;
    private long documentId;
    private long processInstanceId;
    private String name;
    private String description;
    private String version;
    private int index;
    private SLightDocument document;


    public SMappedDocumentImpl() {
    }

    public SMappedDocumentImpl(SDocumentMapping documentMapping, SLightDocument document) {
        this.id = documentMapping.getId();
        this.name = documentMapping.getName();
        this.description = documentMapping.getDescription();
        this.version = documentMapping.getVersion();
        this.document = document;
        this.documentId = documentMapping.getDocumentId();
        this.processInstanceId = documentMapping.getProcessInstanceId();
        this.index = documentMapping.getIndex();
    }

    public SMappedDocumentImpl(SADocumentMapping documentMapping, SLightDocument document) {
        this.id = documentMapping.getSourceObjectId();
        this.document = document;
        this.documentId = documentMapping.getDocumentId();
        this.processInstanceId = documentMapping.getProcessInstanceId();
        this.name = documentMapping.getName();
        this.description = documentMapping.getDescription();
        this.version = documentMapping.getVersion();
        this.index = documentMapping.getIndex();
    }

    public long getDocumentId() {
        return documentId;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public SLightDocument getDocument() {
        return document;
    }

    public void setDocument(SLightDocument document) {
        this.document = document;
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

    @Override
    public long getAuthor() {
        return document.getAuthor();
    }

    @Override
    public long getCreationDate() {
        return document.getCreationDate();
    }

    @Override
    public String getMimeType() {
        return document.getMimeType();
    }

    @Override
    public String getFileName() {
        return document.getFileName();
    }

    @Override
    public boolean hasContent() {
        return document.hasContent();
    }

    @Override
    public String getUrl() {
        return document.getUrl();
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

    @Override
    public String getDiscriminator() {
        return SMappedDocumentImpl.class.getName();
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

        SMappedDocumentImpl that = (SMappedDocumentImpl) o;

        if (documentId != that.documentId) return false;
        if (id != that.id) return false;
        if (index != that.index) return false;
        if (processInstanceId != that.processInstanceId) return false;
        if (tenantId != that.tenantId) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (document != null ? !document.equals(that.document) : that.document != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (tenantId ^ (tenantId >>> 32));
        result = 31 * result + (int) (documentId ^ (documentId >>> 32));
        result = 31 * result + (int) (processInstanceId ^ (processInstanceId >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + index;
        result = 31 * result + (document != null ? document.hashCode() : 0);
        return result;
    }

}
