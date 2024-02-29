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
package org.bonitasoft.engine.core.document.model.builder;

/**
 * @author Nicolas Chabanoles
 * @author Zhang Bole
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class SDocumentBuilderFactory {

    public SDocumentBuilder createNewInstance(final String fileName, final String mimetype, final long authorId) {
        final SDocumentBuilder sDocumentBuilder = new SDocumentBuilder();
        sDocumentBuilder.setFileName(fileName);
        sDocumentBuilder.setMimeType(mimetype);
        sDocumentBuilder.setAuthor(authorId);
        sDocumentBuilder.setCreationDate(System.currentTimeMillis());
        return sDocumentBuilder;
    }

    public SDocumentBuilder createNewProcessDocument(final String fileName, final String mimetype, final long authorId,
            final byte[] content) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("The fileName must be filled for a document with content");
        }

        final SDocumentBuilder sDocumentBuilder = createNewInstance(fileName, mimetype, authorId);
        sDocumentBuilder.setContent(content);
        sDocumentBuilder.setHasContent(true);
        return sDocumentBuilder;
    }

    public SDocumentBuilder createNewExternalProcessDocumentReference(final String fileName, final String mimetype,
            final long authorId, final String url) {
        final SDocumentBuilder sDocumentBuilder = createNewInstance(fileName, mimetype, authorId);
        sDocumentBuilder.setURL(url);
        sDocumentBuilder.setHasContent(false);
        return sDocumentBuilder;
    }

}
