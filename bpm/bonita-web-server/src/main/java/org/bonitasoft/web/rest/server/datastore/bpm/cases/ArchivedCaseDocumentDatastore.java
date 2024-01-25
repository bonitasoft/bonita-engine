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

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.engine.bpm.document.DocumentException;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseDocumentItem;
import org.bonitasoft.web.rest.model.bpm.cases.CaseDocumentItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.datastore.filter.Filters;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.rest.server.datastore.utils.Sorts;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasDelete;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Fabio Lombardi
 */
public class ArchivedCaseDocumentDatastore extends CommonDatastore<ArchivedCaseDocumentItem, ArchivedDocument>
        implements DatastoreHasGet<CaseDocumentItem>,
        DatastoreHasDelete {

    protected final ProcessAPI processAPI;

    protected SearchOptionsCreator searchOptionsCreator;

    /**
     * Default constructor.
     */
    public ArchivedCaseDocumentDatastore(final APISession engineSession, final ProcessAPI processAPI) {
        super(engineSession);
        this.processAPI = processAPI;
    }

    // GET Method
    @Override
    public ArchivedCaseDocumentItem get(final APIID id) {
        try {
            final ArchivedDocument documentItem = processAPI.getArchivedProcessDocument(id.toLong());
            return convertEngineToConsoleItem(documentItem);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    // GET Method for SEARCH
    public ItemSearchResult<ArchivedCaseDocumentItem> search(final int page, final int resultsByPage,
            final String search, final Map<String, String> filters, final String orders) {
        return searchDocument(page, resultsByPage, search, filters, orders);
    }

    protected ItemSearchResult<ArchivedCaseDocumentItem> searchDocument(final int page, final int resultsByPage,
            final String search,
            final Map<String, String> filters, final String orders) {

        try {
            final APIID supervisorAPIID = APIID.makeAPIID(filters.get(ArchivedCaseDocumentItem.FILTER_SUPERVISOR_ID));
            final APIID archivedCaseId = APIID.makeAPIID(filters.get(ArchivedCaseDocumentItem.FILTER_ARCHIVED_CASE_ID));

            if (supervisorAPIID != null) {
                filters.remove(ArchivedCaseDocumentItem.FILTER_SUPERVISOR_ID);
            }

            if (archivedCaseId != null && archivedCaseId.isValidLongID()) {
                filters.remove(ArchivedCaseDocumentItem.FILTER_ARCHIVED_CASE_ID);
                final ArchivedProcessInstance archivedProcessInstance = processAPI
                        .getArchivedProcessInstance(archivedCaseId.toLong());
                if (archivedProcessInstance != null) {
                    final Long sourceCaseId = archivedProcessInstance.getSourceObjectId();
                    filters.put(ArchivedCaseDocumentItem.ATTRIBUTE_CASE_ID, sourceCaseId.toString());
                }
            }

            searchOptionsCreator = buildSearchOptionCreator(page, resultsByPage, search, filters, orders);

            final SearchResult<ArchivedDocument> engineSearchResults;
            if (supervisorAPIID != null && supervisorAPIID.isValidLongID()) {
                engineSearchResults = processAPI.searchArchivedDocumentsSupervisedBy(supervisorAPIID.toLong(),
                        searchOptionsCreator.create());
            } else {
                engineSearchResults = processAPI.searchArchivedDocuments(searchOptionsCreator.create());
            }
            return new ItemSearchResult<>(page, resultsByPage, engineSearchResults.getCount(),
                    convertEngineToConsoleItem(engineSearchResults.getResult()));
        } catch (final ArchivedProcessInstanceNotFoundException e) {
            throw new APIException("archivedCaseId not found. Request with bad param value.");
        } catch (final UserNotFoundException e) {
            throw new APIException("supervisor_id not found. Request with bad param value.");
        } catch (final SearchException e) {
            throw new APIException("Error while searching.");
        }
    }

    protected SearchOptionsCreator buildSearchOptionCreator(final int page, final int resultsByPage,
            final String search, final Map<String, String> filters,
            final String orders) {
        return new SearchOptionsCreator(page, resultsByPage, search, new Sorts(orders,
                getArchivedDocumentSearchAttributeConverter()),
                new Filters(filters, new ArchivedCaseDocumentFilterCreator(
                        getArchivedDocumentSearchAttributeConverter())));
    }

    private ArchivedCaseDocumentSearchAttributeConverter getArchivedDocumentSearchAttributeConverter() {
        return new ArchivedCaseDocumentSearchAttributeConverter();
    }

    // DELETE Method
    @Override
    public void delete(final List<APIID> ids) {
        if (ids != null) {

            try {
                for (final APIID id : ids) {
                    processAPI.deleteContentOfArchivedDocument(id.toLong());
                }
            } catch (final DocumentNotFoundException e) {
                throw new APIException("Error while deleting a document. Document not found");
            } catch (final DocumentException e) {
                throw new APIException(e);
            }

        } else {
            throw new APIException("Error while deleting a document. Document id not specified in the request");
        }
    }

    @Override
    protected ArchivedCaseDocumentItem convertEngineToConsoleItem(final ArchivedDocument item) {
        if (item != null) {
            return new ArchivedCaseDocumentItemConverter().convert(item);
        }
        return null;
    }

    private List<ArchivedCaseDocumentItem> convertEngineToConsoleItem(final List<ArchivedDocument> result) {
        if (result != null) {
            return new ArchivedCaseDocumentItemConverter().convert(result);
        }
        return null;
    }
}
