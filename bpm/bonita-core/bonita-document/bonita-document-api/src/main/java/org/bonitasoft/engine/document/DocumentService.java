/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.document;

import org.bonitasoft.engine.document.model.SDocument;

/**
 * Document Service
 * 
 * @author Nicolas Chabanoles
 * @author Celine Souchet
 * @since 6.0
 */
public interface DocumentService {

    /**
     * Get content for specific document
     * 
     * @param documentId
     *            Identifier of document
     * @return content of document, it is a byte array
     * @throws org.bonitasoft.engine.document.SDocumentException
     *             Error thrown if has exception during the document content searching
     */
    byte[] getContent(String documentId) throws SDocumentException;

    /**
     * Store the document content
     * 
     * @param sDocument
     * @param documentContent
     *            Content of document, it is a byte array
     * @return document object with storage id
     * @throws org.bonitasoft.engine.document.SDocumentStorageException
     *             Error thrown if has exception during the document content storage
     */
    SDocument storeDocumentContent(SDocument sDocument, byte[] documentContent) throws SDocumentStorageException;

    /**
     * Delete the document content
     * 
     * @param documentId
     * @throws org.bonitasoft.engine.document.SDocumentDeletionException
     *             Error thrown if has exception during the document content delete
     * @throws org.bonitasoft.engine.document.SDocumentException
     *             Error thrown if has exception during the document content retrieve
     */
    void deleteDocumentContent(String documentId) throws SDocumentDeletionException, SDocumentException;

}
