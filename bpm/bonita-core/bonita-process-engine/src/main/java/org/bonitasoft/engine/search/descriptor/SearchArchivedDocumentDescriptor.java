/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.document.ArchivedDocumentsSearchDescriptor;
import org.bonitasoft.engine.core.process.document.mapping.model.archive.SADocumentMapping;
import org.bonitasoft.engine.core.process.document.mapping.model.archive.builder.SADocumentMappingBuilder;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilderAccessor;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 */
public class SearchArchivedDocumentDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> documentAllFields;

    SearchArchivedDocumentDescriptor(final SDocumentMappingBuilderAccessor sDocumentMappingBuilderAccessor) {
        searchEntityKeys = new HashMap<String, FieldDescriptor>(11);
        final SADocumentMappingBuilder documentMappingBuilder = sDocumentMappingBuilderAccessor.getSADocumentMappingBuilder();
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.ARCHIVE_DATE,
                new FieldDescriptor(SADocumentMapping.class, documentMappingBuilder.getArchiveDateKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.CONTENT_STORAGE_ID,
                new FieldDescriptor(SADocumentMapping.class, documentMappingBuilder.getContentStorageIdKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_AUTHOR,
                new FieldDescriptor(SADocumentMapping.class, documentMappingBuilder.getDocumentAuthorKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_CONTENT_FILENAME,
                new FieldDescriptor(SADocumentMapping.class, documentMappingBuilder.getDocumentContentFileNameKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_CONTENT_MIMETYPE,
                new FieldDescriptor(SADocumentMapping.class, documentMappingBuilder.getDocumentContentMimeTypeKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_CREATIONDATE,
                new FieldDescriptor(SADocumentMapping.class, documentMappingBuilder.getDocumentCreationDateKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_HAS_CONTENT,
                new FieldDescriptor(SADocumentMapping.class, documentMappingBuilder.getDocumentHasContentKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_NAME,
                new FieldDescriptor(SADocumentMapping.class, documentMappingBuilder.getDocumentNameKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_URL,
                new FieldDescriptor(SADocumentMapping.class, documentMappingBuilder.getDocumentURLKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.PROCESSINSTANCE_ID,
                new FieldDescriptor(SADocumentMapping.class, documentMappingBuilder.getProcessInstanceIdKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.SOURCEOBJECT_ID,
                new FieldDescriptor(SADocumentMapping.class, documentMappingBuilder.getSourceObjectIdKey()));

        documentAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> documentFields = new HashSet<String>(7);
        documentFields.add(documentMappingBuilder.getDocumentNameKey());
        documentFields.add(documentMappingBuilder.getContentStorageIdKey());
        documentFields.add(documentMappingBuilder.getDocumentContentFileNameKey());
        documentFields.add(documentMappingBuilder.getDocumentContentMimeTypeKey());
        documentFields.add(documentMappingBuilder.getDocumentURLKey());
        documentAllFields.put(SADocumentMapping.class, documentFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return searchEntityKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return documentAllFields;
    }

}
