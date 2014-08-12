/**
 * Copyright (C) 2012-2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.document.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bonitasoft.engine.document.DocumentService;
import org.bonitasoft.engine.document.SDocumentContentNotFoundException;
import org.bonitasoft.engine.document.SDocumentNotFoundException;
import org.bonitasoft.engine.document.SDocumentStorageException;
import org.bonitasoft.engine.document.model.SDocument;

/**
 * Temporary implementation to run tests in memory
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class InMemoryTemporaryDocumentServiceImpl implements DocumentService {

    private final Map<String, SDocument> inMemoryDocStore = new HashMap<String, SDocument>();

    private final Map<String, byte[]> inMemoryContentStore = new HashMap<String, byte[]>();

    @Override
    public byte[] getContent(final String documentId) {
        return inMemoryContentStore.get(documentId);
    }

    @Override
    public SDocument storeDocumentContent(final SDocument sDocument, final byte[] documentContent) throws SDocumentStorageException {
        final String documentId = String.valueOf(UUID.randomUUID().getLeastSignificantBits());
        inMemoryDocStore.put(documentId, sDocument);
        inMemoryContentStore.put(documentId, documentContent);
        try {
            sDocument.getClass().getMethod("setId", String.class).invoke(sDocument, documentId);
            return sDocument;
        } catch (final Exception e) {
            inMemoryDocStore.remove(documentId);
            inMemoryContentStore.remove(documentId);
            throw new SDocumentStorageException(e);
        }
    }

    @Override
    public void deleteDocumentContent(final String documentId) throws SDocumentContentNotFoundException, SDocumentNotFoundException {
        if (inMemoryDocStore.remove(documentId) == null) {
            throw new SDocumentNotFoundException(documentId);
        }
        if (inMemoryContentStore.remove(documentId) == null) {
            throw new SDocumentContentNotFoundException(documentId);
        }
    }

}
