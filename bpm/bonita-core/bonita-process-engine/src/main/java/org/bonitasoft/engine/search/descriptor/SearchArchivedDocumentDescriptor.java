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
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.document.ArchivedDocumentsSearchDescriptor;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.bonitasoft.engine.core.document.model.archive.builder.SADocumentMappingBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 */
public class SearchArchivedDocumentDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> documentAllFields;

    SearchArchivedDocumentDescriptor() {
        searchEntityKeys = new HashMap<String, FieldDescriptor>(11);
        final SADocumentMappingBuilderFactory fact = BuilderFactory.get(SADocumentMappingBuilderFactory.class);
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.ARCHIVE_DATE,
                new FieldDescriptor(SAMappedDocument.class, fact.getArchiveDateKey()));
        //        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.CONTENT_STORAGE_ID,
        //                new FieldDescriptor(SAMappedDocument.class, fact.getContentStorageIdKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_AUTHOR,
                new FieldDescriptor(SAMappedDocument.class, "document." + fact.getAuthorKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_CONTENT_FILENAME,
                new FieldDescriptor(SAMappedDocument.class, "document." + fact.getFileNameKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_CONTENT_MIMETYPE,
                new FieldDescriptor(SAMappedDocument.class, "document." + fact.getMimeTypeKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_CREATIONDATE,
                new FieldDescriptor(SAMappedDocument.class, "document." + fact.getCreationDateKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_HAS_CONTENT,
                new FieldDescriptor(SAMappedDocument.class, "document." + fact.getHasContentKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_NAME,
                new FieldDescriptor(SAMappedDocument.class, fact.getNameKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_DESCRIPTION, new FieldDescriptor(SAMappedDocument.class, fact.getDescriptionKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_VERSION, new FieldDescriptor(SAMappedDocument.class, fact.getVersionKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.LIST_INDEX, new FieldDescriptor(SAMappedDocument.class, fact.getIndexKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.DOCUMENT_URL,
                new FieldDescriptor(SAMappedDocument.class, "document." + fact.getURLKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.PROCESSINSTANCE_ID,
                new FieldDescriptor(SAMappedDocument.class, fact.getProcessInstanceIdKey()));
        searchEntityKeys.put(ArchivedDocumentsSearchDescriptor.SOURCEOBJECT_ID,
                new FieldDescriptor(SAMappedDocument.class, fact.getSourceObjectIdKey()));

        documentAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> documentFields = new HashSet<String>(7);
        documentFields.add(fact.getNameKey());
        documentFields.add(fact.getDescriptionKey());
        documentFields.add("document." + fact.getFileNameKey());
        documentFields.add("document." + fact.getMimeTypeKey());
        documentFields.add("document." + fact.getURLKey());
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
