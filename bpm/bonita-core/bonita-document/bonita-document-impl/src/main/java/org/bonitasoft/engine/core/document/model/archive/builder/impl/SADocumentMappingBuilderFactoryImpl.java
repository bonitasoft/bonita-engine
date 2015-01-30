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

import org.bonitasoft.engine.core.document.model.SDocumentMapping;
import org.bonitasoft.engine.core.document.model.archive.builder.SADocumentMappingBuilder;
import org.bonitasoft.engine.core.document.model.archive.builder.SADocumentMappingBuilderFactory;
import org.bonitasoft.engine.core.document.model.archive.impl.SADocumentMappingImpl;

/**
 * @author Nicolas Chabanoles
 * @author Zhang Bole
 */
public class SADocumentMappingBuilderFactoryImpl implements SADocumentMappingBuilderFactory {

    static final String ID = "id";

    static final String PROCESS_INSTANCE_ID = "processInstanceId";

    static final String ARCHIVE_DATE = "archiveDate";

    static final String SOURCE_OBJECT_ID = "sourceObjectId";

    static final String DOCUMENT_ID = "documentId";

    static final String URL = "url";

    static final String NAME = "name";

    static final String HAS_CONTENT = "hasContent";

    static final String AUTHOR = "author";

    static final String FILE_NAME = "fileName";

    static final String MIME_TYPE = "mimeType";

    static final String CREATION_DATE = "creationDate";

    static final String DESCRIPTION = "description";

    static final String VERSION = "version";

    static final String INDEX = "index";

    @Override
    public SADocumentMappingBuilder createNewInstance() {
        final SADocumentMappingImpl documentMapping = new SADocumentMappingImpl();
        return new SADocumentMappingBuilderImpl(documentMapping);
    }

    @Override
    public SADocumentMappingBuilder createNewInstance(SDocumentMapping documentMapping) {
        return new SADocumentMappingBuilderImpl(new SADocumentMappingImpl(documentMapping.getDocumentId(), documentMapping.getProcessInstanceId(),
                System.currentTimeMillis(), documentMapping.getId(), documentMapping.getName(), documentMapping.getDescription(), documentMapping.getVersion()));
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
    public String getNameKey() {
        return NAME;
    }

    @Override
    public String getSourceObjectIdKey() {
        return SOURCE_OBJECT_ID;
    }

    @Override
    public String getArchiveDateKey() {
        return ARCHIVE_DATE;
    }

    @Override
    public String getAuthorKey() {
        return AUTHOR;
    }

    @Override
    public String getCreationDateKey() {
        return CREATION_DATE;
    }

    @Override
    public String getHasContentKey() {
        return HAS_CONTENT;
    }

    @Override
    public String getFileNameKey() {
        return FILE_NAME;
    }

    @Override
    public String getMimeTypeKey() {
        return MIME_TYPE;
    }

    @Override
    public String getContentStorageIdKey() {
        return DOCUMENT_ID;
    }

    @Override
    public String getURLKey() {
        return URL;
    }

    @Override
    public String getDescriptionKey() {
        return DESCRIPTION;
    }

    @Override
    public String getVersionKey() {
        return VERSION;
    }

    @Override
    public String getIndexKey() {
        return INDEX;
    }
}
