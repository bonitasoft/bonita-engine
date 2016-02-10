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
package org.bonitasoft.engine.search.document;

import java.util.List;

import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractDocumentSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchDocumentDescriptor;

/**
 * @author Zhang Bole
 */
public class SearchDocuments extends AbstractDocumentSearchEntity {

    private final DocumentService documentService;

    public SearchDocuments(final DocumentService documentService, final SearchDocumentDescriptor searchDescriptor, final SearchOptions options) {
        super(searchDescriptor, options, documentService);
        this.documentService = documentService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return documentService.getNumberOfDocuments(searchOptions);
    }

    @Override
    public List<SMappedDocument> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return documentService.searchDocuments(searchOptions);
    }

}
