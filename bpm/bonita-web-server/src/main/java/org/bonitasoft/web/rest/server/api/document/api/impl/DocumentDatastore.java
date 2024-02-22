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
package org.bonitasoft.web.rest.server.api.document.api.impl;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.preferences.properties.PropertiesFactory;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentAttachmentException;
import org.bonitasoft.engine.bpm.document.DocumentException;
import org.bonitasoft.engine.bpm.process.*;
import org.bonitasoft.engine.exception.*;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.rest.model.document.ArchivedDocumentItem;
import org.bonitasoft.web.rest.model.document.DocumentItem;

/**
 * Document data store
 *
 * @author Yongtao Guo
 */
public class DocumentDatastore {

    private final APISession apiSession;

    public final static String CREATE_NEW_VERSION_DOCUMENT = "AddNewVersionDocument";

    public final static String CREATE_NEW_DOCUMENT = "AddNewDocument";

    /**
     * Default constructor.
     */
    public DocumentDatastore(final APISession apiSession) {
        this.apiSession = apiSession;
    }

    public SearchResult<Document> searchDocuments(final long userId, final String viewType,
            final SearchOptionsBuilder builder) throws InvalidSessionException,
            BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException, SearchException, NotFoundException {
        final ProcessAPI processAPI = getProcessAPI();
        if (DocumentItem.VALUE_VIEW_TYPE_ADMINISTRATOR.equals(viewType)
                || DocumentItem.VALUE_VIEW_TYPE_USER.equals(viewType)) {
            return processAPI.searchDocuments(builder.done());
        } else if (DocumentItem.VALUE_VIEW_TYPE_TEAM_MANAGER.equals(viewType)) {
            return processAPI.searchDocuments(builder.done());
        } else if (DocumentItem.VALUE_VIEW_TYPE_PROCESS_OWNER.equals(viewType)) {
            return processAPI.searchDocumentsSupervisedBy(userId, builder.done());
        }
        throw new IllegalArgumentException("Invalid view type.");
    }

    public SearchResult<ArchivedDocument> searchArchivedDocuments(final long userId, final String viewType,
            final SearchOptionsBuilder builder)
            throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException,
            SearchException, NotFoundException {
        final ProcessAPI processAPI = getProcessAPI();
        if (DocumentItem.VALUE_VIEW_TYPE_ADMINISTRATOR.equals(viewType)
                || DocumentItem.VALUE_VIEW_TYPE_USER.equals(viewType)
                || DocumentItem.VALUE_VIEW_TYPE_TEAM_MANAGER.equals(viewType)) {
            return processAPI.searchArchivedDocuments(builder.done());
        } else if (DocumentItem.VALUE_VIEW_TYPE_PROCESS_OWNER.equals(viewType)) {
            return processAPI.searchArchivedDocumentsSupervisedBy(userId, builder.done());
        }
        throw new IllegalArgumentException("Invalid view type.");
    }

    public DocumentItem createDocument(final long processInstanceId, final String documentName,
            final String documentCreationType, final String path, final BonitaHomeFolderAccessor tenantFolder)
            throws BonitaException, IOException, InvalidSessionException, RetrieveException {

        DocumentItem item = new DocumentItem();
        final ProcessAPI processAPI = getProcessAPI();

        final FileContent theSourceFile = tenantFolder.retrieveUploadedTempContent(path);

        try (InputStream inputStream = theSourceFile.getInputStream()) {
            final long maxSize = PropertiesFactory.getConsoleProperties().getMaxSize();
            if (theSourceFile.getSize() > maxSize * 1048576) {
                final String errorMessage = "This document is exceeded " + maxSize + "Mo";
                throw new DocumentException(errorMessage);
            }
            byte[] fileContent = IOUtils.toByteArray(inputStream);
            String fileName = theSourceFile.getFileName();
            String mimeType = theSourceFile.getMimeType();

            // Attach a new document to a case
            if (CREATE_NEW_DOCUMENT.equals(documentCreationType)) {
                final Document document = processAPI.attachDocument(processInstanceId, documentName, fileName, mimeType,
                        fileContent);
                item = mapToDocumentItem(document);
            } else if (CREATE_NEW_VERSION_DOCUMENT.equals(documentCreationType)) {
                final Document document = processAPI.attachNewDocumentVersion(processInstanceId, documentName, fileName,
                        mimeType, fileContent);
                item = mapToDocumentItem(document);
            }
            return item;
        } finally {
            tenantFolder.removeUploadedTempContent(path);
        }
    }

    protected ProcessAPI getProcessAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getProcessAPI(apiSession);
    }

    public DocumentItem createDocumentFromUrl(final long processInstanceId, final String documentName,
            final String documentCreationType, final String path)
            throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException,
            ProcessInstanceNotFoundException,
            DocumentAttachmentException, IOException, RetrieveException, ProcessDefinitionNotFoundException {

        DocumentItem item = new DocumentItem();
        final ProcessAPI processAPI = getProcessAPI();
        final String fileName = DocumentUtil.getFileNameFromUrl(path);
        final String mimeType = DocumentUtil.getMimeTypeFromUrl(path);
        if (fileName != null && mimeType != null) {
            // Attach a new document to a case
            if (CREATE_NEW_DOCUMENT.equals(documentCreationType)) {
                final Document document = processAPI.attachDocument(processInstanceId, documentName, fileName, mimeType,
                        path);
                item = mapToDocumentItem(document);
            } else if (CREATE_NEW_VERSION_DOCUMENT.equals(documentCreationType)) {
                final Document document = processAPI.attachNewDocumentVersion(processInstanceId, documentName, fileName,
                        mimeType, path);
                item = mapToDocumentItem(document);
            }
        }

        return item;
    }

    public DocumentItem mapToDocumentItem(final Document document)
            throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException, ProcessDefinitionNotFoundException, RetrieveException {

        if (document == null) {
            throw new IllegalArgumentException("The document must be not null!");
        }
        DocumentItem item = new DocumentItem();
        final ProcessAPI processAPI = getProcessAPI();
        ProcessInstance processInstance;
        String caseName = "";
        String processDisplayName = "";
        String version = "";
        try {
            processInstance = processAPI.getProcessInstance(document.getProcessInstanceId());
            caseName = processInstance.getName();
            final ProcessDeploymentInfo processDeploymentInfo = processAPI
                    .getProcessDeploymentInfo(processInstance.getProcessDefinitionId());
            processDisplayName = processDeploymentInfo.getDisplayName();
            version = processDeploymentInfo.getVersion();
        } catch (final ProcessInstanceNotFoundException e) {
            item = buildDocumentItem(caseName, processDisplayName, version, document);
            return item;
        }

        item = buildDocumentItem(caseName, processDisplayName, version, document);
        return item;

    }

    private DocumentItem buildDocumentItem(final String caseName, final String processDisplayName, final String version,
            final Document document) {
        final DocumentItem item = new DocumentItem();
        item.setDocumentId(String.valueOf(document.getId()));
        item.setCaseId(String.valueOf(document.getProcessInstanceId()));
        item.setDocumentName(document.getName());
        item.setDocumentAuthor(document.getAuthor());
        item.setDocumentFileName(document.getContentFileName());
        item.setDocumentCreationDate(parseDate(document.getCreationDate()));
        item.setDocumentMIMEType(document.getContentMimeType());
        item.setDocumentHasContent(String.valueOf(document.hasContent()));
        item.setDocumentStorageId(document.getContentStorageId());
        item.setDocumentURL(document.getUrl());
        item.setProcessDisplayName(processDisplayName);
        item.setProcessVersion(version);
        item.setCaseName(caseName);
        return item;
    }

    public ArchivedDocumentItem mapToArchivedDocumentItem(final ArchivedDocument document)
            throws InvalidSessionException, BonitaHomeNotSetException,
            ServerAPIException, UnknownAPITypeException, RetrieveException, ProcessDefinitionNotFoundException {

        if (document == null) {
            throw new IllegalArgumentException("The document must be not null!");
        }
        final ProcessAPI processAPI = getProcessAPI();
        ArchivedDocumentItem item = new ArchivedDocumentItem();
        String caseName = "";
        String processDisplayName = "";
        String version = "";

        List<ArchivedProcessInstance> archivedCaseList;
        try {
            archivedCaseList = processAPI.getArchivedProcessInstances(document.getProcessInstanceId(), 0, 1);
            final ArchivedProcessInstance processInstance = archivedCaseList.get(0);
            caseName = processInstance.getName();
            final ProcessDeploymentInfo processDeploymentInfo = processAPI
                    .getProcessDeploymentInfo(processInstance.getProcessDefinitionId());
            processDisplayName = processDeploymentInfo.getDisplayName();
            version = processDeploymentInfo.getVersion();
        } catch (final NotFoundException e) {
            item = buildArchivedDocumentItem(caseName, processDisplayName, version, document);
            return item;
        }

        item = buildArchivedDocumentItem(caseName, processDisplayName, version, document);
        return item;

    }

    private ArchivedDocumentItem buildArchivedDocumentItem(final String caseName, final String processDisplayName,
            final String version,
            final ArchivedDocument document) {
        final ArchivedDocumentItem item = new ArchivedDocumentItem();
        item.setDocumentId(String.valueOf(document.getSourceObjectId()));
        item.setDocumentSourceObjectId(String.valueOf(document.getSourceObjectId()));
        item.setCaseId(String.valueOf(document.getProcessInstanceId()));
        item.setDocumentName(document.getName());
        item.setDocumentAuthor(document.getAuthor());
        item.setDocumentFileName(document.getContentFileName());
        item.setDocumentCreationDate(parseDate(document.getCreationDate()));
        item.setDocumentMIMEType(document.getContentMimeType());
        item.setDocumentHasContent(String.valueOf(document.hasContent()));
        item.setDocumentStorageId(document.getContentStorageId());
        item.setDocumentURL(document.getUrl());
        item.setProcessDisplayName(processDisplayName);
        item.setProcessVersion(version);
        item.setCaseName(caseName);
        item.setArchivedDate(parseDate(document.getArchiveDate()));
        return item;
    }

    private String parseDate(final Date date) {
        String dateStr = null;
        if (date != null) {
            final DateFormat time = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.ENGLISH);
            final DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH);
            dateStr = time.format(date) + ", " + df.format(date);
        }
        return dateStr;
    }

}
