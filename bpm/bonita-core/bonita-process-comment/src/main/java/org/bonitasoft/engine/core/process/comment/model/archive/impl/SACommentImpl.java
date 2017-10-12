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
package org.bonitasoft.engine.core.process.comment.model.archive.impl;

import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.core.process.comment.model.archive.SAComment;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 */
public class SACommentImpl implements SAComment {

    private static final long serialVersionUID = -1321200639098435989L;

    private long id;

    private long tenantId;

    private Long userId;

    private long processInstanceId;

    private long sourceObjectId;

    private long postDate;

    private long archiveDate;

    private String content;

    public SACommentImpl() {
        super();
    }

    public SACommentImpl(final SComment sComment) {
        tenantId = sComment.getTenantId();
        content = sComment.getContent();
        postDate = sComment.getPostDate();
        sourceObjectId = sComment.getId();
        processInstanceId = sComment.getProcessInstanceId();
        userId = sComment.getUserId();
    }

    @Override
    public String getDiscriminator() {
        return SComment.class.getName();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
    }

    @Override
    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(final long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public long getPostDate() {
        return postDate;
    }

    public void setPostDate(final long postDate) {
        this.postDate = postDate;
    }

    @Override
    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    @Override
    public long getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(final long archiveDate) {
        this.archiveDate = archiveDate;
    }

    @Override
    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(final long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (archiveDate ^ archiveDate >>> 32);
        result = prime * result + (content == null ? 0 : content.hashCode());
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (int) (postDate ^ postDate >>> 32);
        result = prime * result + (int) (processInstanceId ^ processInstanceId >>> 32);
        result = prime * result + (int) (sourceObjectId ^ sourceObjectId >>> 32);
        result = prime * result + (int) (tenantId ^ tenantId >>> 32);
        result = prime * result + (userId == null ? 0 : userId.hashCode());
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
        final SACommentImpl other = (SACommentImpl) obj;
        if (archiveDate != other.archiveDate) {
            return false;
        }
        if (content == null) {
            if (other.content != null) {
                return false;
            }
        } else if (!content.equals(other.content)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (postDate != other.postDate) {
            return false;
        }
        if (processInstanceId != other.processInstanceId) {
            return false;
        }
        if (sourceObjectId != other.sourceObjectId) {
            return false;
        }
        if (tenantId != other.tenantId) {
            return false;
        }
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SACommentImpl [id=" + id + ", tenantId=" + tenantId + ", userId=" + userId + ", processInstanceId=" + processInstanceId + ", sourceObjectId="
                + sourceObjectId + ", postDate=" + postDate + ", archiveDate=" + archiveDate + ", content=" + content + "]";
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SComment.class;
    }

}
