/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.bpm.cases;

import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseDocumentItem;
import org.bonitasoft.web.rest.server.datastore.converter.ItemConverter;

public class ArchivedCaseDocumentItemConverter extends ItemConverter<ArchivedCaseDocumentItem, ArchivedDocument> {

    @Override
    public ArchivedCaseDocumentItem convert(final ArchivedDocument engineItem) {
        final ArchivedCaseDocumentItem item = new ArchivedCaseDocumentItem();
        item.setId(String.valueOf(engineItem.getId()));
        item.setCaseId(String.valueOf(engineItem.getProcessInstanceId()));
        item.setName(engineItem.getName());
        item.setVersion(engineItem.getVersion());
        item.setDescription(engineItem.getDescription());
        item.setSubmittedBy(engineItem.getAuthor());
        item.setFileName(engineItem.getContentFileName());
        item.setCreationDate(engineItem.getCreationDate());
        item.setMIMEType(engineItem.getContentMimeType());
        item.setHasContent(String.valueOf(engineItem.hasContent()));
        item.setStorageId(engineItem.getContentStorageId());
        item.setURL(engineItem.getUrl());
        item.setIndex(engineItem.getIndex());
        item.setSourceObjectId(engineItem.getSourceObjectId());
        item.setArchivedDate(engineItem.getArchiveDate());
        return item;
    }
}
