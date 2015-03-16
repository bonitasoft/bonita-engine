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
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractArchivedDocumentSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchArchivedDocumentDescriptor;

/**
 * @author Zhang Bole
 * @author Celine Souchet
 */
public class SearchArchivedDocumentsSupervisedBy extends AbstractArchivedDocumentSearchEntity {

    private final long userId;

    private final DocumentService documentService;

    public SearchArchivedDocumentsSupervisedBy(final long userId, final DocumentService documentService,
            final SearchArchivedDocumentDescriptor searchDescriptor, final SearchOptions options) {
        super(searchDescriptor, options, documentService);
        this.userId = userId;
        this.documentService = documentService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return documentService.getNumberOfArchivedDocumentsSupervisedBy(userId, searchOptions);
    }

    @Override
    public List<SAMappedDocument> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return documentService.searchArchivedDocumentsSupervisedBy(userId, searchOptions);
    }

}
