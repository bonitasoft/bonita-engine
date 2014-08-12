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
package org.bonitasoft.engine.core.process.document.model.builder;

import org.bonitasoft.engine.core.process.document.model.SAProcessDocument;

/**
 * @author Zhang Bole
 */
public interface SAProcessDocumentBuilder {

    SAProcessDocumentBuilder setId(long id);

    SAProcessDocumentBuilder setProcessInstanceId(final long processInstanceId);

    SAProcessDocumentBuilder setName(String name);

    SAProcessDocumentBuilder setAuthor(long author);

    SAProcessDocumentBuilder setCreationDate(long creationDate);

    SAProcessDocumentBuilder setHasContent(boolean hasContent);

    SAProcessDocumentBuilder setFileName(String fileName);

    SAProcessDocumentBuilder setContentMimeType(String contentMimeType);

    SAProcessDocumentBuilder setContentStorageId(String contentStorageId);

    SAProcessDocumentBuilder setURL(String url);

    SAProcessDocumentBuilder setArchiveDate(long archiveDate);

    SAProcessDocumentBuilder setSourceObjectId(long sourceObjectId);

    SAProcessDocument done();

}
