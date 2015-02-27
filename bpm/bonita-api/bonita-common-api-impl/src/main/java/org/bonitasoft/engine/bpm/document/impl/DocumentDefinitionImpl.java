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
package org.bonitasoft.engine.bpm.document.impl;

import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;

/**
 * @author Baptiste Mesta
 */
public class DocumentDefinitionImpl extends NamedElementImpl implements DocumentDefinition {

    private static final long serialVersionUID = -2478390362777026410L;

    private String url;

    private String file;

    private String mimeType;

    private String description;

    private String fileName;

    /**
     * @param name
     */
    public DocumentDefinitionImpl(final String name) {
        super(name);
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getFile() {
        return file;
    }

    @Override
    public String getContentMimeType() {
        return mimeType;
    }

    /**
     * @param description
     */
    public void setDescription(final String description) {
        this.description = description;

    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setFile(final String file) {
        this.file = file;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (file == null ? 0 : file.hashCode());
        result = prime * result + (fileName == null ? 0 : fileName.hashCode());
        result = prime * result + (mimeType == null ? 0 : mimeType.hashCode());
        result = prime * result + (url == null ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DocumentDefinitionImpl other = (DocumentDefinitionImpl) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (file == null) {
            if (other.file != null) {
                return false;
            }
        } else if (!file.equals(other.file)) {
            return false;
        }
        if (fileName == null) {
            if (other.fileName != null) {
                return false;
            }
        } else if (!fileName.equals(other.fileName)) {
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
        final StringBuilder builder = new StringBuilder();
        builder.append("DocumentDefinitionImpl [url=");
        builder.append(url);
        builder.append(", file=");
        builder.append(file);
        builder.append(", mimeType=");
        builder.append(mimeType);
        builder.append(", description=");
        builder.append(description);
        builder.append(", fileName=");
        builder.append(fileName);
        builder.append(", getName()=");
        builder.append(getName());
        builder.append("]");
        return builder.toString();
    }

}
