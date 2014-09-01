/*
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
package org.bonitasoft.engine.core.document.model.archive.impl;

import org.bonitasoft.engine.core.document.model.SLightDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.bonitasoft.engine.core.document.model.impl.SLightDocumentImpl;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 */
public class SAMappedDocumentImpl implements SAMappedDocument {

    private long id;
    private long tenantId;
    private long archiveDate;
    private long sourceObjectId;
    private long documentId;
    private long processInstanceId;
    private SLightDocument document;

    public long getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(long archiveDate) {
        this.archiveDate = archiveDate;
    }

    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    public long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SMappedDocument.class;
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

    public SLightDocument getDocument() {
        return document;
    }

    public void setDocument(SLightDocument document) {
        this.document = document;
    }

    @Override
    public String getDiscriminator() {
        return SAMappedDocument.class.getName();
    }
}
