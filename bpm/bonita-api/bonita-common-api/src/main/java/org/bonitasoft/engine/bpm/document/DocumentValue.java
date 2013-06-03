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
import java.util.Arrays;

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

    private final boolean hasContent;

    /**
     * Represent the value of a document, content, mime type and file name is given
     * 
     * @param content
     *            content of the document
     * @param mimeType
     *            mime type of the document
     * @param fileName
     *            file name of the document
     */
    public DocumentValue(final byte[] content, final String mimeType, final String fileName) {
        super();
        this.content = content;
        this.mimeType = mimeType;
        this.fileName = fileName;
        hasContent = true;
    }

    /**
     * Represent the value of an external document, only the url is given
     * 
     * @param url
     *            url of the document
     */
    public DocumentValue(final String url) {
        super();
        this.url = url;
        hasContent = false;
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

    public boolean hasContent() {
        return hasContent;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(content);
        result = prime * result + (fileName == null ? 0 : fileName.hashCode());
        result = prime * result + (hasContent ? 1231 : 1237);
        result = prime * result + (mimeType == null ? 0 : mimeType.hashCode());
        result = prime * result + (url == null ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DocumentValue other = (DocumentValue) obj;
        if (!Arrays.equals(content, other.content)) {
            return false;
        }
        if (fileName == null) {
            if (other.fileName != null) {
                return false;
            }
        } else if (!fileName.equals(other.fileName)) {
            return false;
        }
        if (hasContent != other.hasContent) {
            return false;
        }
        if (mimeType == null) {
            if (other.mimeType != null) {
                return false;
            }
        } else if (!mimeType.equals(other.mimeType)) {
            return false;
        }
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DocumentValue [content=" + Arrays.toString(content) + ", mimeType=" + mimeType + ", fileName=" + fileName + ", url=" + url + ", hasContent="
                + hasContent + "]";
    }

}
