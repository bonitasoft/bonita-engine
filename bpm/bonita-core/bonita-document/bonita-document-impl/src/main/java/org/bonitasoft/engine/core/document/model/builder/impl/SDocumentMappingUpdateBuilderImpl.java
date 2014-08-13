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
package org.bonitasoft.engine.core.document.model.builder.impl;

import org.bonitasoft.engine.core.document.model.builder.SDocumentMappingUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Nicolas Chabanoles
 */
public class SDocumentMappingUpdateBuilderImpl implements SDocumentMappingUpdateBuilder {

    private final EntityUpdateDescriptor descriptor;

    public SDocumentMappingUpdateBuilderImpl(final EntityUpdateDescriptor descriptor) {
        super();
        this.descriptor = descriptor;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SDocumentMappingUpdateBuilder setDocumentName(final String documentName) {
        descriptor.addField(SDocumentMappingBuilderFactoryImpl.DOCUMENT_NAME, documentName);
        return this;
    }

    @Override
    public SDocumentMappingUpdateBuilder setDocumentAuthor(final long author) {
        descriptor.addField(SDocumentMappingBuilderFactoryImpl.DOCUMENT_AUTHOR, author);
        return this;
    }

    @Override
    public SDocumentMappingUpdateBuilder setDocumentCreationDate(final long creationDate) {
        descriptor.addField(SDocumentMappingBuilderFactoryImpl.DOCUMENT_CREATION_DATE, creationDate);
        return this;
    }

    @Override
    public SDocumentMappingUpdateBuilder setHasContent(final boolean hasContent) {
        descriptor.addField(SDocumentMappingBuilderFactoryImpl.DOCUMENT_HAS_CONTENT, hasContent);
        return this;
    }

    @Override
    public SDocumentMappingUpdateBuilder setDocumentContentFileName(final String contentFileName) {
        descriptor.addField(SDocumentMappingBuilderFactoryImpl.DOCUMENT_CONTENT_FILENAME, contentFileName);
        return this;
    }

    @Override
    public SDocumentMappingUpdateBuilder setDocumentContentMimeType(final String contentMimeType) {
        descriptor.addField(SDocumentMappingBuilderFactoryImpl.DOCUMENT_CONTENT_MIMETYPE, contentMimeType);
        return this;
    }

    @Override
    public SDocumentMappingUpdateBuilder setDocumentStorageId(final String storageId) {
        descriptor.addField(SDocumentMappingBuilderFactoryImpl.DOCUMENT_CONTENT_STORAGE_ID, storageId);
        return this;
    }

    @Override
    public SDocumentMappingUpdateBuilder setDocumentURL(final String url) {
        descriptor.addField(SDocumentMappingBuilderFactoryImpl.DOCUMENT_URL, url);
        return this;
    }

}
