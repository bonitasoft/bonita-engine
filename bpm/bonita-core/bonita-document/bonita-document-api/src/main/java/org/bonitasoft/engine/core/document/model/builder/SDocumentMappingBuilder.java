/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import org.bonitasoft.engine.core.document.model.SDocumentMapping;

/**
 * @author Nicolas Chabanoles
 * @author Zhang Bole
 */
public interface SDocumentMappingBuilder {

    SDocumentMappingBuilder setProcessInstanceId(final long processInstanceId);

    SDocumentMappingBuilder setDocumentName(String documentName);

    SDocumentMappingBuilder setDocumentAuthor(long author);

    SDocumentMappingBuilder setDocumentCreationDate(long creationDate);

    SDocumentMappingBuilder setHasContent(boolean hasContent);

    SDocumentMappingBuilder setDocumentContentFileName(String contentFileName);

    SDocumentMappingBuilder setDocumentContentMimeType(String contentMimeType);

    SDocumentMappingBuilder setDocumentStorageId(final String documentId);

    SDocumentMappingBuilder setDocumentURL(String generateURL);

    SDocumentMapping done();

}
