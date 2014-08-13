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
package org.bonitasoft.engine.core.document.api.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bonitasoft.engine.core.document.DocumentContentService;
import org.bonitasoft.engine.core.document.exception.SDocumentContentNotFoundException;
import org.bonitasoft.engine.core.document.exception.SDocumentNotFoundException;
import org.bonitasoft.engine.core.document.exception.SDocumentStorageException;
import org.bonitasoft.engine.core.document.model.SDocumentMapping;

/**
 * Temporary implementation to run tests in memory
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class InMemoryTemporaryDocumentContentServiceImpl implements DocumentContentService {

    private final Map<String, byte[]> inMemoryContentStore = new HashMap<String, byte[]>();

    @Override
    public byte[] getContent(final String documentId) {
        return inMemoryContentStore.get(documentId);
    }

    @Override
    public String storeDocumentContent(final SDocumentMapping sDocument, final byte[] documentContent) throws SDocumentStorageException {
        final String documentId = String.valueOf(UUID.randomUUID().getLeastSignificantBits());
        inMemoryContentStore.put(documentId, documentContent);
        return documentId;
    }

    @Override
    public void deleteDocumentContent(final String documentId) throws SDocumentContentNotFoundException, SDocumentNotFoundException {
        if (inMemoryContentStore.remove(documentId) == null) {
            throw new SDocumentContentNotFoundException(documentId);
        }
    }

}
