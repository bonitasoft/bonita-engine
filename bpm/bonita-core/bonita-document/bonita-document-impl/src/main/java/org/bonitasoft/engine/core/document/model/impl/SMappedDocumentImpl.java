/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
    private SLightDocument document;


    public SMappedDocumentImpl() {
    }

    public SMappedDocumentImpl(SDocumentMapping documentMapping, SLightDocument document) {
        this.id = documentMapping.getId();
        this.document = document;
        this.documentId = documentMapping.getDocumentId();
        this.processInstanceId = documentMapping.getProcessInstanceId();
    }

    public SMappedDocumentImpl(SADocumentMapping documentMapping, SLightDocument document) {
        this.id = documentMapping.getSourceObjectId();
        this.document = document;
        this.documentId = documentMapping.getDocumentId();
        this.processInstanceId = documentMapping.getProcessInstanceId();
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
    public String getName() {
        return document.getName();
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

    @Override
    public String getDiscriminator() {
        return SMappedDocumentImpl.class.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SMappedDocumentImpl that = (SMappedDocumentImpl) o;

        if (documentId != that.documentId) return false;
        if (id != that.id) return false;
        if (processInstanceId != that.processInstanceId) return false;
        if (tenantId != that.tenantId) return false;
        if (document != null ? !document.equals(that.document) : that.document != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (tenantId ^ (tenantId >>> 32));
        result = 31 * result + (int) (documentId ^ (documentId >>> 32));
        result = 31 * result + (int) (processInstanceId ^ (processInstanceId >>> 32));
        result = 31 * result + (document != null ? document.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SMappedDocumentImpl{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", documentId=" + documentId +
                ", processInstanceId=" + processInstanceId +
                ", document=" + document +
                '}';
    }
}
