/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.document.ArchivedDocumentsSearchDescriptor;
import org.bonitasoft.engine.core.document.model.archive.SADocumentMapping;
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 */
public class SearchArchivedDocumentDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> documentAllFields;

    SearchArchivedDocumentDescriptor() {
        searchEntityKeys = new HashMap<>(12);
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.ARCHIVE_DATE,
                new FieldDescriptor(SAMappedDocument.class, SADocumentMapping.ARCHIVE_DATE));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_AUTHOR,
                new FieldDescriptor(SAMappedDocument.class, "document." + SADocumentMapping.AUTHOR));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_CONTENT_FILENAME,
                new FieldDescriptor(SAMappedDocument.class, "document." + SADocumentMapping.FILE_NAME));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_CONTENT_MIMETYPE,
                new FieldDescriptor(SAMappedDocument.class, "document." + SADocumentMapping.MIME_TYPE));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_CREATIONDATE,
                new FieldDescriptor(SAMappedDocument.class, "document." + SADocumentMapping.CREATION_DATE));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_HAS_CONTENT,
                new FieldDescriptor(SAMappedDocument.class, "document." + SADocumentMapping.HAS_CONTENT));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_NAME,
                new FieldDescriptor(SAMappedDocument.class, SADocumentMapping.NAME));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_DESCRIPTION,
                new FieldDescriptor(SAMappedDocument.class, SADocumentMapping.DESCRIPTION));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_VERSION,
                new FieldDescriptor(SAMappedDocument.class, SADocumentMapping.VERSION));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.LIST_INDEX,
                new FieldDescriptor(SAMappedDocument.class, SADocumentMapping.INDEX));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_URL,
                new FieldDescriptor(SAMappedDocument.class, "document." + SADocumentMapping.URL));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.PROCESSINSTANCE_ID,
                new FieldDescriptor(SAMappedDocument.class, SADocumentMapping.PROCESS_INSTANCE_ID));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.SOURCEOBJECT_ID,
                new FieldDescriptor(SAMappedDocument.class, SADocumentMapping.SOURCE_OBJECT_ID));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.CONTENT_STORAGE_ID,
                new FieldDescriptor(SAMappedDocument.class, SADocumentMapping.DOCUMENT_ID));

        documentAllFields = new HashMap<>(1);
        final Set<String> documentFields = new HashSet<String>(7);
        documentFields.add(SADocumentMapping.NAME);
        documentFields.add(SADocumentMapping.DESCRIPTION);
        documentFields.add("document." + SADocumentMapping.FILE_NAME);
        documentFields.add("document." + SADocumentMapping.MIME_TYPE);
        documentFields.add("document." + SADocumentMapping.URL);
        documentAllFields.put(SAMappedDocument.class, documentFields);
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
