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
package org.bonitasoft.engine.core.process.document.mapping.model.archive.builder.impl;

import org.bonitasoft.engine.core.process.document.mapping.model.SDocumentMapping;
import org.bonitasoft.engine.core.process.document.mapping.model.archive.SADocumentMapping;
import org.bonitasoft.engine.core.process.document.mapping.model.archive.builder.SADocumentMappingBuilder;
import org.bonitasoft.engine.core.process.document.mapping.model.archive.impl.SADocumentMappingImpl;

/**
 * @author Nicolas Chabanoles
 * @author Zhang Bole
 */
public class SADocumentMappingBuilderImpl implements SADocumentMappingBuilder {

    private SADocumentMappingImpl documentMapping;

    static final String ID = "id";

    static final String PROCESS_INSTANCE_ID = "processInstanceId";

    static final String ARCHIVEDATE = "archiveDate";

    static final String SOURCE_OBJECT_ID = "sourceObjectId";

    static final String CONTENT_STORAGE_ID = "contentStorageId";

    static final String DOCUMENT_URL = "documentURL";

    static final String DOCUMENT_NAME = "documentName";

    static final String HAS_CONTENT = "documentHasContent";

    static final String DOCUMENT_AUTHOR = "documentAuthor";

    static final String DOCUMENT_CONTENT_FILENAME = "documentContentFileName";

    static final String DOCUMENT_CONTENT_MIMETYPE = "documentContentMimeType";

    static final String DOCUMENT_CREATIONDATE = "documentCreationDate";

    @Override
    public SADocumentMappingBuilder createNewInstance() {
        documentMapping = new SADocumentMappingImpl();
        return this;
    }

    @Override
    public SADocumentMappingBuilder createNewInstance(final SDocumentMapping documentMapping) {
        this.documentMapping = new SADocumentMappingImpl(documentMapping);
        return this;
    }

    @Override
    public String getIdKey() {
        return ID;
    }

    @Override
    public String getProcessInstanceIdKey() {
        return PROCESS_INSTANCE_ID;
    }

    @Override
    public String getDocumentNameKey() {
        return DOCUMENT_NAME;
    }

    @Override
    public SADocumentMappingBuilder setProcessInstanceId(final long processInstanceId) {
        documentMapping.setProcessInstanceId(processInstanceId);
        return this;
    }

    @Override
    public SADocumentMappingBuilder setDocumentName(final String documentName) {
        documentMapping.setDocumentName(documentName);
        return this;
    }

    @Override
    public SADocumentMappingBuilder setDocumentAuthor(final long author) {
        documentMapping.setDocumentAuthor(author);
        return this;
    }

    @Override
    public SADocumentMappingBuilder setHasContent(final boolean hasContent) {
        documentMapping.setDocumentHasContent(hasContent);
        return this;
    }

    @Override
    public SADocumentMappingBuilder setDocumentStorageId(final String storageId) {
        documentMapping.setContentStorageId(storageId);
        return this;
    }

    @Override
    public SADocumentMappingBuilder setDocumentCreationDate(final long creationDate) {
        documentMapping.setDocumentCreationDate(creationDate);
        return this;
    }

    @Override
    public SADocumentMappingBuilder setDocumentContentFileName(final String contentFileName) {
        documentMapping.setDocumentContentFileName(contentFileName);
        return this;
    }

    @Override
    public SADocumentMappingBuilder setDocumentContentMimeType(final String contentMimeType) {
        documentMapping.setDocumentContentMimeType(contentMimeType);
        return this;
    }

    @Override
    public SADocumentMappingBuilder setDocumentURL(final String url) {
        documentMapping.setDocumentURL(url);
        return this;
    }

    @Override
    public SADocumentMappingBuilder setContentStorageId(final String contentStorageId) {
        documentMapping.setContentStorageId(contentStorageId);
        return this;
    }

    @Override
    public SADocumentMapping done() {
        return documentMapping;
    }

    @Override
    public String getSourceObjectIdKey() {
        return SOURCE_OBJECT_ID;
    }

    @Override
    public String getArchiveDateKey() {
        return null;
    }

    @Override
    public String getDocumentAuthorKey() {
        return DOCUMENT_AUTHOR;
    }

    @Override
    public String getDocumentCreationDateKey() {
        return DOCUMENT_CREATIONDATE;
    }

    @Override
    public String getDocumentHasContentKey() {
        return HAS_CONTENT;
    }

    @Override
    public String getDocumentContentFileNameKey() {
        return DOCUMENT_CONTENT_FILENAME;
    }

    @Override
    public String getDocumentContentMimeTypeKey() {
        return DOCUMENT_CONTENT_MIMETYPE;
    }

    @Override
    public String getContentStorageIdKey() {
        return CONTENT_STORAGE_ID;
    }

    @Override
    public String getDocumentURLKey() {
        return DOCUMENT_URL;
    }

    @Override
    public SADocumentMappingBuilder setSourceObjectId(final long sourceObjectId) {
        documentMapping.setSourceObjectId(sourceObjectId);
        return this;
    }

    @Override
    public SADocumentMappingBuilder setArchiveDate(final long archiveDate) {
        documentMapping.setArchiveDate(archiveDate);
        return null;
    }

}
