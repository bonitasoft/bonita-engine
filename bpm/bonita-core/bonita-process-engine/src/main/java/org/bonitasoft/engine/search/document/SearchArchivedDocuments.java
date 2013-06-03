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
package org.bonitasoft.engine.search.document;

import java.util.List;

import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.model.SAProcessDocument;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.search.AbstractArchivedDocumentSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchArchivedDocumentDescriptor;

/**
 * @author Zhang Bole
 */
public class SearchArchivedDocuments extends AbstractArchivedDocumentSearchEntity {

    private final ProcessDocumentService processDocumentService;

    private final ReadPersistenceService persistenceService;

    public SearchArchivedDocuments(final ProcessDocumentService processDocumentService, final SearchArchivedDocumentDescriptor searchDescriptor,
            final SearchOptions options, final ReadPersistenceService persistenceService) {
        super(searchDescriptor, options);
        this.processDocumentService = processDocumentService;
        this.persistenceService = persistenceService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaSearchException {
        return processDocumentService.getNumberOfArchivedDocuments(searchOptions, persistenceService);
    }

    @Override
    public List<SAProcessDocument> executeSearch(final QueryOptions searchOptions) throws SBonitaSearchException {
        return processDocumentService.searchArchivedDocuments(searchOptions, persistenceService);
    }

}
