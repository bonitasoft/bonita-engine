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
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SDocumentMapping;
import org.bonitasoft.engine.core.document.model.SLightDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Zhang Bole
 * @author Baptiste Mesta
 */
public class SearchDocumentDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> documentAllFields;

    SearchDocumentDescriptor() {
        final SDocumentBuilderFactory fact = BuilderFactory.get(SDocumentBuilderFactory.class);
        searchEntityKeys = new HashMap<String, FieldDescriptor>(9);

//        searchEntityKeys.put(DocumentsSearchDescriptor.CONTENT_STORAGE_ID, new FieldDescriptor(SDocument.class, fact.geContentStorageIdKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_AUTHOR, new FieldDescriptor(SMappedDocument.class, "document."+fact.getAuthorKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_CONTENT_FILENAME, new FieldDescriptor(SMappedDocument.class, "document."+fact.getFileNameKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_CONTENT_MIMETYPE, new FieldDescriptor(SMappedDocument.class, "document."+fact.getMimeTypeKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_CREATIONDATE, new FieldDescriptor(SMappedDocument.class, "document."+fact.getCreationDateKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_HAS_CONTENT, new FieldDescriptor(SMappedDocument.class, "document."+fact.getHasContentKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_NAME, new FieldDescriptor(SMappedDocument.class, "document."+fact.getNameKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_URL, new FieldDescriptor(SMappedDocument.class, "document."+fact.getURLKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, new FieldDescriptor(SMappedDocument.class, "processInstanceId"));

        documentAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> documentFields = new HashSet<String>(8);
//        documentFields.add(fact.geContentStorageIdKey());
        documentFields.add("document."+fact.getFileNameKey());
        documentFields.add("document."+fact.getMimeTypeKey());
        documentFields.add("document."+fact.getNameKey());
        documentFields.add("document."+fact.getURLKey());
        documentAllFields.put(SMappedDocument.class, documentFields);
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
