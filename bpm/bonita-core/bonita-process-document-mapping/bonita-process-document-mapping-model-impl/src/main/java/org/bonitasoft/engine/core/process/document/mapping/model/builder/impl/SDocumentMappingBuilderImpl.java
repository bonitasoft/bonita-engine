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
package org.bonitasoft.engine.core.process.document.mapping.model.builder.impl;

import org.bonitasoft.engine.core.process.document.mapping.model.SDocumentMapping;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilder;
import org.bonitasoft.engine.core.process.document.mapping.model.impl.SDocumentMappingImpl;

/**
 * @author Emmanuel Duchastenier
 * @author Nicolas Chabanoles
 * @author Zhang Bole
 */
public class SDocumentMappingBuilderImpl implements SDocumentMappingBuilder {

    private SDocumentMappingImpl documentMapping;

    static final String ID = "id";

    static final String PROCESS_INSTANCE_ID = "processInstanceId";

    static final String DOCUMENT_NAME = "documentName";

    static final String DOCUMENT_AUTHOR = "documentAuthor";

    static final String DOCUMENT_CREATION_DATE = "documentCreationDate";

    static final String DOCUMENT_HAS_CONTENT = "documentHasContent";

    static final String DOCUMENT_CONTENT_FILENAME = "documentContentFileName";

    static final String DOCUMENT_CONTENT_MIMETYPE = "documentContentMimeType";

    static final String DOCUMENT_CONTENT_STORAGE_ID = "contentStorageId";

    static final String DOCUMENT_URL = "documentURL";

    @Override
    public SDocumentMappingBuilder createNewInstance() {
        this.documentMapping = new SDocumentMappingImpl();
        return this;
    }

    @Override
    public SDocumentMappingBuilder createNewInstance(final SDocumentMapping documentMapping) {
        this.documentMapping = new SDocumentMappingImpl(documentMapping);
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
    public String getDocumentAuthorKey() {
        return DOCUMENT_AUTHOR;
    }

    @Override
    public String getDocumentCreationDateKey() {
        return DOCUMENT_CREATION_DATE;
    }

    @Override
    public String getDocumentHasContent() {
        return DOCUMENT_HAS_CONTENT;
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
    public String geContentStorageIdKey() {
        return DOCUMENT_CONTENT_STORAGE_ID;
    }

    @Override
    public String getDocumentURLKey() {
        return DOCUMENT_URL;
    }

    @Override
    public SDocumentMappingBuilder setProcessInstanceId(final long processInstanceId) {
        this.documentMapping.setProcessInstanceId(processInstanceId);
        return this;
    }

    @Override
    public SDocumentMappingBuilder setDocumentName(final String documentName) {
        documentMapping.setDocumentName(documentName);
        return this;
    }

    @Override
    public SDocumentMappingBuilder setDocumentAuthor(final long author) {
        documentMapping.setDocumentAuthor(author);
        return this;
    }

    @Override
    public SDocumentMappingBuilder setHasContent(final boolean hasContent) {
        documentMapping.setDocumentHasContent(hasContent);
        return this;
    }

    @Override
    public SDocumentMappingBuilder setDocumentStorageId(final String storageId) {
        documentMapping.setContentStorageId(storageId);
        return this;
    }

    @Override
    public SDocumentMappingBuilder setDocumentCreationDate(final long creationDate) {
        documentMapping.setDocumentCreationDate(creationDate);
        return this;
    }

    @Override
    public SDocumentMappingBuilder setDocumentContentFileName(final String contentFileName) {
        documentMapping.setDocumentContentFileName(contentFileName);
        return this;
    }

    @Override
    public SDocumentMappingBuilder setDocumentContentMimeType(final String contentMimeType) {
        documentMapping.setDocumentContentMimeType(contentMimeType);
        return this;
    }

    @Override
    public SDocumentMappingBuilder setDocumentURL(final String url) {
        documentMapping.setDocumentURL(url);
        return this;
    }

    @Override
    public SDocumentMapping done() {
        return documentMapping;
    }
}
