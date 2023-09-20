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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.preferences.properties.PropertiesFactory;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentException;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.cases.CaseDocumentItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.datastore.filter.Filters;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.rest.server.datastore.utils.Sorts;
import org.bonitasoft.web.rest.server.framework.api.*;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.util.StringUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Fabio Lombardi
 */
public class CaseDocumentDatastore extends CommonDatastore<CaseDocumentItem, Document>
        implements DatastoreHasAdd<CaseDocumentItem>,
        DatastoreHasGet<CaseDocumentItem>,
        DatastoreHasUpdate<CaseDocumentItem>, DatastoreHasDelete {

    protected final ProcessAPI processAPI;

    final long maxSizeForTenant;

    final FileTypeMap mimetypesFileTypeMap;

    final BonitaHomeFolderAccessor tenantFolder;

    protected SearchOptionsCreator searchOptionsCreator;

    /**
     * Default constructor.
     */
    public CaseDocumentDatastore(final APISession engineSession, final ProcessAPI processAPI,
            final BonitaHomeFolderAccessor tenantFolder) {
        super(engineSession);
        this.processAPI = processAPI;
        this.tenantFolder = tenantFolder;
        maxSizeForTenant = PropertiesFactory.getConsoleProperties().getMaxSize();
        mimetypesFileTypeMap = new MimetypesFileTypeMap();
    }

    // GET Method
    @Override
    public CaseDocumentItem get(final APIID id) {
        try {
            final Document documentItem = processAPI.getDocument(id.toLong());
            return convertEngineToConsoleItem(documentItem);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    // POST Method
    @Override
    public CaseDocumentItem add(final CaseDocumentItem item) {

        long caseId = -1;
        int index = -1;
        String documentDescription = "";
        DocumentValue documentValue = null;

        try {
            if (item.getAttributeValue(CaseDocumentItem.ATTRIBUTE_CASE_ID) != null) {
                caseId = Long.valueOf(item.getAttributeValue(CaseDocumentItem.ATTRIBUTE_CASE_ID));
            }

        } catch (final NumberFormatException e) {
            throw new APIException("Error while attaching a new document. Request with bad case id value.");
        }

        final String documentName = item.getAttributeValue(CaseDocumentItem.ATTRIBUTE_NAME);
        final String uploadPath = item.getAttributeValue(CaseDocumentItem.ATTRIBUTE_UPLOAD_PATH);
        final String urlPath = item.getAttributeValue(CaseDocumentItem.ATTRIBUTE_URL);

        if (item.getAttributeValue(CaseDocumentItem.ATTRIBUTE_DESCRIPTION) != null) {
            documentDescription = item.getAttributeValue(CaseDocumentItem.ATTRIBUTE_DESCRIPTION);
        }

        if (item.getAttributeValue(CaseDocumentItem.ATTRIBUTE_INDEX) != null) {
            index = Integer.parseInt(item.getAttributeValue(CaseDocumentItem.ATTRIBUTE_INDEX));
        }

        try {

            if (caseId != -1 && documentName != null) {

                if (urlPath != null) {
                    documentValue = buildDocumentValueFromUrl(urlPath, -1);
                } else {
                    documentValue = buildDocumentValueFromUploadPath(uploadPath, index, item.getFileName());
                }
                final Document document = processAPI.addDocument(caseId, documentName, documentDescription,
                        documentValue);
                return convertEngineToConsoleItem(document);
            } else {
                throw new APIException("Error while attaching a new document. Request with bad param value.");
            }
        } catch (final BonitaException | IOException e) {
            throw new APIException(e);
        } finally {
            if (urlPath == null) {
                tenantFolder.removeUploadedTempContent(uploadPath);
            }
        }
    }

    // PUT Method
    @Override
    public CaseDocumentItem update(final APIID id, final Map<String, String> attributes) {
        DocumentValue documentValue = null;

        try {
            final String urlPath;

            if (attributes.containsKey(CaseDocumentItem.ATTRIBUTE_UPLOAD_PATH)
                    || attributes.containsKey(CaseDocumentItem.ATTRIBUTE_URL)) {

                if (attributes.containsKey(CaseDocumentItem.ATTRIBUTE_URL)) {
                    urlPath = attributes.get(CaseDocumentItem.ATTRIBUTE_URL);
                    documentValue = buildDocumentValueFromUrl(urlPath, -1);
                } else {
                    urlPath = attributes.get(CaseDocumentItem.ATTRIBUTE_UPLOAD_PATH);
                    documentValue = buildDocumentValueFromUploadPath(urlPath, -1,
                            attributes.get(CaseDocumentItem.ATTRIBUTE_CONTENT_FILENAME));
                }

                final Document document = processAPI.updateDocument(id.toLong(), documentValue);
                return convertEngineToConsoleItem(document);

            } else {
                throw new APIException("Error while attaching a new document. Request with bad param value.");
            }
        } catch (final BonitaException | IOException e) {
            throw new APIException(e);
        } finally {
            if (attributes.containsKey(CaseDocumentItem.ATTRIBUTE_UPLOAD_PATH)) {
                tenantFolder.removeUploadedTempContent(attributes.get(CaseDocumentItem.ATTRIBUTE_UPLOAD_PATH));
            }
        }

    }

    protected DocumentValue buildDocumentValueFromUploadPath(final String uploadKey, final int index, String fileName)
            throws IOException {

        String mimeType = null;
        byte[] fileContent = null;

        if (uploadKey != null) {
            try {
                final FileContent theSourceFile = tenantFolder.retrieveUploadedTempContent(uploadKey);
                if (theSourceFile.getSize() > maxSizeForTenant * 1048576) {
                    final String errorMessage = "This document is exceeded " + maxSizeForTenant + "Mb";
                    throw new DocumentException(errorMessage);
                }
                try (InputStream inputStream = theSourceFile.getInputStream()) {
                    fileContent = IOUtils.toByteArray(inputStream);
                    if (StringUtil.isBlank(fileName)) {
                        fileName = theSourceFile.getFileName();
                    }
                    mimeType = theSourceFile.getMimeType();
                }
            } catch (BonitaException e) {
                throw new FileNotFoundException("Cannot find " + uploadKey + " in the tenant temp directory.");
            }
        }

        final DocumentValue documentValue = new DocumentValue(fileContent, mimeType, fileName);
        if (index != -1) {
            documentValue.setIndex(index);
        }
        return documentValue;
    }

    protected DocumentValue buildDocumentValueFromUrl(final String urlPath, final int index) {

        final DocumentValue documentValue = new DocumentValue(urlPath);
        if (index != -1) {
            documentValue.setIndex(index);
        }
        return documentValue;

    }

    // GET Method for SEARCH
    public ItemSearchResult<CaseDocumentItem> search(final int page, final int resultsByPage,
            final String search, final Map<String, String> filters, final String orders) {
        return searchDocument(page, resultsByPage, search, filters, orders);
    }

    protected ItemSearchResult<CaseDocumentItem> searchDocument(final int page, final int resultsByPage,
            final String search,
            final Map<String, String> filters, final String orders) {

        try {
            final APIID supervisorAPIID = APIID.makeAPIID(filters.get(CaseDocumentItem.FILTER_SUPERVISOR_ID));

            if (supervisorAPIID != null) {
                filters.remove(CaseDocumentItem.FILTER_SUPERVISOR_ID);
            }
            searchOptionsCreator = buildSearchOptionCreator(page, resultsByPage, search, filters, orders);

            final SearchResult<Document> engineSearchResults;
            if (supervisorAPIID != null && supervisorAPIID.isValidLongID()) {
                engineSearchResults = processAPI.searchDocumentsSupervisedBy(supervisorAPIID.toLong(),
                        searchOptionsCreator.create());
            } else {
                engineSearchResults = processAPI.searchDocuments(searchOptionsCreator.create());
            }
            return new ItemSearchResult<>(page, resultsByPage, engineSearchResults.getCount(),
                    convertEngineToConsoleItem(engineSearchResults.getResult()));
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    protected SearchOptionsCreator buildSearchOptionCreator(final int page, final int resultsByPage,
            final String search, final Map<String, String> filters,
            final String orders) {
        return new SearchOptionsCreator(page, resultsByPage, search, new Sorts(orders,
                getDocumentSearchAttributeConverter()),
                new Filters(filters, new CaseDocumentFilterCreator(getDocumentSearchAttributeConverter())));
    }

    private CaseDocumentSearchAttributeConverter getDocumentSearchAttributeConverter() {
        return new CaseDocumentSearchAttributeConverter();
    }

    // DELETE Method
    @Override
    public void delete(final List<APIID> ids) {
        if (ids != null) {

            try {
                for (final APIID id : ids) {
                    processAPI.removeDocument(id.toLong());
                }
            } catch (final DocumentNotFoundException e) {
                throw new APIException("Error while deleting a document. Document not found");
            } catch (final DeletionException e) {
                throw new APIException(e);
            }

        } else {
            throw new APIException("Error while deleting a document. Document id not specified in the request");
        }
    }

    @Override
    protected CaseDocumentItem convertEngineToConsoleItem(final Document item) {
        if (item != null) {
            return new CaseDocumentItemConverter().convert(item);
        }
        return null;
    }

    private List<CaseDocumentItem> convertEngineToConsoleItem(final List<Document> result) {
        if (result != null) {
            return new CaseDocumentItemConverter().convert(result);
        }
        return null;
    }
}
