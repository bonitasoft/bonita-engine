/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.document.model.impl;

import org.bonitasoft.engine.core.process.document.model.SProcessDocument;

/**
 * @author Nicolas Chabanoles
 */
public class SProcessDocumentImpl implements SProcessDocument {

    private static final long serialVersionUID = -3081890604363134535L;

    private long id;

    private long tenantId;

    private long processInstanceId;

    private long author;

    private long creationDate;

    private String contentMimeType;

    private String fileName;

    private long contentSize;

    private String storageContentId;

    private boolean hasContent;

    private String name;

    private String discriminator;

    private String url;

    public SProcessDocumentImpl() {
    }

    public SProcessDocumentImpl(final SProcessDocument sProcessDocument) {
        this.id = sProcessDocument.getId();
        this.processInstanceId = sProcessDocument.getProcessInstanceId();
        this.author = sProcessDocument.getAuthor();
        this.author = sProcessDocument.getAuthor();
        this.creationDate = sProcessDocument.getCreationDate();
        this.contentMimeType = sProcessDocument.getContentMimeType();
        this.fileName = sProcessDocument.getContentFileName();
        this.contentSize = sProcessDocument.getContentSize();
    }

    @Override
    public long getAuthor() {
        return author;
    }

    @Override
    public long getCreationDate() {
        return creationDate;
    }

    @Override
    public String getContentMimeType() {
        return contentMimeType;
    }

    @Override
    public String getContentFileName() {
        return fileName;
    }

    @Override
    public long getContentSize() {
        return contentSize;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setTenantId(final long id) {
        this.tenantId = id;
    }

    public void setAuthor(final long author) {
        this.author = author;
    }

    public void setCreationDate(final long creationDate) {
        this.creationDate = creationDate;
    }

    public void setContentMimeType(final String contentMimeType) {
        this.contentMimeType = contentMimeType;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public void setContentSize(final long contentSize) {
        this.contentSize = contentSize;
    }

    @Override
    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(final long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public boolean hasContent() {
        return hasContent;
    }

    public void setHasContent(final boolean hasContent) {
        this.hasContent = hasContent;
    }

    @Override
    public String getContentStorageId() {
        return storageContentId;
    }

    public void setContentStorageId(final String storageContentId) {
        this.storageContentId = storageContentId;
    }

    @Override
    public String getDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(final String discriminator) {
        this.discriminator = discriminator;
    }

    public void setURL(final String url) {
        this.url = url;
    }

    @Override
    public String getURL() {
        return url;
    }

}
