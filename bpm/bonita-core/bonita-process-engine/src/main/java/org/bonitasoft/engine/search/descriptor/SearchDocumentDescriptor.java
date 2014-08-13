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

import org.bonitasoft.engine.bpm.document.DocumentsSearchDescriptor;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.document.model.SDocumentMapping;
import org.bonitasoft.engine.core.document.model.builder.SDocumentMappingBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Zhang Bole
 * @author Baptiste Mesta
 */
public class SearchDocumentDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> documentAllFields;

    SearchDocumentDescriptor() {
        final SDocumentMappingBuilderFactory fact = BuilderFactory.get(SDocumentMappingBuilderFactory.class);
        searchEntityKeys = new HashMap<String, FieldDescriptor>(9);
        searchEntityKeys.put(DocumentsSearchDescriptor.CONTENT_STORAGE_ID, new FieldDescriptor(SDocumentMapping.class, fact.geContentStorageIdKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_AUTHOR, new FieldDescriptor(SDocumentMapping.class, fact.getDocumentAuthorKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_CONTENT_FILENAME, new FieldDescriptor(SDocumentMapping.class, fact.getDocumentContentFileNameKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_CONTENT_MIMETYPE, new FieldDescriptor(SDocumentMapping.class, fact.getDocumentContentMimeTypeKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_CREATIONDATE, new FieldDescriptor(SDocumentMapping.class, fact.getDocumentCreationDateKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_HAS_CONTENT, new FieldDescriptor(SDocumentMapping.class, fact.getDocumentHasContent()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_NAME, new FieldDescriptor(SDocumentMapping.class, fact.getDocumentNameKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_URL, new FieldDescriptor(SDocumentMapping.class, fact.getDocumentURLKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, new FieldDescriptor(SDocumentMapping.class, fact.getProcessInstanceIdKey()));

        documentAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> documentFields = new HashSet<String>(8);
        documentFields.add(fact.geContentStorageIdKey());
        documentFields.add(fact.getDocumentContentFileNameKey());
        documentFields.add(fact.getDocumentContentMimeTypeKey());
        documentFields.add(fact.getDocumentNameKey());
        documentFields.add(fact.getDocumentURLKey());
        documentAllFields.put(SDocumentMapping.class, documentFields);
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
