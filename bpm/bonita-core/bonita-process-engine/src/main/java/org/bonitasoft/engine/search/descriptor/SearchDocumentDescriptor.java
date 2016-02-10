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

import org.bonitasoft.engine.bpm.document.DocumentsSearchDescriptor;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Zhang Bole
 * @author Baptiste Mesta
 */
public class SearchDocumentDescriptor extends SearchEntityDescriptor {

    public static final String DOCUMENT_PREFIX = "document.";
    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> documentAllFields;

    SearchDocumentDescriptor() {
        final SDocumentBuilderFactory fact = BuilderFactory.get(SDocumentBuilderFactory.class);
        searchEntityKeys = new HashMap<String, FieldDescriptor>(9);

        //        searchEntityKeys.put(DocumentsSearchDescriptor.CONTENT_STORAGE_ID, new FieldDescriptor(SDocument.class, fact.geContentStorageIdKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_AUTHOR, new FieldDescriptor(SMappedDocument.class, DOCUMENT_PREFIX + fact.getAuthorKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_CONTENT_FILENAME,
                new FieldDescriptor(SMappedDocument.class, DOCUMENT_PREFIX + fact.getFileNameKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_CONTENT_MIMETYPE,
                new FieldDescriptor(SMappedDocument.class, DOCUMENT_PREFIX + fact.getMimeTypeKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_CREATIONDATE,
                new FieldDescriptor(SMappedDocument.class, DOCUMENT_PREFIX + fact.getCreationDateKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_HAS_CONTENT,
                new FieldDescriptor(SMappedDocument.class, DOCUMENT_PREFIX + fact.getHasContentKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_NAME, new FieldDescriptor(SMappedDocument.class, fact.getNameKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_DESCRIPTION, new FieldDescriptor(SMappedDocument.class, fact.getDescriptionKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_VERSION, new FieldDescriptor(SMappedDocument.class, fact.getVersionKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.LIST_INDEX, new FieldDescriptor(SMappedDocument.class, fact.getIndexKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.DOCUMENT_URL, new FieldDescriptor(SMappedDocument.class, DOCUMENT_PREFIX + fact.getURLKey()));
        searchEntityKeys.put(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, new FieldDescriptor(SMappedDocument.class, "processInstanceId"));

        documentAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> documentFields = new HashSet<String>(8);
        documentFields.add(DOCUMENT_PREFIX + fact.getFileNameKey());
        documentFields.add(DOCUMENT_PREFIX + fact.getMimeTypeKey());
        documentFields.add(fact.getNameKey());
        documentFields.add(fact.getDescriptionKey());
        documentFields.add(DOCUMENT_PREFIX + fact.getURLKey());
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
