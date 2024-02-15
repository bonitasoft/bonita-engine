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
package org.bonitasoft.web.rest.model.document;

import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * Document item
 *
 * @author Gai Cuisha
 */
public class DocumentItem extends Item {

    public DocumentItem() {
        super();
    }

    public DocumentItem(final IItem item) {
        super(item);
    }

    public static final String DOCUMENT_ID = "id";

    public static final String DOCUMENT_VERSION = "version";

    public static final String PROCESSINSTANCE_ID = "processinstanceId";

    public static final String PROCESSINSTANCE_NAME = "processinstanceName";

    public static final String PROCESS_DISPLAY_NAME = "processDisplayName";

    public static final String PROCESS_VERSION = "processinstanceVersion";

    public static final String DOCUMENT_NAME = "documentName";

    public static final String DOCUMENT_AUTHOR = "documentAuthor";

    public static final String DOCUMENT_CREATIONDATE = "documentCreationDate";

    public static final String DOCUMENT_HAS_CONTENT = "documentHasContent";

    public static final String DOCUMENT_CONTENT_FILENAME = "documentContentFileName";

    public static final String DOCUMENT_CONTENT_MIMETYPE = "documentContentMimeType";

    public static final String CONTENT_STORAGE_ID = "contentStorageId";

    public static final String DOCUMENT_URL = "documentURL";

    public static final String DOCUMENT_CREATION_TYPE = "DOCUMENT_CREATION_TYPE";

    public static final String DOCUMENT_UPLOAD = "documentUpload";

    /*
     * Filter are in uppercase due to a refactoring (delete FilterKey class), please put them in lowercase if there is
     * no side effect
     */
    public static final String FILTER_CASE_ID = "CASE_ID";
    public static final String FILTER_VIEW_TYPE = "VIEW";
    public static final String FILTER_USER_ID = "USER_ID";

    /* idem for values */
    public static final String VALUE_VIEW_TYPE_ADMINISTRATOR = "ADMINISTRATOR";
    public static final String VALUE_VIEW_TYPE_USER = "USER";
    public static final String VALUE_VIEW_TYPE_TEAM_MANAGER = "TEAM_MANAGER";
    public static final String VALUE_VIEW_TYPE_PROCESS_OWNER = "PROCESS_OWNER";

    public void setDocumentId(final String documentId) {
        this.setAttribute(DOCUMENT_ID, documentId);
    }

    public void setDocumentVersion(final String documentVersion) {
        this.setAttribute(DOCUMENT_VERSION, documentVersion);
    }

    public void setCaseId(final String caseId) {
        this.setAttribute(PROCESSINSTANCE_ID, caseId);
    }

    public void setCaseName(final String caseName) {
        this.setAttribute(PROCESSINSTANCE_NAME, caseName);
    }

    public void setProcessDisplayName(final String caseName) {
        this.setAttribute(PROCESS_DISPLAY_NAME, caseName);
    }

    public void setProcessVersion(final String caseVersion) {
        this.setAttribute(PROCESS_VERSION, caseVersion);
    }

    public void setDocumentName(final String documentName) {
        this.setAttribute(DOCUMENT_NAME, documentName);
    }

    public void setDocumentAuthor(final APIID userId) {
        this.setAttribute(DOCUMENT_AUTHOR, userId.toString());
    }

    public void setDocumentAuthor(final Long userId) {
        this.setAttribute(DOCUMENT_AUTHOR, userId.toString());
    }

    public void setDocumentCreationDate(final String documentCreationDate) {
        this.setAttribute(DOCUMENT_CREATIONDATE, documentCreationDate);
    }

    public void setDocumentHasContent(final String documentHasContent) {
        this.setAttribute(DOCUMENT_HAS_CONTENT, documentHasContent);
    }

    public void setDocumentFileName(final String documentFileName) {
        this.setAttribute(DOCUMENT_CONTENT_FILENAME, documentFileName);
    }

    public void setDocumentMIMEType(final String documentMIMEType) {
        this.setAttribute(DOCUMENT_CONTENT_MIMETYPE, documentMIMEType);
    }

    public void setDocumentStorageId(final String documentStorageId) {
        this.setAttribute(CONTENT_STORAGE_ID, documentStorageId);
    }

    public void setDocumentURL(final String documentURL) {
        this.setAttribute(DOCUMENT_URL, documentURL);
    }

    public void setDocumentCreationType(final String documentCreationType) {
        this.setAttribute(DOCUMENT_CREATION_TYPE, documentCreationType);
    }

    public void setDocumentUpload(final String documentUpload) {
        this.setAttribute(DOCUMENT_UPLOAD, documentUpload);
    }

    @Override
    public ItemDefinition getItemDefinition() {
        return new DocumentDefinition();
    }

}
