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
package org.bonitasoft.engine.core.document.model.archive.builder.impl;

import org.bonitasoft.engine.core.document.model.archive.SADocumentMapping;
import org.bonitasoft.engine.core.document.model.archive.builder.SADocumentMappingBuilder;
import org.bonitasoft.engine.core.document.model.archive.impl.SADocumentMappingImpl;

/**
 * @author Nicolas Chabanoles
 * @author Zhang Bole
 */
public class SADocumentMappingBuilderImpl implements SADocumentMappingBuilder {

    private final SADocumentMappingImpl documentMapping;

    public SADocumentMappingBuilderImpl(final SADocumentMappingImpl documentMapping) {
        super();
        this.documentMapping = documentMapping;
    }

    @Override
    public SADocumentMappingBuilder setProcessInstanceId(final long processInstanceId) {
        documentMapping.setProcessInstanceId(processInstanceId);
        return this;
    }

    @Override
    public SADocumentMappingBuilder setDocumentId(long documentId) {
        documentMapping.setDocumentId(documentId);
        return this;
    }

    //    @Override
    //    public SADocumentMappingBuilder setName(final String documentName) {
    //        documentMapping.setName(documentName);
    //        return this;
    //    }
    //
    //    @Override
    //    public SADocumentMappingBuilder setAuthor(final long author) {
    //        documentMapping.setAuthor(author);
    //        return this;
    //    }
    //
    //    @Override
    //    public SADocumentMappingBuilder setHasContent(final boolean hasContent) {
    //        documentMapping.setDocumentHasContent(hasContent);
    //        return this;
    //    }
    //
    //    @Override
    //    public SADocumentMappingBuilder setDocumentStorageId(final String storageId) {
    //        documentMapping.setContentStorageId(storageId);
    //        return this;
    //    }
    //
    //    @Override
    //    public SADocumentMappingBuilder setCreationDate(final long creationDate) {
    //        documentMapping.setCreationDate(creationDate);
    //        return this;
    //    }
    //
    //    @Override
    //    public SADocumentMappingBuilder setFileName(final String contentFileName) {
    //        documentMapping.setFileName(contentFileName);
    //        return this;
    //    }
    //
    //    @Override
    //    public SADocumentMappingBuilder setMimeType(final String contentMimeType) {
    //        documentMapping.setMimeType(contentMimeType);
    //        return this;
    //    }
    //
    //    @Override
    //    public SADocumentMappingBuilder setURL(final String url) {
    //        documentMapping.setURL(url);
    //        return this;
    //    }
    //
    //    @Override
    //    public SADocumentMappingBuilder setContentStorageId(final String contentStorageId) {
    //        documentMapping.setContentStorageId(contentStorageId);
    //        return this;
    //    }

    @Override
    public SADocumentMapping done() {
        return documentMapping;
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
