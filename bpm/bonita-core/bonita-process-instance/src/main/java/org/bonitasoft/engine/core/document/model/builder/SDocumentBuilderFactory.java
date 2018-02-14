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
package org.bonitasoft.engine.core.document.model.builder;

/**
 * @author Nicolas Chabanoles
 * @author Zhang Bole
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public interface SDocumentBuilderFactory {

    String getIdKey();

    String getNameKey();

    String getAuthorKey();

    String getCreationDateKey();

    String getHasContentKey();

    String getFileNameKey();

    String getMimeTypeKey();

    String getURLKey();

    SDocumentBuilder createNewInstance(String fileName, String mimetype, long authorId);

    /**
     * @param fileName
     * @param mimetype
     * @param authorId
     * @param content
     * @return
     * @throws IllegalArgumentException
     *         if the content or the fileName is null or empty
     * @since 6.4.0
     */
    SDocumentBuilder createNewProcessDocument(String fileName, String mimetype, long authorId, byte[] content);

    SDocumentBuilder createNewExternalProcessDocumentReference(String fileName, String mimetype, long authorId, final String url);

    String getDescriptionKey();

    String getVersionKey();

    String getIndexKey();
}
