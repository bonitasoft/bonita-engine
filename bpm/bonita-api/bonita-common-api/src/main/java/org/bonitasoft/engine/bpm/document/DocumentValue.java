/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.document;

import java.io.Serializable;

/**
 * Class that holds a content + mime type and a name OR the url if it's an external document.
 * It is used by DOCUMENT_CREATE_UPDATE operations to create or update a document stored in bonita
 * 
 * @author Baptiste Mesta
 */
public class DocumentValue implements Serializable {

    private static final long serialVersionUID = -637083535741620642L;

    private byte[] content;

    private String mimeType;

    private String fileName;

    private String url;

    private boolean hasContent;

    private Long documentId;

    private boolean hasChanged;

    private int index = -1;

    /**
     * Represent the value of a document. Content, mime type and file name are given
     * 
     * @param content
     *        content of the document
     * @param mimeType
     *        mime type of the document
     * @param fileName
     *        file name of the document
     */
    public DocumentValue(final byte[] content, final String mimeType, final String fileName) {
        super();
        this.content = content;
        this.mimeType = mimeType;
        this.fileName = fileName;
        hasContent = true;
        hasChanged = false;
        documentId = null;
    }

    /**
     * Represent the value of an external document, only the url is given
     *
     * @param url
     *        url of the document
     */
    public DocumentValue(final String url) {
        super();
        this.url = url;
        hasContent = false;
        hasChanged = false;
        documentId = null;
    }

    /**
     * Represent an existing document that did not changed.
     * It is used only in operations to update document list.
     *
     * @param documentId
     *        the id of the existing document (mapping)
     */
    public DocumentValue(final long documentId) {
        super();
        hasChanged = false;
        this.documentId = documentId;
    }

    /**
     * Represent an existing document that changed with the content and metadata in parameters.
     * It is used only in operations to update document list.
     * 
     * @param documentId
     *        the id of the existing document (mapping)
     * @param content
     *        content of the document
     * @param mimeType
     *        mime type of the document
     * @param fileName
     *        file name of the document
     */
    public DocumentValue(final long documentId, final byte[] content, final String mimeType, final String fileName) {
        super();
        this.content = content;
        this.mimeType = mimeType;
        this.fileName = fileName;
        hasContent = true;
        hasChanged = true;
        this.documentId = documentId;
    }

    /**
     * Represent an existing document that changed to an external document.
     * It is used only in operations to update document list.
     *
     * @param documentId
     *        the id of the existing document (mapping)
     */
    public DocumentValue(final long documentId, final String url) {
        super();
        this.url = url;
        hasContent = false;
        hasChanged = true;
        this.documentId = documentId;
    }

    public byte[] getContent() {
        return content;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUrl() {
        return url;
    }

    /**
     * @return true if the document to create is stored internally or externally with an URL
     */
    public boolean hasContent() {
        return hasContent;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHasContent(boolean hasContent) {
        this.hasContent = hasContent;
    }

    /**
     * Indicate which document will be updated <br>
     * Used only when updating list of document
     *
     * @return the id of the document to update
     */
    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    /**
     * If the document value updates an existing document, this getter tels us if the content is modified and should be updated
     *
     * @return true if the content of the original document has changed
     */
    public boolean hasChanged() {
        return hasChanged;
    }

    public void setHasChanged(boolean hasChanged) {
        this.hasChanged = hasChanged;
    }

    public int getIndex() {
        return index;
    }

    /**
     * Index of were to put the document inside the list. Only used when using API method attach document.
     * 
     * @param index
     *        index in the list
     */
    public DocumentValue setIndex(int index) {
        this.index = index;
        return this;
    }
}
