/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.document.model.impl;

import java.util.Arrays;

import org.bonitasoft.engine.document.model.SDocumentContent;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 */
public class SDocumentContentImpl implements SDocumentContent {

    private static final long serialVersionUID = -3292133976128211487L;

    private long id;

    private long tenantId;

    private String documentId;

    private byte[] content;

    public SDocumentContentImpl() {
        super();
    }

    public SDocumentContentImpl(final long id, final String storageId, final byte[] content) {
        super();
        this.id = id;
        documentId = storageId;
        this.content = content;
    }

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getDiscriminator() {
        return null;
    }

    @Override
    public String getStorageId() {
        return documentId;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    public void setStorageId(final String storageId) {
        documentId = storageId;
    }

    public void setContent(final byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("SDocumentContentImpl [id=").append(id);
        builder.append(", storageId=").append(documentId);
        builder.append(", content=").append(Arrays.toString(content));
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(content);
        result = prime * result + (documentId == null ? 0 : documentId.hashCode());
        result = prime * result + (int) (tenantId ^ tenantId >>> 32);
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
        final SDocumentContentImpl other = (SDocumentContentImpl) obj;
        if (!Arrays.equals(content, other.content)) {
            return false;
        }
        if (documentId == null) {
            if (other.documentId != null) {
                return false;
            }
        } else if (!documentId.equals(other.documentId)) {
            return false;
        }
        if (tenantId != other.tenantId) {
            return false;
        }
        return true;
    }

}
