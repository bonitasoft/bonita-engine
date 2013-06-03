/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.document.mapping.model.builder.impl;

import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Nicolas Chabanoles
 */
public class SDocumentMappingUpdateBuilderImpl implements SDocumentMappingUpdateBuilder {

    private final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SDocumentMappingUpdateBuilder createNewInstance() {
        return this;
    }

    @Override
    public SDocumentMappingUpdateBuilder setDocumentName(final String documentName) {
        descriptor.addField(SDocumentMappingBuilderImpl.DOCUMENT_NAME, documentName);
        return this;
    }

    @Override
    public SDocumentMappingUpdateBuilder setDocumentAuthor(final long author) {
        descriptor.addField(SDocumentMappingBuilderImpl.DOCUMENT_AUTHOR, author);
        return this;
    }

    @Override
    public SDocumentMappingUpdateBuilder setDocumentCreationDate(final long creationDate) {
        descriptor.addField(SDocumentMappingBuilderImpl.DOCUMENT_CREATION_DATE, creationDate);
        return this;
    }

    @Override
    public SDocumentMappingUpdateBuilder setHasContent(final boolean hasContent) {
        descriptor.addField(SDocumentMappingBuilderImpl.DOCUMENT_HAS_CONTENT, hasContent);
        return this;
    }

    @Override
    public SDocumentMappingUpdateBuilder setDocumentContentFileName(final String contentFileName) {
        descriptor.addField(SDocumentMappingBuilderImpl.DOCUMENT_CONTENT_FILENAME, contentFileName);
        return this;
    }

    @Override
    public SDocumentMappingUpdateBuilder setDocumentContentMimeType(final String contentMimeType) {
        descriptor.addField(SDocumentMappingBuilderImpl.DOCUMENT_CONTENT_MIMETYPE, contentMimeType);
        return this;
    }

    @Override
    public SDocumentMappingUpdateBuilder setDocumentStorageId(final String storageId) {
        descriptor.addField(SDocumentMappingBuilderImpl.DOCUMENT_CONTENT_STORAGE_ID, storageId);
        return this;
    }

    @Override
    public SDocumentMappingUpdateBuilder setDocumentURL(final String url) {
        descriptor.addField(SDocumentMappingBuilderImpl.DOCUMENT_URL, url);
        return this;
    }

}
