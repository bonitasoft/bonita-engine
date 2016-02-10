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
package org.bonitasoft.engine.bpm.comment.impl;

import org.bonitasoft.engine.bpm.comment.Comment;

/**
 * @author Hongwen Zang
 * @author Matthieu Chaffotte
 */
public class CommentImpl implements Comment {

    private static final long serialVersionUID = 2599025748483260550L;

    private long id;

    private long tenantId;

    private Long userId;

    private long processInstanceId;

    private long postDate;

    private String content;

    public CommentImpl() {
        super();
    }

    /**
     * @deprecated As of 6.1 use {@link #CommentImpl()} and the setters
     */
    @Deprecated
    public CommentImpl(final long id, final long tenantId, final long userId, final long processInstanceId, final long postDate, final String content) {
        super();
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.processInstanceId = processInstanceId;
        this.postDate = postDate;
        this.content = content;
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public long getProcessInstanceId() {
        return processInstanceId;
    }

    @Override
    public long getPostDate() {
        return postDate;
    }

    @Override
    public String getContent() {
        return content;
    }

    public void setProcessInstanceId(final long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public void setPostDate(final long postDate) {
        this.postDate = postDate;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
    }

    @Override
    @Deprecated
    public long getTenantId() {
        return tenantId;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Deprecated
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (content == null ? 0 : content.hashCode());
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (int) (postDate ^ postDate >>> 32);
        result = prime * result + (int) (processInstanceId ^ processInstanceId >>> 32);
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
        final CommentImpl other = (CommentImpl) obj;
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
        return "CommentImpl [id=" + id + ", tenantId=" + tenantId + ", userId=" + userId + ", processInstanceId=" + processInstanceId + ", postDate="
                + postDate + ", content=" + content + "]";
    }

}
